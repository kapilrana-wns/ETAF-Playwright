package testscripts.OrangeHRM;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import testconfig.OrangeHRMTestManager;

public class OrangeHRMLoginTest extends OrangeHRMTestManager {

    @Test(description = "Login to OrangeHRM with valid admin credentials and verify dashboard")
    public void loginWithValidCredentialsTest() {
        extentTest.log(Status.INFO, "Validating login page before login");
        Assert.assertTrue(tc.isLoginPageDisplayed(), "Login page should be visible before login");

        extentTest.log(Status.INFO, "Attempting login with Admin/admin123");
        boolean loggedIn = tc.login("Admin", "admin123");
        Assert.assertTrue(loggedIn, "Login with valid credentials should succeed");

        extentTest.log(Status.INFO, "Validating dashboard is displayed after login");
        Assert.assertTrue(tc.isDashboardDisplayed(), "Dashboard should be visible after successful login");
        extentTest.log(Status.PASS, "Login test completed successfully");
    }
}
