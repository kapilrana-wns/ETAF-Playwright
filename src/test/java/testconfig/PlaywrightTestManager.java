package testconfig;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import testComponents.Playwright.PlaywrightTestComponents;
import wns.automation.core.AllureResultManager;
import wns.automation.core.playwright.CorePlaywrightTestManager;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.constants.AutomationTool;
import wns.automation.core.constants.TestReportType;
import wns.automation.dataprovider.CustomCSVDataProvider;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
/**
 * ETAF Playwright Test Manager
 */
public class PlaywrightTestManager extends CustomCSVDataProvider {

    protected ExtentTest extentTest;

    String reporter;

    TestReportType configuredReport;

    HashMap<String, IApplicationActionManager> testComponents = new HashMap<>();

    protected PlaywrightTestComponents tc;

    protected Properties props;

    public CorePlaywrightTestManager tm = new CorePlaywrightTestManager();

    @BeforeSuite(alwaysRun = true)
    public void init(ITestContext context) {

        try {

            tm.TestInitialization(context);

            context.setAttribute("AutomationTool", AutomationTool.PLAYWRIGHT);

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
        }
    }
    /**
     * Create Driver & Test Components
     */
    @BeforeClass(alwaysRun = true)
    public void setDriver(ITestContext context) {

        props = (Properties) context.getAttribute("props");
//        tm.setupWebDriver();
//        testComponents.put("tc", new PlaywrightTestComponents(tm.getDriver(), tm.page));
        try {

            tm.setupWebDriver();

            testComponents.put("tc", new PlaywrightTestComponents(tm.getDriver(), tm.page));

            tc = (PlaywrightTestComponents) testComponents.get("tc");
        }
        catch (Exception e) {

            Assert.fail("Playwright initialization failed : " + e.getMessage());
        }
        tc = (PlaywrightTestComponents) testComponents.get("tc");
    }
    /**
     * Create Report Test Object
     */
    @BeforeMethod(alwaysRun = true)
    public void InitTestMethod(
            Method method,
            ITestContext context) {

        reporter = ((Properties) context.getAttribute("props")).getProperty("testReporter");

        configuredReport = TestReportType.valueOf(reporter);

        switch (configuredReport) {

            case Extent:

                extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager()).createTest(method.getName());

                break;

            case Allure:

                break;

            default:

                extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager()).createTest(method.getName());

                break;
        }

        testComponents.forEach((key, component) -> component.setReportObject(extentTest));
    }

    /**
     * Log Actual Test Result
     */
    @AfterMethod(alwaysRun = true)
    public void LogResult(
            ITestResult result,
            ITestContext context) {

        /*
         * Important for Playwright Screenshot Capture
         */
        result.setAttribute("driver", tm.page);

        result.setAttribute("testManagementConnector", tm.getTestManagementToolConnector());

        reporter = ((Properties) context.getAttribute("props")).getProperty("testReporter");

        configuredReport = TestReportType.valueOf(reporter);

        AllureResultManager allureResultManager = new AllureResultManager();

        try {

           // allureResultManager.saveScreenshotPNG(tc.getDriver());
            if (tc != null) {
                allureResultManager.saveScreenshotPNG(
                        tc.getDriver());
            }

            allureResultManager.saveTextlog(result.getMethod().getMethodName() + " screenshot");

        } catch (Exception e) {

            System.out.println("Allure Screenshot Error : " + e.getMessage());
        }

        switch (configuredReport) {

            case Extent:

                //tm.getTestResultManager().LogResult(result, extentTest);
                if(result.getTestContext() != null) {
                    tm.getTestResultManager().LogResult(result, extentTest);
                }

                break;

            case Allure:

                break;

            default:

                tm.getTestResultManager().LogResult(result, extentTest);

                break;
        }
    }

    /**
     * Close Browser
     */
    @AfterClass(alwaysRun = true)
    public void Cleanup() {

        try {

            if (tm.page != null) {
                tm.page.close();
            }

            if (tm.getDriver() != null) {
                tm.getDriver().close();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    /**
     * Flush Reports
     */
    @AfterSuite(alwaysRun = true)
    public void teardown() {
        tm.teardown();
    }
}