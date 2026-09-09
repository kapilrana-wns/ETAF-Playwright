package wns.automation.core;

import java.nio.file.Path;
import java.util.Arrays;
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

    private static ExtentReports extentReporter;
    private static ExtentSparkReporter extenthtmlReporter = new ExtentSparkReporter("Spark.html");
    //private static ExtentHtmlReporter extenthtmlReporter;
    private ExtentTest extentTest;
    private ScreenShotFor requireScreenShot = ScreenShotFor.ScreenShotNotRequired;

    public static ExtentReports getExtentReporter() {
        return extentReporter;
    }

    @Override
    public Object getTestResultManager() {
        return extentReporter;
    }

    public static void setExtentReporter(ExtentReports extentReporter) {
        ExtentResultManager.extentReporter = extentReporter;
    }

    public ExtentResultManager(String testResultFileName) {
        configureExtentObjects(testResultFileName);
    }

    private void configureExtentObjects(String testResultFileName) {
        extentReporter = new ExtentReports();
        extenthtmlReporter = new ExtentSparkReporter(testResultFileName);
        //extenthtmlReporter = new ExtentHtmlReporter(testResultFileName);
        //	extenthtmlReporter.config().setChartVisibilityOnOpen(true);
        extenthtmlReporter.config().setDocumentTitle("Test Result");
        extenthtmlReporter.config().setReportName("DefaultReport");
        //	extenthtmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
        extenthtmlReporter.config().setTheme(Theme.DARK);
        extentReporter.attachReporter(extenthtmlReporter);

    }

    public void LogResult(ITestResult Result, Object obj) {

        try {
            extentTest = (ExtentTest) obj;
            int testExecutionStatus = Result.getStatus();
//			extentTest = extentReporter.createTest(
//					Result.getMethod().getDescription() + " :" + Result.getMethod().getMethodName());
            Properties props = (Properties) Result.getTestContext().getAttribute("props");
            //IToolsConnector testManagementToolConnector = (IToolsConnector) Result.getAttribute("testManagementConnector");
            IToolsConnector testManagementToolConnector =
                    TestUtility.getTestManagementToolConnector(props);

            Path destination;

            switch (testExecutionStatus) {

                case ITestResult.FAILURE: {
                    extentTest.log(Status.FAIL, MarkupHelper.createLabel(Result.getName(), ExtentColor.RED));
                    extentTest.fail(Result.getThrowable());

                    if (requireScreenShot.equals(ScreenShotFor.ScreenShotforBothFailedAndPassedCases) ||
                            requireScreenShot.equals(ScreenShotFor.ScreenShotOnlyForFailedCases)
                    ) {
                        String screenshotpath = getScreenShot(Result, props);
                        //extentTest.addScreenCaptureFromPath(screenshotpath);
                        if (screenshotpath != null && !screenshotpath.isEmpty()) {
                            extentTest.addScreenCaptureFromPath(screenshotpath);
                        }
                        extentTest.fail("Screenshot", MediaEntityBuilder.createScreenCaptureFromPath(screenshotpath).build());
                    }
                    String defectID = "";
                    String testCaseID = "";
                    if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true) {
                        Defect defect = new Defect();
                        defect.reporterName = props.getProperty("TestManagementProjectUserName");
                        defect.setAssigneeName(props.getProperty("DefectAssigneeName"));
                        defect.setReporterName(props.getProperty("DefectReportedBy"));

                        //defect.setDefectSummary("Test Name :" + Result.getName()  + " Failed");
                        defect.setDefectSummary(Result.getName());

                        defect.setDefectDescription("Test Name :" + Result.getName() + "\nDescription : " + Result.getMethod().getDescription() + " Failed");
                        defectID = testManagementToolConnector.createDefect(defect);
                        if (defectID != null && !defectID.isEmpty()) {
                            TestResultListener.jiraDefectIDs.add(defectID);
                        }
                        extentTest.log(Status.INFO, MarkupHelper.createLabel("Defect ID :" + defectID, ExtentColor.RED));
                    }

                    if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true) {

                        java.util.List<Object> passedParameters = Arrays.asList(Result.getParameters());
                        if (passedParameters.size() > 0) {
                            Object param = passedParameters.get(passedParameters.size() - 1);
                            System.out.println(testCaseID.toString());
                            testCaseID = param.toString();
                            testManagementToolConnector.addTestsToCycle("project = "
                                    + props.getProperty("TestManagementProejctKey") + " AND Key =" + param);
                            testManagementToolConnector.updateTestCaseResult(testCaseID.toString(), testExecutionStatus,
                                    "Defect ID :" + defectID);
                        }
                    }

                    if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect")) == true &&
                            Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true) {
                        testManagementToolConnector.linkTestCaseandDefect(defectID, testCaseID);
                    }

                    break;

                }

                case ITestResult.SUCCESS: {
                    extentTest.log(Status.PASS, MarkupHelper.createLabel(Result.getName(), ExtentColor.GREEN));
                    if (requireScreenShot.equals(ScreenShotFor.ScreenShotforBothFailedAndPassedCases) ||
                            requireScreenShot.equals(ScreenShotFor.ScreenShotOnlyForPassedCases)) {
                        String screenshotpath = getScreenShot(Result, props);
                        if (screenshotpath != null && !screenshotpath.isEmpty()) {
                            extentTest.addScreenCaptureFromPath(screenshotpath);
                            extentTest.fail(
                                    "Screenshot",
                                    MediaEntityBuilder
                                            .createScreenCaptureFromPath(screenshotpath)
                                            .build());
                        }
                    }

                    if (Boolean.parseBoolean(props.getProperty("autoTestResultUpdate")) == true) {
                        java.util.List<Object> passedParameters = Arrays.asList(Result.getParameters());
                        if (passedParameters.size() > 0) {

                            Object testCaseID = passedParameters.get(passedParameters.size() - 1);
                            System.out.println(testCaseID.toString());
                            testManagementToolConnector.addTestsToCycle("project = " + props.getProperty("TestManagementProejctKey") + " AND Key =" + testCaseID);
                            testManagementToolConnector.updateTestCaseResult(testCaseID.toString(), testExecutionStatus, "");
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
        return TestUtility.getScreenShot(Result, props);

    }

    public void takeScreenShotFor(ScreenShotFor screenShotFor) {
        this.requireScreenShot = screenShotFor;

    }

    public void CloseReport() {
        extentReporter.flush();
    }


}
