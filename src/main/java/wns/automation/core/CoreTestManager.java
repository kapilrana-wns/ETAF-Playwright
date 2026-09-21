package wns.automation.core;


import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import wns.automation.connectors.Tools.AzureDevOpsConnector;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.connectors.Tools.JiraConnector;

import wns.automation.core.constants.Browser;
import wns.automation.core.constants.ScreenShotFor;
import wns.automation.core.constants.TestExecutionMode;
import wns.automation.core.constants.TestReportType;
import wns.automation.core.constants.TestManagementTools;

//import com.nxg.dataprovider.CustomCSVDataProvider;
import  wns.automation.utilities.TestUtility;

import org.apache.commons.io.FileUtils;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.monte.screenrecorder.ScreenRecorder.State;
import org.testng.ITestContext;

public  abstract class CoreTestManager  implements ITestManagerHelper{

	public static Browser browser;
	public static TestExecutionMode testExecutionMode;
	public static String remoteURL;
	public static String testResultDirectory;
	public static String testResultFile;
	public static Properties props;
	public static long browserWaitDuration;
	public ExtentReports extentReporter;
	public static ITestResultManager testResultManager;
	public ExtentTest extentTest;
	protected ScreenRecorder screenRecorder;
	protected  IToolsConnector testManagementToolConnector;

	   
	public ITestResultManager getTestResultManager() {
		return testResultManager;
	}
//
	public void setTestResultManager(ITestResultManager resultManager) {
		testResultManager = resultManager;

	}

