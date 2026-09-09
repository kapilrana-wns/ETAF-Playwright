package testscripts.Playwright;

import org.testng.annotations.Test;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import testconfig.PlaywrightTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;

import java.util.Map;

public class OHRMLoginTestPW extends PlaywrightTestManager {

    // @Test(description = "Login to OrangeHRM with valid admin credentials and verify dashboard")
    @Test(description = "Login with valid credentials", groups = {"Regression", "Positive"},
            dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    public void loginWithValidCredentialsTest(Map<String, String> rowData) throws Exception {

        System.out.println(props.getProperty("OrangeHRMUrl"));

        Assert.assertTrue(
                tc.launchApplication(props.getProperty("OrangeHRMUrl")),
                "Application should launch");

        Assert.assertTrue(
                tc.verifyLoginPageDisplayed(),
                "Login page should be displayed");

        Assert.assertTrue(
                tc.enterUserName(rowData.get("UserName")),
                "Username should be entered");

        Assert.assertTrue(
                tc.enterPassword(rowData.get("Password")),
                "Password should be entered");

        Assert.assertTrue(
                tc.clickLoginButton(),
                "Login button should be clicked");

        Assert.assertTrue(
                tc.verifyDashboardDisplayed(),
                "Dashboard should be displayed");

        extentTest.log(Status.PASS,
                "Login completed successfully");
    }
}