package testscripts.OrangeHRM;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import testconfig.OrangeHRMTestManager;

public class OrangeHRMAutoHealDemoTest extends OrangeHRMTestManager {

    @Test(description = "DEMO: Auto-heal resolves intentionally wrong locators and logs in successfully")
    public void autoHealInActionTest() {
        extentTest.log(Status.INFO, "============================================");
        extentTest.log(Status.INFO, "        AUTO-HEAL DEMONSTRATION");
        extentTest.log(Status.INFO, "============================================");
        extentTest.log(Status.INFO, "Using AutoHealDemoPage with WRONG locators:");
        extentTest.log(Status.INFO, "  textboxUsername -> @FindBy(id='username') -- should be By.name");
        extentTest.log(Status.INFO, "  textboxPassword -> @FindBy(id='password') -- should be By.name");
        extentTest.log(Status.INFO, "Auto-heal will generate fallback locators and find the elements");

        boolean loginSuccess = tc.loginWithAutoHealDemo("Admin", "admin123");
        Assert.assertTrue(loginSuccess,
                "Login should succeed via auto-healed locators");

        boolean dashboardLoaded = tc.isDashboardDisplayed();
        Assert.assertTrue(dashboardLoaded,
                "Dashboard should be visible after healed login");

        extentTest.log(Status.PASS, "AUTO-HEAL DEMO PASSED: Wrong locators were successfully healed");
        extentTest.log(Status.INFO, "Check console for 'AUTO-HEAL STATISTICS' at end of run");
        extentTest.log(Status.INFO, "Expected: Total Heals > 0, Failed Heals = 0");
    }
}
