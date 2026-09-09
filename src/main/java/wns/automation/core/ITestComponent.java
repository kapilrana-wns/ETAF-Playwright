package wns.automation.core;

import wns.automation.core.constants.*;

public interface ITestComponent {
	public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode, String remoteURL);
	public <P> void setPageobject(P pageobject);
	public <T> T getDriver();
	//public void setTestResultManager(ITestResultManager ResultManager);
	//public <R extends ITestResultManager> R getTestResultManager();
}
