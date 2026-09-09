package testconfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import wns.automation.core.constants.*;
import wns.automation.core.AllureResultManager;
import wns.automation.core.CoreSeleniumTestManager;
import wns.automation.core.IApplicationActionManager;
import wns.automation.dataprovider.CustomCSVDataProvider;
import testComponents.SkillMatrix.SeleniumTestComponents;

public class SeleniumTestManager /*extends CustomExcelDataProvider { */  extends CustomCSVDataProvider  {

    protected ExtentTest extentTest;
    public static ExtentTest extentTest1;
    String reporter;
    TestReportType configuredReport;
    HashMap<String, IApplicationActionManager> testComponents = new HashMap<String, IApplicationActionManager>();
    //protected GoogleSearchComponent tc;
    protected SeleniumTestComponents tc;
    protected Properties props;

    public CoreSeleniumTestManager tm   =  new CoreSeleniumTestManager();

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext context) {
        try {
            System.out.println("In Before Suite of SeleniumTestManager - Test Initialization");
            tm.TestInitialization(context);
            context.setAttribute("AutomationTool", AutomationTool.SELENIUM);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    /*
     *
     * Use below method to inject test component(s) so that is available for the
     * test script that extend this class.
     *
     */
    @BeforeClass(alwaysRun = true)
    public void beforeClass(ITestContext context) {
        try {
            System.out.println("In Before Suite of SeleniumTestManager - Test Initialization");
            props = ((Properties) context.getAttribute("props"));
            tm.setupWebDriver();
            testComponents.put("tc", new SeleniumTestComponents(tm.getDriver()));
            tc = (SeleniumTestComponents) testComponents.get("tc");
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /*
     * Below method responsible to create a test report object. below example
     * explains how extentTest Object is created when test.properties configured to
     * use Extent Reporting Tool.
     *
     */

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method,ITestContext context) {
        //reporter = tm.props.getProperty("testReporter");
        reporter =  ((Properties) context.getAttribute("props")).getProperty("testReporter");
        configuredReport = TestReportType.valueOf(reporter);
        switch (configuredReport) {
            case Extent: {
                extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager())
                        .createTest(method.getName());
                // extentTest = extentTest;
                break;
            }
            case Allure: {
                // To be implemented
                break;
            }
            default: // set as extent report
                extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager())
                        .createTest(method.getName());
        }

        // Inject extentTest Object in each business component.

        testComponents.forEach((key, obj) -> obj.setReportObject(extentTest));

    }

    /*
     *
     * Below method logs the actual result of the test
     *
     *
     */
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result, ITestContext context) {

        if (tm.getDriver() != null) {  // Add null check
            result.setAttribute("driver", tm.getDriver());
        }
        result.setAttribute("driver", tm.getDriver());

        if(tm.getTestManagementToolConnector()!=null){
            result.setAttribute("testManagementConnector", tm.getTestManagementToolConnector());
        }
        //result.setAttribute("testManagementConnector", ((Properties) context.getAttribute("props")).getProperty("TestManagementTool"));
        //reporter = tm.props.getProperty("testReporter");
        reporter =  ((Properties) context.getAttribute("props")).getProperty("testReporter");
        configuredReport = TestReportType.valueOf(reporter);

        AllureResultManager allureResultManager = new AllureResultManager();
        allureResultManager.saveScreenshotPNG((WebDriver) tc.getDriver());
        allureResultManager.saveTextlog(result.getMethod().getMethodName() + " failed screenshot ");

        switch (configuredReport) {
            case Extent: {
                tm.getTestResultManager().LogResult(result, extentTest);
                break;
            }
            case Allure: {
                // To be implemented
                break;
            }
            default: // set as extent report
                tm.getTestResultManager().LogResult(result, extentTest);

        }

    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        tm.getDriver().quit();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        tm.teardown();
    }

}
