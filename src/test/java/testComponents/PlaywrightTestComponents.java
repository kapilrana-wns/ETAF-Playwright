package testComponents;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;
import pageobject.PlaywrightPageObject;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.playwright.AutoHealBy;
import wns.automation.core.playwright.AutoHealPageFactory;
import wns.automation.core.playwright.PlaywrightWebActionManager;

public class PlaywrightTestComponents extends PlaywrightWebActionManager implements  IApplicationActionManager {

    // Every page object should have following variables and methods included

    @AutoHealBy(value = "input[name='username']", alternatives = {
            "input[placeholder='Username']", "input[autocomplete='username']"
    })
    private Locator orangeHrmUsername;

    @AutoHealBy(value = "input[name='password']", alternatives = {
            "input[placeholder='Password']", "input[autocomplete='current-password']"
    })
    private Locator orangeHrmPassword;

    @AutoHealBy(value = "button[type='submit']", alternatives = {
            "button.oxd-button--main", "text=Login"
    })
    private Locator orangeHrmLoginButton;

    @AutoHealBy(value = "h6.oxd-topbar-header-breadcrumb-module", alternatives = {
            "h6:has-text('Dashboard')", "text=Dashboard"
    })
    private Locator orangeHrmDashboardHeading;

    public PlaywrightPageObject pageobject;
    public ExtentTest extentTest;

    public PlaywrightTestComponents(Playwright driver, Page page)
    {
        super.setPlaywrightContext(driver, page);
        AutoHealPageFactory.initElements(super.page, this);
        this.pageobject = new PlaywrightPageObject(super.page);
    }

    @Override
    public void setReportObject(ExtentTest extentTest)
    {
        this.extentTest = extentTest;
        Reporter.setExtentTest(extentTest);
    }

    public boolean launchApplication(String url) {
        openWebApp(url);
        return true;
    }

    public boolean verifyLoginPageDisplayed() {
        orangeHrmUsername
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        orangeHrmPassword
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        return true;
    }

    public boolean enterUserName(String username) {
        orangeHrmUsername.fill(username);
        return true;
    }

    public boolean enterPassword(String password) {
        orangeHrmPassword.fill(password);
        return true;
    }

    public boolean clickLoginButton() {
        orangeHrmLoginButton.click();
        return true;
    }

    public boolean verifyDashboardDisplayed() {
        orangeHrmDashboardHeading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        return "Dashboard".equals(orangeHrmDashboardHeading.textContent().trim());
    }

    // Default variable and method block ends here
    // create login method
    public boolean Login(String url, String UserName, String Password) {
        System.out.println(url);
        System.out.println("From Login method");
        openWebApp(url);
        extentTest.log(com.aventstack.extentreports.Status.INFO,"Application launched");
        pageobject.enterUsername(UserName);
        extentTest.log(com.aventstack.extentreports.Status.INFO,"Username input is provided");
        pageobject.enterPassword(Password);
        extentTest.log(com.aventstack.extentreports.Status.INFO,"Password input is provided");
        pageobject.Submit();
        extentTest.log(com.aventstack.extentreports.Status.INFO,"Submit button clicked");
        pageobject.getTextLogOff().waitFor();
        if(pageobject.isLogOffDisplayed()) {
            extentTest.log(com.aventstack.extentreports.Status.INFO, "HomePage is Displayed");
        }
        return pageobject.isLogOffDisplayed();
    }
}