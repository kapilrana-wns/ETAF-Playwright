# ETAF-NXG Auto-Heal Architecture

## Overview

The auto-heal system is a **locator resilience engine** that sits between the test code and Selenium WebDriver. When a test attempts to interact with a web element that cannot be found (or becomes stale), auto-heal intercepts the call and tries up to **six progressively aggressive strategies** to recover the element. If a fallback locator works, the system remembers it for future runs by persisting it to `healed-locators.json`.

The entire mechanism is built on three Java language features:
1. **JDK Dynamic Proxy** — every `WebElement` field in a page object is replaced with a proxy object
2. **`InvocationHandler`** — every method call on the proxy goes through `invoke()`, enabling stale-element retry
3. **Selenium `ElementLocator` interface** — the proxy delegates to `AutoHealElementLocator.findElement()`, which implements the healing strategy

---

## 1. How the Proxy Intercept Chain Gets Initiated

### Step 1: Page object calls `AutoHealPageFactory.initElements()`

```java
// AutoHealPageFactory.java
public static void initElements(WebDriver driver, Object page) {
    PageFactory.initElements(new AutoHealFieldDecorator(driver), page);
}
```

This is a drop-in replacement for `PageFactory.initElements(driver, this)`. Instead of Selenium's default `DefaultFieldDecorator`, it passes in a custom `AutoHealFieldDecorator`.

**Usage in page objects:**
```java
public class LoginPage {
    @FindBy(how = How.ID, using = "username")
    private WebElement textboxUsername;

    public LoginPage(WebDriver driver) {
        AutoHealPageFactory.initElements(driver, this);  // <-- entry point
    }
}
```

### Step 2: `AutoHealFieldDecorator.decorate()` builds the locator list and creates a proxy

```java
// AutoHealFieldDecorator.java (lines 30-68)
public Object decorate(ClassLoader loader, Field field) {
    // 1. Skip non-WebElement fields or fields without @FindBy
    if (!isWebElement && !isList) return null;
    FindBy findBy = field.getAnnotation(FindBy.class);
    if (findBy == null) return defaultDecorator.decorate(loader, field);
    if (!AutoHealConfig.getInstance().isEnabled()) return defaultDecorator.decorate(loader, field);

    // 2. Build the locator priority list
    List<By> byList = new ArrayList<>();
    byList.add(LocatorHelper.buildByFromFindBy(findBy));      // primary (from @FindBy)
    byList.addAll(LocatorHelper.generateAlternateLocators(findBy)); // auto-generated alternatives

    String healed = HealedLocatorStore.getInstance().getHealedLocator(pageName, fieldName);
    if (healed != null) byList.add(parseByString(healed));   // previously-healed locator

    // 3. Create the locator + handler
    AutoHealElementLocator locator = new AutoHealElementLocator(driver, byList, fieldName, pageName);
    InvocationHandler handler = new AutoHealInvocationHandler(locator);

    // 4. Return a JDK dynamic proxy
    return Proxy.newProxyInstance(loader, new Class[]{WebElement.class}, handler);
}
```

The proxy replaces the real `WebElement` field. Every call to methods like `.click()`, `.sendKeys()`, `.getText()` is now routed through `AutoHealInvocationHandler.invoke()`.

### Step 3: `AutoHealInvocationHandler.invoke()` wraps each call with stale-element retry

```java
// AutoHealInvocationHandler.java (lines 20-39)
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if ("toString".equals(method.getName())) return "AutoHeal proxy for " + locator.getFieldName();

    int retries = 0;
    while (true) {
        try {
            WebElement element = locator.findElement();  // <-- triggers healing strategy
            return method.invoke(element, args);          // call click()/sendKeys() on the resolved element
        } catch (StaleElementReferenceException e) {
            retries++;
            if (retries > maxRetries) throw e;
            // re-locate the element and retry the method call
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
```

This ensures that **every** interaction with the element goes through:
1. `locator.findElement()` — the 6-step healing strategy
2. If the element becomes stale between finding it and calling the method, it re-locates and retries

