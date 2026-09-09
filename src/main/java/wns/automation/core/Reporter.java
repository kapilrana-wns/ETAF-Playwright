package wns.automation.core;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reporter {
	private ExtentTest extentTest;
	

	public ExtentTest getExtentTest() {
		return extentTest;
	}


	public void setExtentTest(ExtentTest extentTest) {
		this.extentTest = extentTest;
	}


	public  void   log(Status status,String message)
	{
		Logger logger = LogManager.getLogger();
		logger.info(message);
		Allure.step(message);
		if(extentTest != null)
			extentTest.log(status,message);
 	}
	
}
