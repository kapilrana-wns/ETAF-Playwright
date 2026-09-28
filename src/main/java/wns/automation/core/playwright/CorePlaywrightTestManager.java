package wns.automation.core.playwright;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.openqa.selenium.chrome.ChromeOptions;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.core.CoreTestManager;
import wns.automation.core.constants.Browser;
import wns.automation.core.constants.TestExecutionMode;
import wns.automation.utilities.TestUtility;

import java.util.Arrays;

public class CorePlaywrightTestManager extends CoreTestManager {
    protected Playwright driver;
    public static Browser browser;
    private BrowserType browserType;
    public Page page;

    public Playwright getDriver() {
        return this.driver;
    }

    public IToolsConnector getTestManagementToolConnector() {
        return this.testManagementToolConnector;
    }

    public void setTestManagementToolConnector(IToolsConnector testManagementToolConnector) {
        this.testManagementToolConnector = testManagementToolConnector;
    }

    public void setDriver(Playwright tc) {
        this.driver = tc;
    }

    public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode, String remoteURL) {
        this.driver = Playwright.create();
        switch (browser) {
            case FireFox:
                this.browserType = this.driver.firefox();
                break;
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                TestUtility.setupBrowserCapability(chromeOptions);
                this.browserType = this.driver.chromium();
                this.browserType.launch((new BrowserType.LaunchOptions()).setArgs(Arrays.asList("--start-maximized")));
        }

        this.page = this.browserType.launch((new BrowserType.LaunchOptions()).setChannel("chrome").setHeadless(false).setSlowMo((double)2.0F)).newPage();
    }
}