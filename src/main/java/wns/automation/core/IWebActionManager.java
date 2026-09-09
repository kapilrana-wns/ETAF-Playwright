package wns.automation.core;
import wns.automation.core.constants.*;


public interface IWebActionManager<T> {
	public void openWebApp(String url);
	//public WebElement Find(By element);
	public void Click(T element);
	public void Input(T element,String text);
	public boolean isDisplayed(T element);
	public boolean selectValueFromDropDown(T element, String text);
//	public File takeScreenShot(ITestResult result);
	void Cleanup();
	void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode, String remoteURL);
	boolean IsDisabled(T element);
	boolean IsEnabled(T element);
	boolean getTextFromElement(T element, String text);

}

