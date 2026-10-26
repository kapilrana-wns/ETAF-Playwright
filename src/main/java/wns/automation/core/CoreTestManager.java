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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.monte.screenrecorder.ScreenRecorder.State;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

public  abstract class CoreTestManager  implements ITestManagerHelper{

    public Browser browser;
    public TestExecutionMode testExecutionMode;
    public String remoteURL;
    public String testResultDirectory;
    public String testResultFile;
    public Properties props;
    public long webDriverWaitDuration;
    public ExtentReports extentReporter;
    public ITestResultManager testResultManager;
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
//	public void setTc(WebDriver tc) {
//		this.driver = tc;
//	}

    @Override
    public void TestInitialization(ITestContext context)  {
        try {
            System.out.println("In Test Initialization of CoreTestManager - Loading the properties started");
            loadProperties(); // Load test.properties file
            if (context.getSuite() != null && context.getSuite().getName() != null) {
                props.setProperty("suiteName", context.getSuite().getName());
            }
            context.setAttribute("props", props);
            System.out.println("In Test Initialization of CoreTestManager - Setting up test result");
            setupTestResult(); // setup Test Result Directory & Screenshot requirement
            System.out.println("In Test Initialization of CoreTestManager - Setting up test execution mode");
            setupTestExecutionMode(); // Configure Test Execution mode
            System.out.println("In Test Initialization of CoreTestManager - Test Management Tool Connector Initialization");
            initializeTestManagementToolConnector(context);
            System.out.println("In Test Initialization of CoreTestManager - Setting up Record Video feature");
            boolean parallelSuite = context.getSuite() != null
                    && context.getSuite().getXmlSuite().getParallel() != XmlSuite.ParallelMode.NONE;
            if (Boolean.parseBoolean(props.getProperty("recordVideo")) && !parallelSuite) {
                getScreenRecorder();
                if (screenRecorder != null)
                    screenRecorder.start();
            } else if (parallelSuite && Boolean.parseBoolean(props.getProperty("recordVideo"))) {
                System.out.println("Screen recording is disabled for parallel suites because it captures the shared desktop.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Test framework initialization failed.", e);
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
                    if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true ||
                            Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true)
                    {
                        testManagementToolConnector = JiraConnector.getInstance(props);
                        context.setAttribute("testManagementToolConnector", testManagementToolConnector);

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
                        context.setAttribute("testManagementToolConnector", testManagementToolConnector);

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
    @Override
    public void loadProperties() {


        System.out.println("Loading Test Configuration from test.properties file started...");

        Object userdir = System.getProperty("user.dir");
        String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";

        try{
            props = TestUtility.getTestConfig(propertyFile);
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

    @Override
    public void setupWebDriver() {
        String timeout = props.getProperty("playwrightTimeout",
                props.getProperty("webDriverTimeDuraiton", "10"));
        webDriverWaitDuration = Long.parseLong(timeout);
        if (webDriverWaitDuration <= 0) {
            throw new IllegalArgumentException("playwrightTimeout must be greater than zero.");
        }
        String remoteUrlStr = remoteURL != null ? remoteURL : "";
        InitializeContext(browser, webDriverWaitDuration, testExecutionMode, remoteUrlStr);
    }

    public void attachToSuite(Properties classProperties, ITestResultManager sharedResultManager,
                              IToolsConnector sharedConnector) {
        props = classProperties;
        testResultManager = sharedResultManager;
        testManagementToolConnector = sharedConnector;
        setupTestExecutionMode();
        String timeout = props.getProperty("playwrightTimeout",
                props.getProperty("webDriverTimeDuraiton", "10"));
        webDriverWaitDuration = Long.parseLong(timeout);
        if (webDriverWaitDuration <= 0) {
            throw new IllegalArgumentException("playwrightTimeout must be greater than zero.");
        }
        extentReporter = (ExtentReports) testResultManager.getTestResultManager();
    }

    @Override
    public void setupBrowser() {
        setupWebDriver();
    }

//	@Override
//	public abstract  void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
//			String remoteURL);


    @Override
    public void setupTestExecutionMode() {

        String stringBrowser = props.getProperty("browser");
        browser = Browser.valueOf(stringBrowser);
        remoteURL = props.getProperty("remoteURL");
        String mode = props.getProperty("testExecutionMode");
        testExecutionMode = (mode != null && !mode.isBlank())
                ? TestExecutionMode.valueOf(mode)
                : TestExecutionMode.Local;
        if (testExecutionMode != TestExecutionMode.Local && testExecutionMode != TestExecutionMode.Remote) {
            throw new IllegalArgumentException("Unsupported Playwright execution mode: " + testExecutionMode);
        }

    }
    @Override
    public void setupTestResult() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
            String configuredOutputDirectory = props.getProperty("testResultOutputDirectory");
            testResultFile = props.getProperty("extentResultMainHtmlFileName");
            String runId = formatter.format(new Date());
            Path resultDirectory = Paths.get(configuredOutputDirectory, "Result_" + runId)
                    .toAbsolutePath().normalize();
            Path testNGDirectory = Paths.get(props.getProperty("testNGReportDir"), "Run_" + runId)
                    .toAbsolutePath().normalize();
            Files.createDirectories(resultDirectory);
            Files.createDirectories(testNGDirectory);
            testResultDirectory = resultDirectory.toString();
            props.setProperty("testResultOutputDirectory", testResultDirectory);
            props.setProperty("testNGReportDir", testNGDirectory.toString());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to prepare test result directories.", ex);
        }
        String reporter = props.getProperty("testReporter");
        TestReportType configuredReport = TestReportType.valueOf(reporter);

        switch (configuredReport) {
            case Extent: {
                setTestResultManager(new ExtentResultManager(
                        Paths.get(testResultDirectory, testResultFile).toString()));
                extentReporter = (ExtentReports) getTestResultManager().getTestResultManager();

                break;
            }
            case Allure: {

                //setTestResultManager(new AllureResultManager();
                //setTestResultManager((new AllureResultManager()));

                throw new IllegalArgumentException(
                        "Allure reporting is not configured. Select Extent until Allure support is enabled.");
            }
            case Excel: {
                throw new IllegalArgumentException(
                        "Excel reporting is not configured. Select Extent until Excel reporting is enabled.");
            }
            default:
                throw new IllegalArgumentException("Unsupported test reporter: " + configuredReport);
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
            if (Boolean.parseBoolean(props.getProperty("recordVideo")) && screenRecorder == null) {
                try {
                    File movieFolder = new File(testResultDirectory);

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
                    e.printStackTrace();
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

    }

    @Override
    public void emailResult() {
        if (Boolean.parseBoolean(props.getProperty("sendMailUponTetsCompletion")) == true) {
            try {
                TestUtility.sendMail(props);
            } catch (Exception ex) {
                ex.printStackTrace();

            }

        }
    }

    @Override
    public void closeScreenRecorder() {
        if (Boolean.parseBoolean(props.getProperty("recordVideo")) && screenRecorder != null) {
            try {
                screenRecorder.stop();
                while (screenRecorder.getState() != State.DONE) {
                    System.out.println("AVI file is being prepared....");
                }
                System.out.println("AVI file is ready....");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
