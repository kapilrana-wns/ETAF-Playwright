package testconfig;

import java.lang.reflect.Method;
import java.util.Properties;

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
import com.aventstack.extentreports.Status;

import testComponents.OrangeHRM.OrangeHRMTestComponents;
import wns.automation.core.AllureResultManager;
import wns.automation.core.CoreSeleniumTestManager;
import wns.automation.core.constants.AutomationTool;

public class OrangeHRMTestManager {

    protected ExtentTest extentTest;
    protected OrangeHRMTestComponents tc;
    protected Properties props;

    public CoreSeleniumTestManager tm = new CoreSeleniumTestManager();

    private static boolean initialized = false;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass(ITestContext context) {
        try {
            if (!initialized) {
                tm.TestInitialization(context);
                initialized = true;
            }
            context.setAttribute("AutomationTool", AutomationTool.SELENIUM);
            props = (Properties) context.getAttribute("props");
            if (props == null) {
                System.out.println("props is null, reloading...");
                tm.TestInitialization(context);
                props = (Properties) context.getAttribute("props");
            }
            tm.setupWebDriver();
            tc = new OrangeHRMTestComponents(tm.getDriver());

            String orangeHRMUrl = props.getProperty("OrangeHRMUrl");
            if (orangeHRMUrl == null || orangeHRMUrl.isEmpty()) {
                orangeHRMUrl = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";
            }
            tc.openWebApp(orangeHRMUrl);
        } catch (Exception ex) {
            System.out.println("beforeClass error: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        try {
            Object reportManager = tm.getTestResultManager().getTestResultManager();
            if (reportManager instanceof ExtentReports) {
                extentTest = ((ExtentReports) reportManager).createTest(method.getName());
                if (tc != null) {
                    tc.setReportObject(extentTest);
                }
            }
        } catch (Exception ex) {
            System.out.println("beforeMethod error: " + ex.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        if (tm.getDriver() != null) {
            result.setAttribute("driver", tm.getDriver());
        }
        if (tm.getTestManagementToolConnector() != null) {
            result.setAttribute("testManagementConnector", tm.getTestManagementToolConnector());
        }

        try {
            AllureResultManager allureResultManager = new AllureResultManager();
            if (tm.getDriver() != null) {
                allureResultManager.saveScreenshotPNG(tm.getDriver());
            }
            allureResultManager.saveTextlog(result.getMethod().getMethodName() + " - " + result.getStatus());
        } catch (Exception ex) {
            System.out.println("Allure logging skipped: " + ex.getMessage());
        }

        if (tm.getTestResultManager() != null) {
            if (extentTest == null) {
                try {
                    Object reportManager = tm.getTestResultManager().getTestResultManager();
                    if (reportManager instanceof ExtentReports) {
                        extentTest = ((ExtentReports) reportManager).createTest(result.getMethod().getMethodName());
                    }
                } catch (Exception ex) {
                    System.out.println("Could not create fallback extentTest: " + ex.getMessage());
                }
            }
            if (extentTest != null) {
                // Set status directly so even if LogResult throws, the report is correct
                switch (result.getStatus()) {
                    case ITestResult.FAILURE:
                        extentTest.log(Status.FAIL, "Test Failed: " + result.getThrowable());
                        break;
                    case ITestResult.SUCCESS:
                        extentTest.log(Status.PASS, "Test Passed");
                        break;
                    case ITestResult.SKIP:
                        extentTest.log(Status.SKIP, "Test Skipped");
                        break;
                }
                tm.getTestResultManager().LogResult(result, extentTest);
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        try {
            if (tm.getDriver() != null) {
                tm.getDriver().quit();
            }
        } catch (Exception ex) {
            System.out.println("afterClass error: " + ex.getMessage());
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        tm.teardown();
    }
}
