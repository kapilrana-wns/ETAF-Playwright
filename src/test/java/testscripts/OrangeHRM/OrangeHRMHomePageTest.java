package testscripts.OrangeHRM;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import testconfig.OrangeHRMTestManager;

public class OrangeHRMHomePageTest extends OrangeHRMTestManager {

    @Test(description = "Login, verify side menu on dashboard, and logout")
    public void homePageNavigationAndLogoutTest() {
        extentTest.log(Status.INFO, "Validating login page before login");
        Assert.assertTrue(tc.isLoginPageDisplayed(), "Login page should be visible before login");

        extentTest.log(Status.INFO, "Logging in with Admin/admin123");
        Assert.assertTrue(tc.login("Admin", "admin123"), "Login should succeed");

        extentTest.log(Status.INFO, "Validating dashboard is displayed");
        Assert.assertTrue(tc.isDashboardDisplayed(), "Dashboard should be visible");

        extentTest.log(Status.INFO, "Validating side menu items (Admin, PIM, Leave)");
        Assert.assertTrue(tc.isSideMenuDisplayed(), "Side menu should display Admin, PIM, and Leave options");

        extentTest.log(Status.INFO, "Logging out from application");
        Assert.assertTrue(tc.logout(), "Logout should return to login page");
        extentTest.log(Status.PASS, "Home page test - navigation and logout completed successfully");
    }
}
