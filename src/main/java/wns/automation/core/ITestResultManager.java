package wns.automation.core;

import org.testng.ITestResult;

//import com.aventstack.extentreports.ExtentReporter;
import wns.automation.core.constants.ScreenShotFor;

public interface ITestResultManager {

	public void  takeScreenShotFor(ScreenShotFor screenShotFlag);
	public void  LogResult(ITestResult Result, Object obj);
	public void  CloseReport();
	public Object getTestResultManager();
	
}
