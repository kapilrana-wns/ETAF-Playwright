package wns.automation.core.playwright;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.SelectOption;
import wns.automation.core.IWebActionManager;
import wns.automation.core.Reporter;
import wns.automation.core.constants.Browser;
import wns.automation.core.constants.TestExecutionMode;

public class PlaywrightWebActionManager implements IWebActionManager<Locator> {

    private Playwright driver;
    private com.microsoft.playwright.Browser launchedBrowser;
    public Page page;
    public Reporter Reporter;

    public PlaywrightWebActionManager() {
        Reporter = new Reporter();
    }

    @Override
    public void InitializeContext(Browser browser, Long waitduration,
                                  TestExecutionMode executionMode, String remoteURL) {
        driver = Playwright.create();
        BrowserType browserType;
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(false);
        switch (browser) {
            case FireFox:
                browserType = driver.firefox();
                break;
            case Edge:
                browserType = driver.chromium();
                options.setChannel("msedge");
                options.setArgs(java.util.Collections.singletonList("--start-maximized"));
                break;
            case Chrome:
                browserType = driver.chromium();
                options.setChannel("chrome");
                options.setArgs(java.util.Collections.singletonList("--start-maximized"));
                break;
            case CHROMIUM:
            default:
                browserType = driver.chromium();
                options.setArgs(java.util.Collections.singletonList("--start-maximized"));
                break;
        }

        launchedBrowser = browserType.launch(options);
        page = launchedBrowser.newPage();
        page.setViewportSize(
                Integer.parseInt(AutoHealConfig.getInstance().getProperty("browserWidth", "1380")),
                Integer.parseInt(AutoHealConfig.getInstance().getProperty("browserHeight", "1080")));
    }

    public void setPlaywrightContext(Playwright driver, Page page) {
        this.driver = driver;
        this.page = page;
        this.launchedBrowser = page.context().browser();
    }

    public Page getDriver() {
        return page;
    }

    @Override
    public void openWebApp(String url) {
        page.navigate(url);
        page.waitForLoadState();
    }

    @Override
    public void Click(Locator element) {
        element.click();
    }

    @Override
    public void Input(Locator element, String text) {
        element.fill(text);
    }

    @Override
    public boolean isDisplayed(Locator element) {
        return element.isVisible();
    }

    @Override
    public boolean selectValueFromDropDown(Locator element, String text) {
        element.selectOption(new SelectOption().setLabel(text));
        return true;
    }

    @Override
    public void Cleanup() {
        if (launchedBrowser != null) {
            launchedBrowser.close();
            launchedBrowser = null;
            page = null;
        }
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }

    @Override
    public boolean IsDisabled(Locator element) {
        return !element.isEnabled();
    }

    @Override
    public boolean IsEnabled(Locator element) {
        return element.isEnabled();
    }

    @Override
    public boolean getTextFromElement(Locator element, String text) {
        return java.util.Objects.equals(text, element.textContent());
    }
}
