package wns.automation.core.playwright;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.core.CoreTestManager;
import wns.automation.core.constants.Browser;
import wns.automation.core.constants.TestExecutionMode;

import java.util.Arrays;

public class CorePlaywrightTestManager extends CoreTestManager {

    protected Playwright driver;
    public static Browser browser;
    private com.microsoft.playwright.Browser launchedBrowser;
    private BrowserType browserType;
    public Page page;

    public Playwright getDriver() {
        return driver;
    }

    public IToolsConnector getTestManagementToolConnector() {
        return testManagementToolConnector;
    }

    public void setTestManagementToolConnector(IToolsConnector testManagementToolConnector) {
        this.testManagementToolConnector = testManagementToolConnector;
    }

    public void setDriver(Playwright driver) {
        this.driver = driver;
    }

    public void InitializeContext(Browser browser, Long waitduration,
                                  TestExecutionMode executionMode, String remoteURL) {
        CorePlaywrightTestManager.browser = browser;
        driver = Playwright.create();
        switch (browser) {
            case FireFox:
                browserType = driver.firefox();
                break;
            case Edge:
                browserType = driver.chromium();
                break;
            case Chrome:
            case CHROMIUM:
            default:
                browserType = driver.chromium();
                break;
        }

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(false);
        if (browser == Browser.Edge) {
            options.setChannel("msedge");
            options.setArgs(Arrays.asList("--start-maximized"));
        } else if (browser == Browser.Chrome) {
            options.setChannel("chrome");
            options.setArgs(Arrays.asList("--start-maximized"));
        } else if (browser == Browser.CHROMIUM) {
            options.setArgs(Arrays.asList("--start-maximized"));
        }
        launchedBrowser = browserType.launch(options);
        page = launchedBrowser.newPage();
        page.setViewportSize(
                Integer.parseInt(AutoHealConfig.getInstance().getProperty("browserWidth", "1380")),
                Integer.parseInt(AutoHealConfig.getInstance().getProperty("browserHeight", "1080")));
    }

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
}
