// package testconfig;

// import java.lang.reflect.Method;
// import java.util.HashMap;
// import java.util.Properties;

// import io.appium.java_client.ios.IOSDriver;
// import org.openqa.selenium.remote.RemoteWebElement;
// import org.testng.ITestContext;
// import org.testng.ITestResult;
// import org.testng.annotations.AfterClass;
// import org.testng.annotations.AfterMethod;
// import org.testng.annotations.AfterSuite;
// import org.testng.annotations.BeforeClass;
// import org.testng.annotations.BeforeMethod;
// import org.testng.annotations.BeforeSuite;
// import com.aventstack.extentreports.ExtentReports;
// import com.aventstack.extentreports.ExtentTest;
// import com.epam.healenium.SelfHealingDriver;
// import com.nxg.constants.AutomationTool;
// import com.nxg.constants.TestReportType;
// import com.nxg.core.AllureResultManager;
// import com.nxg.core.CoreAndroidTestManager;
// import com.nxg.core.CoreiOSTestManager;
// import com.nxg.core.IMobileActionManager;
// import com.nxg.dataprovider.CustomCSVDataProvider;
// import pageobject.IOSMobilePageObject;
// import testcomponents.DemoAppAndroidTestComponents;
// import testcomponents.DemoAppIOSTestComponents;

// public class iOSMobileTestManger extends CustomCSVDataProvider {

// 	protected ExtentTest extentTest;
// 	String reporter;
// 	TestReportType configuredReport;
// 	HashMap<String, IMobileActionManager<RemoteWebElement>> testComponents = new HashMap<String, IMobileActionManager<RemoteWebElement>>();
// 	protected DemoAppIOSTestComponents tc;
// 	protected Properties props;

// 	public CoreiOSTestManager tm = new CoreiOSTestManager();

// 	@BeforeSuite(alwaysRun = true)
// 	public void beforeSuite(ITestContext context) {
// 		try {
// 			tm.TestInitialization(context);
// 			context.setAttribute("AutomationTool", AutomationTool.SELENIUM);

// 		} catch (Exception ex) {
// 			System.out.println(ex.getMessage());
// 		}

// 	}

// 	/*
// 	 *
// 	 * Use below method to inject test component(s) so that is available for the
// 	 * test script that extend this class.
// 	 *
// 	 */

// 	@BeforeClass(alwaysRun = true)
// 	public void beforeClass(ITestContext context) {
// 		try {
// 			// this.props = tm.props;
// 			props = ((Properties) context.getAttribute("props"));
// 			tm.setupWebDriver();
// 			testComponents.put("tc", new DemoAppIOSTestComponents(tm.getDriver()));
// 			// Each test script when it extend this class, it will have its own copy of
// 			// below business component.

// 			tc = (DemoAppIOSTestComponents) testComponents.get("tc");
// 		} catch (Exception ex) {
// 			ex.printStackTrace();
// 		}

// 	}

// 	/*
// 	 * Below method responsible to create a test report object. below example
// 	 * explains how extentTest Object is created when test.properties configured to
// 	 * use Extent Reporting Tool.
// 	 *
// 	 */

// 	@BeforeMethod(alwaysRun = true)
// 	public void beforeMethod(Method method, ITestContext context) {
// 		// reporter = tm.props.getProperty("testReporter");
// 		reporter = ((Properties) context.getAttribute("props")).getProperty("testReporter");
// 		configuredReport = TestReportType.valueOf(reporter);
// 		switch (configuredReport) {
// 		case Extent: {
// 			extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager())
// 					.createTest(method.getName());
// 			// extentTest = extentTest;
// 			break;
// 		}
// 		case Allure: {
// 			// To be implemented
// 			break;
// 		}
// 		default: // set as extent report
// 			extentTest = ((ExtentReports) tm.getTestResultManager().getTestResultManager())
// 					.createTest(method.getName());
// 		}

// 		// Inject extentTest Object in each business component.

// 		//testComponents.forEach((key, obj) -> obj.setReportObject(extentTest));

// 	}

// 	/*
// 	 *
// 	 * Below method logs the actual result of the test
// 	 *
// 	 *
// 	 */
// 	@AfterMethod(alwaysRun = true)
// 	public void afterMethod(ITestResult result, ITestContext context) {
// 		result.setAttribute("driver", tm.getDriver());
// 		// result.setAttribute("testManagementConnector",
// 		// tm.getTestManagementToolConnector());
// 		result.setAttribute("testManagementConnector", context.getAttribute("testMangementToolConnector"));
// 		// reporter = tm.props.getProperty("testReporter");
// 		reporter = ((Properties) context.getAttribute("props")).getProperty("testReporter");
// 		configuredReport = TestReportType.valueOf(reporter);

// 		AllureResultManager allureResultManager = new AllureResultManager();
// 		allureResultManager.saveScreenshotPNG((SelfHealingDriver) tc.getDriver());
// 		allureResultManager.saveTextlog(result.getMethod().getMethodName() + " failed screenshot ");

// 		switch (configuredReport) {
// 		case Extent: {
// 			tm.getTestResultManager().LogResult(result, extentTest);
// 			break;
// 		}
// 		case Allure: {
// 			// To be implemented
// 			break;
// 		}
// 		default: // set as extent report
// 			tm.getTestResultManager().LogResult(result, extentTest);

// 		}

// 	}

// 	@AfterClass(alwaysRun = true)
// 	public void afterClass() {
// 		((IOSDriver)tc.getDriver()).terminateApp("com.apple.mobilesafari");
// 		((IOSDriver)tm.getDriver()).terminateApp("com.testgrid.testapp");
// 		tm.getDriver().quit();
// 	}

// 	@AfterSuite(alwaysRun = true)
// 	public void afterSuite() {
// 		tm.teardown();
// 	}

// }
