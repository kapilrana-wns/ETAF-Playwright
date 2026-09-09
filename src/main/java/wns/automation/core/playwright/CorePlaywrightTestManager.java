package wns.automation.core.playwright;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import lombok.Setter;
//import org.openqa.selenium.chrome.ChromeOptions;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.core.CoreTestManager;
//import wns.automation.core.constants.Browser;
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

                System.setProperty(
                        "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD",
                        "1");

                System.out.println("Before Playwright.create()");

                driver = Playwright.create();

                System.out.println("After Playwright.create()");

            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(
                        "Playwright Browser Launch Failed",
                        e);
            }
            driver = Playwright.create();

            BrowserType browserType;

            switch (browser) {

                case FireFox:
                    browserType = driver.firefox();
                    break;

                default:
                    browserType = driver.chromium();
                    break;
            }

            com.microsoft.playwright.Browser browserInstance =
                    browserType.launch(new BrowserType.LaunchOptions().setExecutablePath(java.nio.file.Paths.get(
                            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")).setHeadless(false));

            page = browserInstance.newPage();

        }
        catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Playwright Browser Launch Failed", e);
        }
    }
}