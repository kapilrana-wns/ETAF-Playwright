package testComponents.OrangeHRM;

import org.openqa.selenium.WebDriver;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import wns.automation.core.Reporter;
import wns.automation.core.selenium.SeleniumWebActionManager;
import wns.automation.core.ExtentTestManager;
import wns.automation.utilities.TestUtility;
import pageobject.OrangeHRM.LoginPage;
import pageobject.OrangeHRM.DashboardPage;
import pageobject.OrangeHRM.AutoHealDemoPage;

public class OrangeHRMTestComponents extends SeleniumWebActionManager {

    public LoginPage loginPage;
    public DashboardPage dashboardPage;
    public AutoHealDemoPage autoHealDemoPage;

    public OrangeHRMTestComponents(WebDriver driver) {
        super.setWebDriver(driver);
        this.loginPage = new LoginPage(driver);
        this.dashboardPage = new DashboardPage(driver);
        this.autoHealDemoPage = new AutoHealDemoPage(driver);
    }

    public void setReportObject(ExtentTest extentTest) {
        Reporter.setExtentTest(extentTest);
        ExtentTestManager.setExtentTest(extentTest);
    }

    public boolean isLoginPageDisplayed() {
        try {
            boolean title = isDisplayed(loginPage.textLoginTitle);
            boolean branding = isDisplayed(loginPage.imgBranding);
            boolean username = isDisplayed(loginPage.textboxUsername);
            Reporter.log(Status.INFO, "Login page loaded: title=" + title
                    + ", branding=" + branding + ", username=" + username);
            return title && branding && username;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "Login page validation failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            Reporter.log(Status.INFO, "Entering username");
            Input(loginPage.textboxUsername, username);
            Reporter.log(Status.INFO, "Entering password");
            Input(loginPage.textboxPassword, password);
            Reporter.log(Status.INFO, "Clicking login button");
            Click(loginPage.btnLogin);
            TestUtility.waitUntilVisible(dashboardPage.headingDashboard, 10, getDriver());
            Reporter.log(Status.INFO, "Login successful, dashboard loaded");
            return true;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "Login failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean isDashboardDisplayed() {
        try {
            boolean heading = isDisplayed(dashboardPage.headingDashboard);
            boolean profile = isDisplayed(dashboardPage.textProfileName);
            Reporter.log(Status.INFO, "Dashboard loaded: heading=" + heading
                    + ", profile=" + profile);
            return heading && profile;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "Dashboard validation failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean isSideMenuDisplayed() {
        try {
            boolean admin = isDisplayed(dashboardPage.menuAdmin);
            boolean pim = isDisplayed(dashboardPage.menuPIM);
            boolean leave = isDisplayed(dashboardPage.menuLeave);
            Reporter.log(Status.INFO, "Side menu: admin=" + admin
                    + ", pim=" + pim + ", leave=" + leave);
            return admin && pim && leave;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "Side menu validation failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean logout() {
        try {
            Reporter.log(Status.INFO, "Opening user dropdown");
            Click(dashboardPage.textProfileName);
            Thread.sleep(500);
            Reporter.log(Status.INFO, "Clicking logout");
            Click(dashboardPage.linkLogout);
            Thread.sleep(1000);
            boolean loggedOut = isDisplayed(loginPage.textLoginTitle);
            Reporter.log(Status.INFO, "Logged out: " + loggedOut);
            return loggedOut;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "Logout failed: " + ex.getMessage());
            return false;
        }
    }

    public boolean loginWithAutoHealDemo(String username, String password) {
        try {
            Reporter.log(Status.INFO, "DEMO: Using page object with intentionally WRONG locators");
            Reporter.log(Status.INFO, "DEMO: Username field uses @FindBy(id='username') - auto-heal will find via By.name");
            Input(autoHealDemoPage.textboxUsername, username);
            Reporter.log(Status.INFO, "DEMO: Password field uses @FindBy(id='password') - auto-heal will find via By.name");
            Input(autoHealDemoPage.textboxPassword, password);
            Reporter.log(Status.INFO, "DEMO: Clicking login button (correct locator)");
            Click(autoHealDemoPage.btnLogin);
            TestUtility.waitUntilVisible(dashboardPage.headingDashboard, 10, getDriver());
            Reporter.log(Status.INFO, "DEMO: Login succeeded via auto-healed locators");
            return true;
        } catch (Exception ex) {
            Reporter.log(Status.FAIL, "DEMO: Auto-healed login failed: " + ex.getMessage());
            return false;
        }
    }
}
