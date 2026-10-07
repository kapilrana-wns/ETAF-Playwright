package wns.automation.core.playwright;

import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import wns.automation.core.ExtentTestManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AutoHealElementLocator {

    private final Page page;
    private final List<String> selectors;
    private final String fieldName;
    private final String pageName;
    private final AutoHealConfig config;
    private final Integer index;

    private int lastHealIndex = -1;
    private long lastHealTimeMs;
    private String cachedHealedSelector;

    public AutoHealElementLocator(Page page, List<String> selectors, String fieldName, String pageName) {
        this(page, selectors, fieldName, pageName, null);
    }

    private AutoHealElementLocator(Page page, List<String> selectors, String fieldName,
                                   String pageName, Integer index) {
        if (selectors == null || selectors.isEmpty()) {
            throw new IllegalArgumentException("At least one selector is required for " + fieldName);
        }
        this.page = page;
        this.selectors = List.copyOf(selectors);
        this.fieldName = fieldName;
        this.pageName = pageName;
        this.config = AutoHealConfig.getInstance();
        this.index = index;
    }

    public AutoHealElementLocator atIndex(int elementIndex) {
        if (elementIndex < 0) {
            throw new IndexOutOfBoundsException("Element index must not be negative: " + elementIndex);
        }
        return new AutoHealElementLocator(page, selectors, fieldName, pageName, elementIndex);
    }

    public Locator findLocator() {
        lastHealIndex = -1;
        lastHealTimeMs = 0;
        long startedAt = System.currentTimeMillis();

        if (!config.isEnabled()) {
            return indexed(page.locator(selectors.get(0)));
        }

        if (cachedHealedSelector != null && exists(cachedHealedSelector)) {
            lastHealTimeMs = elapsedSince(startedAt);
            return indexed(page.locator(cachedHealedSelector));
        }
        cachedHealedSelector = null;

        String primary = selectors.get(0);
        Locator primaryLocator = page.locator(primary);
        try {
            primaryLocator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED)
                    .setTimeout(config.getWaitTimeout() * 1000.0));
            lastHealTimeMs = elapsedSince(startedAt);
            return indexed(primaryLocator);
        } catch (TimeoutError ignored) {
            // Continue to fallback selectors after the configured primary wait.
        }

        for (int attempt = 1; attempt <= config.getRetryPrimary(); attempt++) {
            if (exists(primary)) {
                lastHealTimeMs = elapsedSince(startedAt);
                return indexed(page.locator(primary));
            }
            page.waitForTimeout(300);
        }

        for (int i = 1; i < selectors.size(); i++) {
            String selector = selectors.get(i);
            if (!exists(selector)) {
                continue;
            }
            lastHealIndex = i;
            lastHealTimeMs = elapsedSince(startedAt);
            cachedHealedSelector = selector;
            logHealSuccess(selector);
            AutoHealMetrics.getInstance().recordHeal(
                    pageName, fieldName, selectorType(selector), lastHealTimeMs);
            HealedLocatorStore.getInstance().setHealedLocator(pageName, fieldName, selector);
            takeScreenshot();
            return indexed(page.locator(selector));
        }

        DomSimilarityMatcher.DomMatchResult match =
                DomSimilarityMatcher.findSimilarElement(page, primary, fieldName, pageName);
        if (match != null) {
            lastHealIndex = selectors.size();
            lastHealTimeMs = elapsedSince(startedAt);
            cachedHealedSelector = match.selector;
            HealedLocatorStore.getInstance().setHealedLocator(pageName, fieldName, match.selector);
            AutoHealMetrics.getInstance().recordHeal(
                    pageName, fieldName, "DOM-Similarity", lastHealTimeMs);
            takeScreenshot();
            return indexed(match.locator);
        }

        lastHealTimeMs = elapsedSince(startedAt);
        AutoHealMetrics.getInstance().recordFailure(pageName, fieldName);
        logHealFailed();
        throw new PlaywrightException("AutoHeal could not find '" + fieldName + "' on "
                + pageName + " using " + selectors.size() + " selector strategies");
    }

    public Locator findLocatorAtIndex(int elementIndex) {
        return atIndex(elementIndex).findLocator();
    }

    public int count() {
        String selector = resolveSelector();
        return page.locator(selector).count();
    }

    public String resolveSelector() {
        findLocator();
        if (index != null) {
            return cachedHealedSelector != null ? cachedHealedSelector : selectors.get(0);
        }
        if (cachedHealedSelector != null) {
            return cachedHealedSelector;
        }
        return selectors.get(0);
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

    public String getPrimarySelector() {
        return selectors.get(0);
    }

    public String getLastSuccessfulSelector() {
        if (lastHealIndex >= 0 && lastHealIndex < selectors.size()) {
            return selectors.get(lastHealIndex);
        }
        return null;
    }

    public int getSelectorCount() {
        return selectors.size();
    }

    private boolean exists(String selector) {
        return page.locator(selector).count() > (index == null ? 0 : index);
    }

    private Locator indexed(Locator locator) {
        return index == null ? locator : locator.nth(index);
    }

    private static long elapsedSince(long start) {
        return System.currentTimeMillis() - start;
    }

    private static String selectorType(String selector) {
        if (selector.startsWith("xpath=")) return "xpath";
        if (selector.startsWith("css=")) return "css";
        return "selector";
    }

    private void logHealSuccess(String selector) {
        System.out.println();
        System.out.println("[AUTO-HEAL SUCCESS]");
        System.out.println("Element        : " + fieldName);
        System.out.println("Primary Selector: " + selectors.get(0));
        System.out.println("Recovered Using: " + selector);
        System.out.println("Page           : " + pageName);
        System.out.println("Execution Time : " + lastHealTimeMs + " ms");

        if (config.isReportEnabled()) {
            ExtentTestManager.log(Status.WARNING,
                    "<span style='color:#E67E22;'>AUTO-HEAL ACTIVATED</span>"
                            + "<br><b>Element:</b> " + fieldName
                            + "<br><b>Original:</b> " + selectors.get(0)
                            + "<br><b>Recovered:</b> " + selector
                            + "<br><b>Page:</b> " + pageName
                            + "<br><b>Time:</b> " + lastHealTimeMs + " ms");
        }
    }

    private void logHealFailed() {
        System.out.println();
        System.out.println("[AUTO-HEAL FAILED]");
        System.out.println("Element         : " + fieldName);
        System.out.println("Primary Selector: " + selectors.get(0));
        System.out.println("Page            : " + pageName);
        System.out.println("Selectors Tried : " + selectors.size());

        if (config.isReportEnabled()) {
            ExtentTestManager.log(Status.FAIL,
                    "<span style='color:#E74C3C;'>AUTO-HEAL FAILED</span>"
                            + "<br><b>Element:</b> " + fieldName
                            + "<br><b>Selector:</b> " + selectors.get(0)
                            + "<br><b>Page:</b> " + pageName
                            + "<br><b>Strategies Tried:</b> " + selectors.size());
        }
    }

    private void takeScreenshot() {
        if (!config.isScreenshotEnabled()) {
            return;
        }
        String safeName = pageName.replaceAll("[^a-zA-Z0-9._-]", "_") + "_"
                + fieldName.replaceAll("[^a-zA-Z0-9._-]", "_") + "_"
                + System.currentTimeMillis() + ".png";
        Path directory = Paths.get(System.getProperty("user.dir"), "auto-heal-screenshots");
        Path screenshot = directory.resolve(safeName);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create auto-heal screenshot directory " + directory, e);
        }
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshot));
        System.out.println("[AUTO-HEAL] Screenshot saved: " + screenshot);
    }
}