---

## 2. The 6-Step Healing Strategy (`AutoHealElementLocator.findElement()`)

```java
// AutoHealElementLocator.java (lines 36-120)
```

| Step | Strategy | What happens | Typical use case |
|------|----------|-------------|-----------------|
| **1** | Immediate primary | `driver.findElement(primaryBy)` with **zero wait** | Element is already present |
| **2** | Explicit wait | `WebDriverWait(visibilityOfElementLocated(primaryBy))` using `webDriverTimeDuraiton` timeout | Element appears after JS delay |
| **3** | Primary retries | Retries `primaryBy` up to `autoHealRetryPrimary` times with 300ms gaps | Element load racing |
| **4** | Fallback locators | Iterates through auto-generated alternates + previously-healed locators | Locator changed (e.g., ID → name) |
| **5** | DOM similarity | `DomSimilarityMatcher.findSimilarElement()` — JavaScript fuzzy scan of all interactive elements | No known locator works; last resort |
| **6** | Failure | Records failure metrics, throws `NoSuchElementException` | Element truly missing |

Each successful heal (steps 4 or 5) is:
- Logged to Extent Report as a warning with original and recovered locator
- Persisted to `healed-locators.json` via `HealedLocatorStore`
- Captured as a screenshot (if `autoHealScreenshot=true`)
- Recorded in `AutoHealMetrics` for the shutdown-hook report

---

## 3. Class-by-Class Purpose

### `AutoHealPageFactory` (11 lines)
**Entry point.** A thin wrapper around `PageFactory.initElements()` that injects `AutoHealFieldDecorator`. Drop-in replacement for standard PageFactory — page objects just call `AutoHealPageFactory.initElements(driver, this)` instead.

### `AutoHealFieldDecorator` (88 lines)
**Proxy factory.** Implements Selenium's `FieldDecorator` interface. For each `@FindBy`-annotated `WebElement` field, it:
- Builds a prioritized `List<By>`: `[primary, alternates..., healed]`
- Creates an `AutoHealElementLocator` with that list
- Wraps it in an `AutoHealInvocationHandler`
- Returns `Proxy.newProxyInstance()` — a virtual `WebElement`
- Falls back to `DefaultFieldDecorator` if auto-heal is disabled, field has no `@FindBy`, or is not a `WebElement`
- Contains `parseByString()` to convert persisted locator strings (e.g., `"By.name: password"`) back into `By` objects

### `AutoHealElementLocator` (220 lines)
**Core healing engine.** Implements Selenium's `ElementLocator` interface. The `findElement()` method runs the 6-step strategy. Also:
- `findElements()` — iterates through locators until a non-empty list is found
- `logHealSuccess()` — logs to console and Extent Report with colored HTML
- `logHealFailed()` — logs failure with all strategies tried
- `takeScreenshot()` — saves a timestamped PNG to `auto-heal-screenshots/`
- Exposes `getLastHealIndex()`, `getLastSuccessfulBy()`, `getFieldName()` for other components

### `AutoHealInvocationHandler` (41 lines)
**Stale-element retry loop.** Implements `java.lang.reflect.InvocationHandler`. Every WebElement method call (click, sendKeys, getText, etc.) goes through `invoke()`:
1. Calls `locator.findElement()` to get a fresh element reference
2. Invokes the actual method on that element
3. If `StaleElementReferenceException` is thrown, re-locates and retries (up to `autoHealMaxRetries`)
4. If the intercepted method is `toString()`, returns `"AutoHeal proxy for fieldName"` for debugging

