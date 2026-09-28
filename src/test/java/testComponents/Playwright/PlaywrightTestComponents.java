package testComponents.Playwright;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import pageobject.Playwright.PlaywrightPageObject;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.playwright.PlaywrightWebActionManager;

public class PlaywrightTestComponents extends PlaywrightWebActionManager implements  IApplicationActionManager {

    // Every page object should hvae following variables and methods included

    public PlaywrightPageObject pageobject;
    public ExtentTest extentTest;

    public PlaywrightTestComponents(Playwright driver, Page page)
    {
        super.setWebDriver(driver, page);
        this.pageobject = new PlaywrightPageObject(super.page);
    }

    @Override
    public void setReportObject(ExtentTest extentTest)
    {
        this.extentTest = extentTest;
        Reporter.setExtentTest(extentTest);
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
        page.locator("Log off").waitFor();
        if(pageobject.isLogOffDisplayed()) {
            extentTest.log(com.aventstack.extentreports.Status.INFO, "HomePage is Displayed");
        }
        return pageobject.isLogOffDisplayed();
    }
}