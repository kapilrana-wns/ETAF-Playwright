package wns.automation.core;

import java.util.Arrays;
import java.nio.file.Paths;
import java.util.Properties;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.testng.ITestResult;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import wns.automation.connectors.Tools.Defect;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.utilities.TestResultListener;
import wns.automation.utilities.TestUtility;
import wns.automation.core.constants.*;

public class ExtentResultManager implements ITestResultManager {

    private final ExtentReports extentReporter;
    private  ExtentTest extentTest;
    private final ExtentSparkReporter extenthtmlReporter;
    private ScreenShotFor requireScreenShot = ScreenShotFor.ScreenShotNotRequired;

    public ExtentReports getExtentReporter() {
        return extentReporter;
    }

    @Override
    public Object getTestResultManager() {
        return extentReporter;
    }

    public ExtentResultManager(String testResultFileName) {
        extentReporter = new ExtentReports();
        extenthtmlReporter = new ExtentSparkReporter(testResultFileName);
        extenthtmlReporter.config().setDocumentTitle("Test Result");
        extenthtmlReporter.config().setReportName("DefaultReport");
        extenthtmlReporter.config().setTheme(Theme.DARK);
        extentReporter.attachReporter(extenthtmlReporter);

    }

    public synchronized void LogResult(ITestResult Result, Object obj) {

        try {
            extentTest = (ExtentTest) obj;
            int testExecutionStatus = Result.getStatus();
//			extentTest = extentReporter.createTest(
//					Result.getMethod().getDescription() + " :" + Result.getMethod().getMethodName());
            Properties props = (Properties) Result.getAttribute("props");
            if (props == null) {
                props = (Properties) Result.getTestContext().getAttribute("props");
            }
            IToolsConnector testManagementToolConnector =
                    (IToolsConnector) Result.getAttribute("testManagementConnector");
            if (testManagementToolConnector == null) {
                testManagementToolConnector = (IToolsConnector) Result.getTestContext()
                        .getAttribute("testManagementToolConnector");
            }
            if (testManagementToolConnector == null) {
                testManagementToolConnector = (IToolsConnector) Result.getTestContext()
                        .getAttribute("testMangementToolConnector");
            }
            if (testManagementToolConnector == null) {
                testManagementToolConnector = TestUtility.getTestManagementToolConnector(props);
            }

            switch (testExecutionStatus) {

                case ITestResult.FAILURE: {
                    extentTest.log(Status.FAIL, MarkupHelper.createLabel(Result.getName(), ExtentColor.RED));
                    extentTest.fail(Result.getThrowable());

                    if (requireScreenShot.equals(ScreenShotFor.ScreenShotforBothFailedAndPassedCases) ||
                            requireScreenShot.equals(ScreenShotFor.ScreenShotOnlyForFailedCases)
                    ) {
                        String screenshotpath=	getScreenShot(Result, props);
                        //extentTest.addScreenCaptureFromPath(screenshotpath);
                        if (screenshotpath != null && !screenshotpath.isEmpty()) {
                            String reportPath = Paths.get(screenshotpath).getFileName().toString();
                            extentTest.addScreenCaptureFromPath(reportPath);
                            extentTest.fail("Screenshot", MediaEntityBuilder.createScreenCaptureFromPath(reportPath).build());
                        }
                    }
                    String defectID="";
                    String testCaseID = "";
                    if(Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true)
                    {
                        Defect defect = new Defect();
                        defect.reporterName= props.getProperty("TestManagementProjectUserName");
                        defect.setAssigneeName(props.getProperty("DefectAssigneeName"));
                        defect.setReporterName(props.getProperty("DefectReportedBy"));

                        defect.setDefectSummary(Result.getName());
                        String testIdentity = Result.getTestClass().getName() + "#"
                                + Result.getMethod().getMethodName();
                        defect.setTestIdentity(testIdentity);
                        defect.setDefectDescription("Test Name :" + Result.getName() +"\nDescription : " + Result.getMethod().getDescription() + " Failed");
                        if (testManagementToolConnector == null) {
                            throw new IllegalStateException(
                                    "Auto defect logging is enabled but no test management connector is available.");
                        }
                        defectID = testManagementToolConnector.createDefect(defect);
                        if (defectID != null && !defectID.isEmpty()) {
                            if ("Jira".equalsIgnoreCase(props.getProperty("TestManagementTool"))) {
                                TestResultListener.recordJiraDefect(testIdentity, defectID);
                            }
                            extentTest.log(Status.INFO, MarkupHelper.createLabel(
                                    "Defect ID :" + defectID, ExtentColor.RED));
                        } else {
                            extentTest.log(Status.WARNING, "Jira defect was not created or returned.");
                        }
                    }

                    if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true) {
                        testCaseID = getTestCaseId(Result);
                        if (!testCaseID.isEmpty()) {
                            testManagementToolConnector.addTestsToCycle("project = "
                                    + TestUtility.getTestManagementProjectKey(props) + " AND Key =" + testCaseID);
                            testManagementToolConnector.updateTestCaseResult(testCaseID, testExecutionStatus,
                                    "Defect ID :" + defectID);
                        } else {
                            System.out.println(
                                    "Skipping test management result update: no test-case ID was provided.");
                        }
                    }

                    if(Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true &&
                            Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true &&
                            defectID != null && !defectID.isEmpty() && !testCaseID.isEmpty())
                    {
                        testManagementToolConnector.linkTestCaseandDefect(defectID,testCaseID);
                    }

                    break;

                }

                case ITestResult.SUCCESS: {
                    extentTest.log(Status.PASS, MarkupHelper.createLabel(Result.getName(), ExtentColor.GREEN));
                    if (requireScreenShot.equals(ScreenShotFor.ScreenShotforBothFailedAndPassedCases) ||
                            requireScreenShot.equals(ScreenShotFor.ScreenShotOnlyForPassedCases)) {
                        String screenshotpath=	getScreenShot(Result, props);
                        if (screenshotpath != null && !screenshotpath.isEmpty()) {
                            String reportPath = Paths.get(screenshotpath).getFileName().toString();
                            extentTest.addScreenCaptureFromPath(reportPath);
                        }
                    }

                    if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true) {
                        String testCaseID = getTestCaseId(Result);
                        if (!testCaseID.isEmpty()) {
                            testManagementToolConnector.addTestsToCycle("project = "
                                    + TestUtility.getTestManagementProjectKey(props) + " AND Key =" + testCaseID);
                            testManagementToolConnector.updateTestCaseResult(testCaseID, testExecutionStatus, "");
                        } else {
                            System.out.println(
                                    "Skipping test management result update: no test-case ID was provided.");
                        }
                    }


                    break;
                }

                case ITestResult.SKIP: {
                    extentTest.log(Status.SKIP, MarkupHelper.createLabel(Result.getName(), ExtentColor.ORANGE));
                    break;

                }

            }


        } catch (Exception ex) {
            System.out.println("ExtentResultManager.LogResult error: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    /**
     * @param Result
     * @param props
     * @throws Exception
     */
    private String getScreenShot(ITestResult Result, Properties props) throws Exception {
        return	TestUtility.getScreenShot(Result, props);

    }

    private String getTestCaseId(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length == 0) {
            return "";
        }
        Object parameter = parameters[parameters.length - 1];
        if (parameter instanceof java.util.Map<?, ?> rowData) {
            for (String key : Arrays.asList(
                    "TestCaseID", "TestCaseId", "testCaseID", "testCaseId", "JiraKey", "IssueKey")) {
                Object value = rowData.get(key);
                if (value != null && !value.toString().isBlank()) {
                    return value.toString().trim();
                }
            }
            return "";
        }
        return parameter == null ? "" : parameter.toString().trim();
    }

    public void takeScreenShotFor(ScreenShotFor screenShotFor) {
        this.requireScreenShot = screenShotFor;

    }

    public synchronized void CloseReport() {
        extentReporter.flush();
    }


}