### `AutoHealConfig` (65 lines)
**Singleton configuration provider.** Reads auto-heal settings from `test.properties`:
| Property | Default | Used by |
|----------|---------|---------|
| `autoHealEnabled` | `true` | FieldDecorator — master on/off |
| `autoHealScreenshot` | `false` | ElementLocator — screenshot on heal |
| `autoHealMaxRetries` | `3` | InvocationHandler — stale element retries |
| `autoHealReport` | `true` | Metrics — JVM shutdown report |
| `autoHealRetryPrimary` | `2` | ElementLocator — step 3 primary retries |
| `webDriverTimeDuraiton` | `10` | ElementLocator — step 2 wait timeout |

### `AutoHealMetrics` (119 lines)
**Singleton metrics collector.** Tracks healing statistics using thread-safe data structures:
- `AtomicInteger totalHeals` / `failedHeals` — total counts
- `ConcurrentHashMap<String, AtomicInteger> pageHealCounts` — per-page instability
- `ConcurrentHashMap<String, AtomicInteger> fallbackTypeCounts` — per-fallback-type effectiveness
- `List<Long> healTimes` — individual heal durations for averaging

**JVM shutdown hook:** If `autoHealReport=true`, prints a summary like:
```
========== AUTO-HEAL STATISTICS ==========
Total Heals          : 4
Failed Heals         : 0
Most Unstable Page   : AutoHealDemoPage (4 heals)
Most Common Fallback : By.name (4 times)
Average Heal Time    : 1234 ms
==========================================
```

### `HealedLocatorStore` (76 lines)
**Persistence layer for learned locators.** Uses `ConcurrentHashMap<String, String>` keyed by `"PageName.fieldName"`:
- `load()` — reads `healed-locators.json` on first access (JSONObject)
- `save()` — writes to JSON with pretty-print (2-space indent)
- `setHealedLocator()` — only persists when the value changes (avoids unnecessary I/O)
- **File format:**
  ```json
  {
    "AutoHealDemoPage.textboxPassword": "By.name: password",
    "AutoHealDemoPage.textboxUsername": "By.name: username"
  }
  ```
  This means after the first successful heal, future runs have `By.name: username` available as a fallback immediately, without repeating step 5.

### `DomSimilarityMatcher` (341 lines)
**Last-resort fuzzy DOM matcher.** When all `By`-based strategies fail, this runs a JavaScript in the browser to collect every interactive element's attributes, then scores each against the original locator hints:

1. **`parseLocator(By)`** — reverse-engineers `By.id`, `By.name`, `By.cssSelector`, `By.xpath` into a `SearchHints` object with up to 10 attribute fields (tag, id, name, class, type, text, placeholder, aria-label, label)

2. **`fetchAllElementAttrs(driver)`** — executes JavaScript that queries `querySelectorAll('input, select, textarea, button, a, label, li, span, h1-h4, p, td, th, div[role], *[aria-label], *[data-testid]')` and returns each element's tag, id, name, class, type, placeholder, aria-label, label text, visible text, and computed XPath

3. **`scoreElement(attrs, hints)`** — weighted scoring (max ~140 points):
   | Dimension | Max score | Match condition |
   |-----------|-----------|-----------------|
   | Tag | 15 | Exact match |
   | ID | 25 (exact) / 18 (partial) | Exact or substring |
   | Name | 20 (exact) / 12 (partial) | Exact or substring |
   | Class | 15 (any part matches) | Contains class or part |
   | Type | 10 | Case-insensitive exact |
   | Text | 15 (exact) / 10 (partial) | Visible text |
   | Placeholder | 15 (exact) / 8 (partial) | placeholder attribute |
   | aria-label | 15 (exact) / 8 (partial) | aria-label attribute |
   | Label text | 10 (exact) / 5 (partial) | Associated `<label>` text |

4. Returns the best match if score >= **40** (`MATCH_THRESHOLD`), otherwise returns `null` → step 6 failure

