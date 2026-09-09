package testconfig;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import wns.automation.core.api.RestAssuredAPIManager;
import wns.automation.core.constants.*;

import wns.automation.core.ExtentResultManager;
import wns.automation.core.ITestResultManager;
import wns.automation.core.Reporter;
import wns.automation.dataprovider.CustomCSVDataProvider;
import wns.automation.utilities.*;

import io.restassured.response.Response;

public class APITestManager extends CustomCSVDataProvider{
	private Response APIresponse;
	protected static RestAssuredAPIManager restAssuredManager;
	private static ITestResultManager testResultManager;
	private static Properties props;
	public static String testResultDirectory;
	public static String testResultFile;
	
	String reporter;
	TestReportType configuredReport;
	public ExtentTest extentTest;
	protected Reporter Reporter = new Reporter();

	
	
	@BeforeSuite
	public void init(ITestContext context) {
		try {
			loadProperties();
			restAssuredManager = new RestAssuredAPIManager(props.getProperty("BaseURL"));
			setupTestResult(context);
		} catch (Exception ex) {
			ex.printStackTrace();	
		}

	}

	@BeforeClass
	public void setDriver(ITestContext context) {
		context.setAttribute("props", props);
	}

	@BeforeMethod()
	public void InitTestMethod(Method method)
	{
		reporter = props.getProperty("testReporter");
		configuredReport = TestReportType.valueOf(reporter);
		switch (configuredReport) {
		case Extent: {
			extentTest = ((ExtentReports) testResultManager.
					getTestResultManager()).createTest(method.getName());
			break;
		}
		case Allure: {
			// To be implemented
			break;
		}
		default: // set as extent report
			extentTest = ((ExtentReports) testResultManager.
					getTestResultManager()).createTest(method.getName());
		}
		Reporter.setExtentTest(extentTest);
	}

	@AfterMethod(alwaysRun = true)
	public void LogResult(ITestResult result) {
		reporter = props.getProperty("testReporter");
		configuredReport = TestReportType.valueOf(reporter);
		switch (configuredReport) {
		case Extent: {
			testResultManager.LogResult(result, extentTest);
			break;
		}
		case Allure: {
			// To be implemented
			break;
		}
		default: // set as extent report
			testResultManager.LogResult(result, extentTest);
		}
	}

	@AfterSuite
	public void teardown() {
		testResultManager.CloseReport();
		if (Boolean.parseBoolean(props.getProperty("sendMailUponTetsCompletion")) == true) {
			try {
				TestUtility.sendMail(props);
			} catch (Exception ex) {
			}
		}
	}
	
	private void setupTestResult(ITestContext context) {
		try {
			testResultDirectory = props.getProperty("testResultOutputDirectory");
			testResultFile = props.getProperty("extentResultMainHtmlFileName");
			// Setup TestNGReport
			String testNGResultDirectory = props.getProperty("testNGReportDir");
			String allureResultDirectory = props.getProperty("allureTestResultOutputDirectory");
			File testResultdirectory = new File(String.valueOf(testResultDirectory));
			File testNGDirectory = new File(String.valueOf(testNGResultDirectory));
			File allureResultDir = new File(String.valueOf(allureResultDirectory));
			if (!allureResultDir.exists()) {
				allureResultDir.mkdir();
			}
			if (!testResultdirectory.exists()) {
				testResultdirectory.mkdir();
			}
			FileUtils.cleanDirectory(testResultdirectory);
			FileUtils.cleanDirectory(testNGDirectory);
			FileUtils.cleanDirectory(allureResultDir);
			TestRunner testNGrunner = (TestRunner) context;
			testNGrunner.setOutputDirectory(testNGResultDirectory);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String reporter = props.getProperty("testReporter");
		TestReportType configuredReport = TestReportType.valueOf(reporter);
		switch (configuredReport) {
		case Extent: {
			testResultManager = new ExtentResultManager(testResultDirectory + "\\" + testResultFile);
			break;
		}
		case Allure: {
			//testResultManager = (new AllureResultManager());
			break;
		}
		default:
			testResultManager = (new ExtentResultManager(testResultDirectory + "\\" + testResultFile));
		}
		String screenShotconfig = props.getProperty("takeScreenShotFor");
		ScreenShotFor configuredScreenShotParam = ScreenShotFor.valueOf(screenShotconfig);
		testResultManager.takeScreenShotFor(configuredScreenShotParam);

	}
	
	private void loadProperties() {
		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\APItest.properties";
		props = TestUtility.getTestConfig(propertyFile);
		System.out.println("---------------------------------------------------");
		System.out.println("              Test configuration Details            ");
		System.out.println("----------------------------------------------------");
		for (Object propkey : props.keySet()) {
			System.out.println(propkey.toString() + " :" + props.getProperty(propkey.toString()));
		}
		System.out.println("---------------------------------------------------");
	}
}