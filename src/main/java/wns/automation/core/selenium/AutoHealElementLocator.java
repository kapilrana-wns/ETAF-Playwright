package wns.automation.core.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.aventstack.extentreports.Status;
import wns.automation.core.ExtentTestManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class AutoHealElementLocator implements ElementLocator {

    private final WebDriver driver;
    private final List<By> locators;
    private final String fieldName;
    private final String pageName;
    private final AutoHealConfig config;

    private int lastHealIndex = -1;
    private long lastHealTimeMs = 0;
    private By cachedHealedBy = null;

    public AutoHealElementLocator(WebDriver driver, List<By> locators, String fieldName, String pageName) {
        this.driver = driver;
        this.locators = locators;
        this.fieldName = fieldName;
        this.pageName = pageName;
        this.config = AutoHealConfig.getInstance();
    }

    @Override
    public WebElement findElement() {
        lastHealIndex = -1;
        lastHealTimeMs = 0;
        long startTime = System.currentTimeMillis();

        // 0. Try cached healed locator from previous call in this session
        if (cachedHealedBy != null) {
            try {
                WebElement element = driver.findElement(cachedHealedBy);
                lastHealTimeMs = System.currentTimeMillis() - startTime;
                return element;
            } catch (NoSuchElementException e) {
                cachedHealedBy = null;
            }
        }

        By primaryBy = locators.get(0);

        // 1. Immediate attempt with primary
        try {
            WebElement element = driver.findElement(primaryBy);
            lastHealTimeMs = System.currentTimeMillis() - startTime;
            return element;
        } catch (NoSuchElementException e) {
            // continue to retry
        }

        // 2. Explicit wait for primary (handles timing-related failures)
        int waitTimeout = config.getWaitTimeout();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitTimeout));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(primaryBy));
            lastHealTimeMs = System.currentTimeMillis() - startTime;
            return element;
        } catch (TimeoutException e) {
            // continue to retry
        }

        // 3. Retry primary N times with small delay
        int retryCount = config.getRetryPrimary();
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                Thread.sleep(300);
                WebElement element = driver.findElement(primaryBy);
                lastHealTimeMs = System.currentTimeMillis() - startTime;
                return element;
            } catch (NoSuchElementException e) {
                System.out.println("[AUTO-HEAL] Primary retry " + attempt + "/" + retryCount
                        + " for '" + fieldName + "' on " + pageName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 4. Try fallback locators (auto-generated + healed)
        for (int i = 1; i < locators.size(); i++) {
            By by = locators.get(i);
            try {
                WebElement element = driver.findElement(by);
                lastHealIndex = i;
                lastHealTimeMs = System.currentTimeMillis() - startTime;

                logHealSuccess(i, by);
                AutoHealMetrics.getInstance().recordHeal(
                        pageName, fieldName, extractFallbackType(by), lastHealTimeMs);
                HealedLocatorStore.getInstance().setHealedLocator(
                        pageName, fieldName, by.toString());
                cachedHealedBy = by;
                takeScreenshot();

                return element;
            } catch (NoSuchElementException | InvalidSelectorException e) {
                System.out.println("[AUTO-HEAL] Fallback locator failed: " + by + " (" + e.getClass().getSimpleName() + ")");
            }
        }

        // 5. DOM similarity matching (last resort)
        lastHealTimeMs = System.currentTimeMillis() - startTime;
        System.out.println("[AUTO-HEAL] Step 5: Attempting DOM similarity matching for '" + fieldName + "' on " + pageName);

        if (config.isEnabled()) {
            DomSimilarityMatcher.DomMatchResult domResult = DomSimilarityMatcher.findSimilarElement(
                    driver, primaryBy, fieldName, pageName);
            if (domResult != null) {
                lastHealIndex = locators.size();
                HealedLocatorStore.getInstance().setHealedLocator(
                        pageName, fieldName, domResult.matchedBy.toString());
                AutoHealMetrics.getInstance().recordHeal(
                        pageName, fieldName, "DOM-Similarity", lastHealTimeMs);
                cachedHealedBy = domResult.matchedBy;
                takeScreenshot();
                return domResult.element;
            }
        }

        // 6. All strategies exhausted
        AutoHealMetrics.getInstance().recordFailure(pageName, fieldName);
        logHealFailed();

        throw new NoSuchElementException(
                "AutoHeal: Could not find element '" + fieldName
                        + "' with any of " + locators.size() + " locator strategies");
    }

    @Override
    public List<WebElement> findElements() {
        for (int i = 0; i < locators.size(); i++) {
            By by = locators.get(i);
            List<WebElement> elements = driver.findElements(by);
            if (!elements.isEmpty()) {
                return elements;
            }
        }
        return driver.findElement(locators.get(0)).findElements(By.xpath(".."));
    }

    public int getLastHealIndex() {
        return lastHealIndex;
    }

    public long getLastHealTimeMs() {
        return lastHealTimeMs;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getPageName() {
        return pageName;
    }

    public By getPrimaryBy() {
        return locators.isEmpty() ? null : locators.get(0);
    }

    public By getLastSuccessfulBy() {
        if (lastHealIndex >= 0 && lastHealIndex < locators.size()) {
            return locators.get(lastHealIndex);
        }
        return null;
    }

    public int getLocatorCount() {
        return locators.size();
    }

    private void logHealSuccess(int index, By by) {
        System.out.println();
        System.out.println("[AUTO-HEAL SUCCESS]");
        System.out.println("Element        : " + fieldName);
        System.out.println("Primary Locator: " + locators.get(0));
        System.out.println("Recovered Using: " + by);
        System.out.println("Page           : " + pageName);
        System.out.println("Execution Time : " + lastHealTimeMs + " ms");

        ExtentTestManager.log(Status.WARNING,
                "<span style='color:#E67E22;'>\u26A0 AUTO-HEAL ACTIVATED</span>"
                + "<br><b>Element:</b> " + fieldName
                + "<br><b>Original:</b> " + locators.get(0)
                + "<br><b>Recovered:</b> " + by
                + "<br><b>Page:</b> " + pageName
                + "<br><b>Time:</b> " + lastHealTimeMs + " ms");
    }

    private void logHealFailed() {
        System.out.println();
        System.out.println("[AUTO-HEAL FAILED]");
        System.out.println("Element        : " + fieldName);
        System.out.println("Primary Locator: " + locators.get(0));
        System.out.println("Page           : " + pageName);
        System.out.println("Locators Tried : " + locators.size());

        ExtentTestManager.log(Status.FAIL,
                "<span style='color:#E74C3C;'>\u26A0 AUTO-HEAL FAILED</span>"
                + "<br><b>Element:</b> " + fieldName
                + "<br><b>Locator:</b> " + locators.get(0)
                + "<br><b>Page:</b> " + pageName
                + "<br><b>Strategies Tried:</b> " + locators.size());
    }

    private static String extractFallbackType(By by) {
        String str = by.toString();
        int colon = str.indexOf(':');
        return colon > 0 ? str.substring(0, colon) : str;
    }

    private void takeScreenshot() {
        if (!config.isScreenshotEnabled()) return;
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String dirPath = System.getProperty("user.dir") + "/auto-heal-screenshots/";
            new File(dirPath).mkdirs();
            String filePath = dirPath + pageName + "_" + fieldName + "_" + timestamp + ".png";
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            src.renameTo(new File(filePath));
            System.out.println("[AUTO-HEAL] Screenshot saved: " + filePath);
        } catch (Exception e) {
            System.out.println("[AUTO-HEAL] Screenshot failed: " + e.getMessage());
        }
    }
}
