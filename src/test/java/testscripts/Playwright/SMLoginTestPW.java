package testscripts.Playwright;

import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.Test;
import testconfig.PlaywrightTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;

import java.util.Map;

@Test(groups = {"Authentication"})
public class SMLoginTestPW extends PlaywrightTestManager{

	@Test(description = "Login with valid credentials", groups = {"Regression", "Positive"},
	dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
	public void testSuccessfulLogin(Map<String, String> rowData) {
		tc.Reporter.log(Status.INFO,"Test Data : UserName- "+rowData.get("UserName"));
		System.out.println(props.getProperty("ApplicationUrl"));
		Assert.assertTrue(tc.Login(props.getProperty("ApplicationUrl"),  rowData.get("UserName"), rowData.get("Password")));
        extentTest.log(Status.PASS, "Login Passed");
        tc.Reporter.log(Status.PASS,"Login Passed");
		tc.LogOff();
	}
}