package testComponents.Playwright;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;
import pageobject.Playwright.PlaywrightPageObject;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.playwright.PlaywrightWebActionManager;

public class PlaywrightTestComponents extends PlaywrightWebActionManager implements  IApplicationActionManager {

    // Every page object should hvae following variables and methods included

    public PlaywrightPageObject pageobject;
    public ExtentTest extentTest;

    public PlaywrightTestComponents(Playwright driver, Page page)
    {
        super.setPlaywrightContext(driver, page);
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
        page.locator("input[name='username']")
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        page.locator("input[name='password']")
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE));
        return true;
    }

    public boolean enterUserName(String username) {
        page.locator("input[name='username']").fill(username);
        return true;
    }

    public boolean enterPassword(String password) {
        page.locator("input[name='password']").fill(password);
        return true;
    }

    public boolean clickLoginButton() {
        page.locator("button[type='submit']").click();
        return true;
    }

    public boolean verifyDashboardDisplayed() {
        Locator dashboardHeading = page.locator("h6.oxd-topbar-header-breadcrumb-module");
        dashboardHeading.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        return "Dashboard".equals(dashboardHeading.textContent().trim());
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