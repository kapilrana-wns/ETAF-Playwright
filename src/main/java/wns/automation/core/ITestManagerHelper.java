package wns.automation.core;

import org.monte.screenrecorder.ScreenRecorder;
import org.testng.ITestContext;
import wns.automation.core.constants.*;

public interface ITestManagerHelper {
	public void TestInitialization(ITestContext context);
	public void initializeTestManagementToolConnector(ITestContext context);
	public void loadProperties();
	public void setupWebDriver();
	public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
			String remoteURL);
	public void setupTestExecutionMode();
	public void setupTestResult();
	public ScreenRecorder getScreenRecorder();
	public void teardown();
	public void emailResult();
	public void closeScreenRecorder();
}