    public IToolsConnector getTestManagementToolConnector() {return testManagementToolConnector;}
//
	@Override
	public void TestInitialization(ITestContext context)  {
		try {
			System.out.println("In Test Initialization of CoreTestManager - Loading the properties started");
			loadProperties(); // Load test.properties file
			context.setAttribute("props", props);
			System.out.println("In Test Initialization of CoreTestManager - Setting up test result");
			setupTestResult(); // setup Test Result Directory & Screenshot requirement
			System.out.println("In Test Initialization of CoreTestManager - Setting up test execution mode");
			setupTestExecutionMode(); // Configure Test Execution mode
			System.out.println("In Test Initialization of CoreTestManager - Test Management Tool Connector Initialization");
			initializeTestManagementToolConnector(context);
			System.out.println("In Test Initialization of CoreTestManager - Setting up Record Video feature");
			if (Boolean.parseBoolean(props.getProperty("recordVideo")) == true) {
				getScreenRecorder();
				if (screenRecorder != null)
					screenRecorder.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception in Test Initialization of CoreTestManager");
			e.printStackTrace();
 
		}
	
	

	}

	@Override
	public void initializeTestManagementToolConnector(ITestContext context) {
		TestManagementTools testManagementTool = (TestManagementTools.valueOf(props.getProperty("TestManagementTool")));
		//IToolsConnector testManagementToolConnector;
		switch (testManagementTool) {
		case Jira: {
			//JiraConnector Jconnector;

			try {
				if (!hasJiraConfiguration()) {
					System.err.println("Jira integration disabled: required configuration is missing.");
					break;
				}
				if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true ||
						Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true)
				{
					testManagementToolConnector = JiraConnector.getInstance(props);
					context.setAttribute("testMangementToolConnector", testManagementToolConnector);

					if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true	)
					{
						// Create Test Cycle
						testManagementToolConnector.createTestCycle();
	 				}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		}

		case AzureDevOPS: {
			//JiraConnector Jconnector;

			try {
				if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true ||
						Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true)
				{
					testManagementToolConnector = AzureDevOpsConnector.getInstance(props);
					context.setAttribute("testMangementToolConnector", testManagementToolConnector);

					if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true	)
					{
						// Create Test Cycle
						testManagementToolConnector.createTestCycle();
	 				}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		}

		default: {
			break;

		}
		}
	}

	private boolean hasJiraConfiguration() {
		String[] keys = {"baseURI", "accessKey", "secretKey", "projectId",
				"versionId", "TestManagementProjectKey"};
		for (String key : keys) {
			String value = props.getProperty(key);
			if (value == null || value.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void loadProperties() {

		
		System.out.println("Loading Test Configuration from test.properties file started...");

		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";
		
		try{
		props = TestUtility.getTestConfig(propertyFile);
		applyConfigurationOverrides(props);
		// System.out.println("---------------------------------------------------");
		// System.out.println("              Test configuration Details            ");
		// System.out.println("----------------------------------------------------");
		// for (Object propkey : props.keySet()) {
		// 	System.out.println(propkey.toString() + " :" + props.getProperty(propkey.toString()));

		// }
		// System.out.println("---------------------------------------------------");
		TestUtility.setProps(props);
		}catch(Exception ex)
		{
			System.out.println("Error in loading test configuration from test.properties file from "+ propertyFile + " : " + ex.getMessage());
		}
	}

	private void applyConfigurationOverrides(Properties properties) {
			String[] keys = {
					"emailSMTPServer", "emailAddress", "emailFrom",
					"DBUserName", "DBPassword", "TestManagementToolApiKey",
					"TestManagementProjectUserName", "accessKey", "secretKey",
					"accountId", "apiToken"
			};
			for (String key : keys) {
				String environmentKey = key.replaceAll("[^A-Za-z0-9]", "_")
						.toUpperCase(Locale.ROOT);
				String override = System.getProperty("etaf." + key);
				if (override == null || override.trim().isEmpty()) {
					override = System.getenv(environmentKey);
				}
				if (override != null && !override.trim().isEmpty()) {
					properties.setProperty(key, override.trim());
				}
			}
		}
	
	@Override
	public void setupBrowser() {
		browserWaitDuration = Long.parseLong(props.getProperty("playwrightTimeout", "10"));
		String remoteUrlStr = remoteURL != null ? remoteURL : "";
		InitializeContext(browser, browserWaitDuration, testExecutionMode, remoteUrlStr);
	}
	
//	@Override
//	public abstract  void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
//			String remoteURL);
	 
	
	@Override
	public void setupTestExecutionMode() {

		String configuredBrowser = props.getProperty("browser", "Chrome").trim();
		switch (configuredBrowser.toLowerCase(java.util.Locale.ROOT)) {
			case "chrome":
				browser = Browser.Chrome;
				break;
			case "firefox":
				browser = Browser.FireFox;
				break;
			case "edge":
				browser = Browser.Edge;
				break;
			case "chromium":
				browser = Browser.CHROMIUM;
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported browser '" + configuredBrowser
								+ "'. Supported values: Chrome, Firefox, Edge, Chromium");
		}

		remoteURL = props.getProperty("remoteURL");
		String mode = props.getProperty("testExecutionMode");
		testExecutionMode = (mode != null && !mode.trim().isEmpty())
				? TestExecutionMode.valueOf(mode.trim())
				: TestExecutionMode.Local;

	}
	public static String reportDirectpath = "";
	@Override
	public void setupTestResult() {
		// setup test result directory
		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");

			testResultDirectory = props.getProperty("testResultOutputDirectory");
			testResultFile = props.getProperty("extentResultMainHtmlFileName");
			testResultDirectory = testResultDirectory+ "Result_"+formater.format(calendar.getTime());
			reportDirectpath = testResultDirectory;
			// Setup TestNGReport
			String testNGResultDirectory = props.getProperty("testNGReportDir");
			String allureResultDirectory = props.getProperty("allureTestResultOutputDirectory");
			File testResultdirectory = new File(String.valueOf(testResultDirectory));
			File testNGDirectory = new File(String.valueOf(testNGResultDirectory));
			File allureResultDir = new File(String.valueOf(allureResultDirectory));
			for (File dir : new File[]{testResultdirectory, testNGDirectory, allureResultDir}) {
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}

			FileUtils.cleanDirectory(testResultdirectory);
			FileUtils.cleanDirectory(testNGDirectory);
			FileUtils.cleanDirectory(allureResultDir);

			// TestRunner testNGrunner = (TestRunner) context;
			// testNGrunner.setOutputDirectory(testNGResultDirectory);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String reporter = props.getProperty("testReporter");
		TestReportType configuredReport = TestReportType.valueOf(reporter);
		
		switch (configuredReport) {
		case Extent: {
			if (this instanceof wns.automation.core.playwright.CorePlaywrightTestManager) {
				setTestResultManager(new wns.automation.core.playwright.ExtentResultManagerPW(
						testResultDirectory + "\\" + testResultFile));
			} else {
				setTestResultManager(new ExtentResultManager(testResultDirectory + "\\" + testResultFile));
			}
			extentReporter = (ExtentReports) getTestResultManager().getTestResultManager();

			break;
		}
		case Allure: {

			//setTestResultManager(new AllureResultManager();
			//setTestResultManager((new AllureResultManager()));
			
			break;
		}
		default:
			setTestResultManager(new ExtentResultManager(testResultDirectory + "\\" + testResultFile));
		}

		try {
			String screenShotconfig = props.getProperty("takeScreenShotFor");
			ScreenShotFor configuredScreenShotParam = ScreenShotFor.valueOf(screenShotconfig);
			getTestResultManager().takeScreenShotFor(configuredScreenShotParam);
		} catch (Exception ex) {
			System.out.println("Screenshot config error: " + ex.getMessage());
		}
	}
	@Override
	public ScreenRecorder getScreenRecorder() {
		
		try {
			if (Boolean.parseBoolean(props.getProperty("recordVideo"))
					&& !GraphicsEnvironment.isHeadless()) {
				try {
					//File movieFolder = new File((props.getProperty("testResultOutputDirectory")));
                    File movieFolder = new File(
                            CoreTestManager.reportDirectpath);
                    if (!movieFolder.exists() && !movieFolder.mkdirs()) {
						throw new IOException("Unable to create recording directory: "
								+ movieFolder.getAbsolutePath());
					}

					screenRecorder = new ScreenRecorder(
							GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
									.getDefaultConfiguration(),
							new Rectangle(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width,
									Toolkit.getDefaultToolkit().getScreenSize().height),
							new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
							new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey,
									ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey,
									ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey,
									Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
							new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey,
									Rational.valueOf(30)),
							null, movieFolder);

				} catch (Exception e) {
					// TODO Auto-generated catch block
						System.out.println("Screen recording could not be initialized: "
								+ e.getMessage());
						screenRecorder = null;
					}
				}
		} catch (Exception ex) {
			ex.printStackTrace();
			screenRecorder = null;
		}
		return screenRecorder;
	}
	@Override
	public void teardown() {
		getTestResultManager().CloseReport();
		closeScreenRecorder();
		emailResult();

		String src = CoreTestManager.testResultDirectory;
		src = src + "\\Result.html";
		String des = System.getProperty("user.dir") + "\\Result.html";
		try {
			 String path;
			File f = new File(".");
			path = f.getAbsolutePath().replace(".", "");

			Thread.sleep(2000);
			Path src1 = Paths.get(src);
			Path dest1 = Paths.get(des);
			Files.copy(src1, dest1, StandardCopyOption.REPLACE_EXISTING);
			Thread.sleep(10000);
			Process process = Runtime.getRuntime().exec("cmd /c allure generate " + path + "allure-results --clean");
			process.waitFor();
			Thread.sleep(2500);
			Process process1 = Runtime.getRuntime().exec("cmd /c allure serve " + path + "allure-results --clean");
			process1.waitFor(30, TimeUnit.SECONDS);
			Thread.sleep(2500);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@Override
	public void emailResult() {
		if (Boolean.parseBoolean(props.getProperty("sendMailUponTetsCompletion")) == true) {
			try {
				TestUtility.sendMail(props);
			} catch (Exception ex) {
				System.err.println("Email notification unavailable: " + ex.getMessage());
			}

		}
	}

	@Override
	public void closeScreenRecorder() {
		if (Boolean.parseBoolean(props.getProperty("recordVideo"))
				&& screenRecorder != null) {
			try {
				screenRecorder.stop();
				while (screenRecorder.getState() != State.DONE) {
					System.out.println("AVI file is being prepared....");
				}
				System.out.println("AVI file is ready....");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Screen recording could not be finalized: "
						+ e.getMessage());
			}
		}
	}

	
	

}
