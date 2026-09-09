package wns.automation.core.playwright;

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
import wns.automation.core.ITestResultManager;
import wns.automation.utilities.TestResultListener;
import wns.automation.utilities.TestUtility;
import wns.automation.core.constants.ScreenShotFor;

public class ExtentResultManagerPW implements ITestResultManager {

    private static ExtentReports extentReporter;

    private ExtentTest extentTest;

    private ScreenShotFor requireScreenShot =
            ScreenShotFor.ScreenShotNotRequired;

    public ExtentResultManagerPW(String reportFileName) {
        configureExtentObjects(reportFileName);
    }

    public static ExtentReports getExtentReporter() {
        return extentReporter;
    }

    @Override
    public Object getTestResultManager() {
        return extentReporter;
    }

    private void configureExtentObjects(String reportFileName) {

        extentReporter = new ExtentReports();

        ExtentSparkReporter extentHtmlReporter = new ExtentSparkReporter(reportFileName);

        extentHtmlReporter.config().setDocumentTitle("Playwright Test Results");

        extentHtmlReporter.config().setReportName("ETAF Playwright Report");

        extentHtmlReporter.config().setTheme(Theme.DARK);

        extentReporter.attachReporter(extentHtmlReporter);
    }

    @Override
    public void LogResult(ITestResult result, Object obj) {

        try {

            extentTest = (ExtentTest) obj;

            int testExecutionStatus = result.getStatus();

            Properties props = (Properties) result.getTestContext().getAttribute("props");

            IToolsConnector connector = TestUtility.getTestManagementToolConnector(props);

            switch (testExecutionStatus) {

                case ITestResult.SUCCESS:

                    extentTest.log(
                            Status.PASS,
                            MarkupHelper.createLabel(result.getName(), ExtentColor.GREEN));

                    if (requireScreenShot == ScreenShotFor.ScreenShotOnlyForPassedCases || requireScreenShot == ScreenShotFor.ScreenShotforBothFailedAndPassedCases) {

                        String screenshotPath = getScreenShot(result, props);

                        if (screenshotPath != null && !screenshotPath.isEmpty()) {

                            extentTest.addScreenCaptureFromPath(screenshotPath);
                        }
                    }

                    updateTestManagement(result, connector, props, testExecutionStatus, "");

                    break;

                case ITestResult.FAILURE:

                    extentTest.log(Status.FAIL, MarkupHelper.createLabel(result.getName(), ExtentColor.RED));

                    extentTest.fail(result.getThrowable());

                    String screenshotPath = "";

                    if (requireScreenShot == ScreenShotFor.ScreenShotOnlyForFailedCases || requireScreenShot == ScreenShotFor.ScreenShotforBothFailedAndPassedCases) {
                        screenshotPath = getScreenShot(result, props);

                        if (screenshotPath != null && !screenshotPath.isEmpty()) {

                            extentTest.fail("Screenshot", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                        }
                    }

                    String defectId = "";

                    if (Boolean.parseBoolean(props.getProperty("AutoLoggingDefect", "false"))) {

                        Defect defect = new Defect();

                        defect.setAssigneeName(props.getProperty("DefectAssigneeName"));

                        defect.setReporterName(props.getProperty("DefectReportedBy"));

                        defect.setDefectSummary(result.getName());

                        defect.setDefectDescription("Test Name : " + result.getName() + "\nDescription : " + result.getMethod().getDescription());

                        defectId = connector.createDefect(defect);

                        if (defectId != null && !defectId.isEmpty()) {TestResultListener.jiraDefectIDs.add(defectId);
                        }

                        extentTest.info("Defect ID : " + defectId);
                    }

                    updateTestManagement(result, connector, props, testExecutionStatus, defectId);

                    break;

                case ITestResult.SKIP:

                    extentTest.log(Status.SKIP, MarkupHelper.createLabel(result.getName(), ExtentColor.ORANGE));

                    break;
            }

        } catch (Exception ex) {

            System.out.println("ExtentResultManagerPW Error : " + ex.getMessage());

            ex.printStackTrace();
        }
    }

    private void updateTestManagement(
            ITestResult result,
            IToolsConnector connector,
            Properties props,
            int executionStatus,
            String defectId) {

        try {

            if (!Boolean.parseBoolean(props.getProperty("autoTestResultUpdate", "false"))) {

                return;
            }

            java.util.List<Object> passedParameters = Arrays.asList(result.getParameters());

            if (passedParameters.size() > 0) {Object testCaseID = passedParameters.get(passedParameters.size() - 1);

                connector.addTestsToCycle("project = " + props.getProperty("TestManagementProejctKey") + " AND Key = " + testCaseID);

                connector.updateTestCaseResult(testCaseID.toString(), executionStatus, defectId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getScreenShot(
            ITestResult result,
            Properties props)
            throws Exception {

        return TestUtility.getScreenShot(result, props);
    }

    public void takeScreenShotFor(ScreenShotFor screenShotFor) {

        this.requireScreenShot = screenShotFor;
    }

    @Override
    public void CloseReport() {

        if (extentReporter != null) {
            extentReporter.flush();
        }
    }
}