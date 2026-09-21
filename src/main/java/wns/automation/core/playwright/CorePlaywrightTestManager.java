package wns.automation.core.playwright;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import lombok.Setter;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.core.CoreTestManager;
import com.microsoft.playwright.Browser;
import wns.automation.core.constants.TestExecutionMode;
import wns.automation.utilities.TestUtility;

import java.util.Arrays;
import java.util.List;

public class CorePlaywrightTestManager extends CoreTestManager {
    @Getter
    @Setter
    protected Playwright driver;
    public static Browser browser;
    public Page page;

    public void setTestManagementToolConnector(IToolsConnector testManagementToolConnector) {
        this.testManagementToolConnector = testManagementToolConnector;
    }

    @Override
    public void InitializeContext(
            wns.automation.core.constants.Browser browser,
            Long duration,
            TestExecutionMode executionMode,
            String remoteURL) {

        try {
            try {

                System.out.println("Before Playwright.create()");
                driver = Playwright.create();
                System.out.println("After Playwright.create()");
            }
            catch (Exception e) {
                e.printStackTrace();throw new RuntimeException("Playwright Browser Launch Failed", e);
            }
            BrowserType browserType;

            switch (browser) {

                case FireFox:
                    browserType = driver.firefox();
                    break;

                default:
                    browserType = driver.chromium();
                    break;
            }

           /* com.microsoft.playwright.Browser browserInstance =
                    browserType.launch(new BrowserType.LaunchOptions().setExecutablePath(java.nio.file.Paths.get(
                            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")).setHeadless(false));

            page = browserInstance.newPage();*/
            String browserName = props.getProperty("browser");

            System.out.println("Selected Browser : " + browserName);

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(Boolean.parseBoolean(props.getProperty("headless", "false")));

            if (browser == wns.automation.core.constants.Browser.Chrome) {
                launchOptions.setChannel("chrome");
            } else if (browser == wns.automation.core.constants.Browser.Edge) {
                launchOptions.setChannel("msedge");
            }

            com.microsoft.playwright.Browser browserInstance =
                    browserType.launch(launchOptions);
            page = browserInstance.newPage();

        }
        catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Playwright Browser Launch Failed", e);
        }
    }
}