### `LocatorHelper` (96 lines)
**Locator translation utility.** Two static methods:
- `buildByFromFindBy(FindBy)` — converts a `@FindBy(how=..., using=...)` annotation into a single `By` object
- `generateAlternateLocators(FindBy)` — generates a `List<By>` of alternative locator strategies based on the original:

  | Original strategy | Generated alternates |
  |---|---|
  | `ID` | `By.name`, `By.cssSelector("#id")`, `By.xpath("//*[@id='...']")`, `By.cssSelector("input[id='...']")`, `By.xpath("//*[contains(@id,'...')]")` |
  | `NAME` | `By.id`, `By.cssSelector("[name='...']")`, `By.xpath("//*[@name='...']")`, `By.xpath("//*[contains(@name,'...')]")` |
  | `CLASS_NAME` | `By.cssSelector(".class")`, `By.xpath("//*[contains(@class,'...')]")` |
  | `CSS` | `By.xpath` (converted) |
  | `XPATH` | `By.cssSelector` (if simple), tag-agnostic xpath |

---

## 4. Data Flow Diagram

```
Test code:  loginPage.textboxUsername.sendKeys("admin")
                    │
                    ▼
Proxy (JDK Dynamic Proxy)
                    │
                    ▼
AutoHealInvocationHandler.invoke(proxy, "sendKeys", ["admin"])
                    │
            ┌───────┴──────────────┐
            │  retry loop          │  (on StaleElementReferenceException)
            │  (maxRetries=3)      │
            └───────┬──────────────┘
                    │
                    ▼
AutoHealElementLocator.findElement()
                    │
        ┌───────────┼───────────┐
        │  Step 1-3              │  immediate → wait → retry primary
        │  (primaryBy)           │
        └───────────┬───────────┘
                    │  (if failed)
                    ▼
        ┌───────────────────────┐
        │  Step 4               │  fallback locators
        │  (alternates + healed)│
        └───────────┬───────────┘
                    │  (if all failed)
                    ▼
        ┌───────────────────────┐
        │  Step 5               │  DOM similarity matcher
        │  (JavaScript scan)    │
        └───────────┬───────────┘
                    │  (if found)
                    ▼
              WebElement
                    │
                    ▼
         method.invoke(element, args)
         → sendKeys("admin")
```

---

## 5. Configuration Properties

All properties live in `src/test/java/testconfig/test.properties`:

```properties
# Master switch
autoHealEnabled=true

# Screenshot capture on successful heal
autoHealScreenshot=true

# Stale element retry count (in AutoHealInvocationHandler)
autoHealMaxRetries=3

# Print JVM shutdown report (true/false)
autoHealReport=true

# Primary locator retry count (step 3 in AutoHealElementLocator)
autoHealRetryPrimary=2

# Explicit wait timeout for step 2 (also used globally via webDriverTimeDuraiton)
webDriverTimeDuraiton=10
```

---

## 6. Extent Report Integration

Every heal event is logged to the Extent Spark report with colored HTML:

**On successful recovery** (`AutoHealElementLocator.logHealSuccess()`):
```
⚠ AUTO-HEAL ACTIVATED
Element: textboxUsername
Original: By.id: username
Recovered: By.name: username
Time: 1234 ms
```

**On DOM similarity match** (`DomSimilarityMatcher.findSimilarElement()`):
```
⚠ AUTO-HEAL DOM MATCH
Element: textboxUsername
Original: By.id: username
Matched: <input#username>
Score: 85%
```

**On complete failure** (`AutoHealElementLocator.logHealFailed()`):
```
⚠ AUTO-HEAL FAILED
Element: textboxUsername
Locator: By.id: username
Strategies Tried: 7
```

---

## 7. File Storage

| Artifact | Location | Format |
|----------|----------|--------|
| Healed locators | `./healed-locators.json` | JSON `{"Page.field": "By.type: value"}` |
| Screenshots | `./auto-heal-screenshots/` | `{Page}_{field}_{timestamp}.png` |

The `healed-locators.json` file acts as a **learning cache** — after the first test run heals `textboxUsername` to `By.name: username`, the next run loads this upfront via `HealedLocatorStore.load()` and adds it to the priority list immediately (step 4), making the heal faster or potentially avoiding the need for DOM scanning entirely.
