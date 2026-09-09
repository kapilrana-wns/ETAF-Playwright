package testComponents.Playwright;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
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
            //page.locator("Log off").waitFor();
//            page.setDefaultTimeout(60000);

           /* if(pageobject.isLogOffDisplayed()) {
                extentTest.log(com.aventstack.extentreports.Status.INFO, "HomePage is Displayed");
            }
			return pageobject.isLogOffDisplayed();*/
        //pageobject.Submit();
        pageobject.getTextLogOff().waitFor(
                new Locator.WaitForOptions().setTimeout(6000));
        System.out.println("Login successful");
        //page.pause();
        extentTest.log(Status.INFO, "Home Page Displayed");
//        try {
//            Thread.sleep(30000); // 5 minutes
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        page.waitForLoadState();
        return true;
	}

    @Override
    public boolean LogOff() {
        pageobject.LogOff();
        pageobject.getTextUserName().waitFor();
        System.out.println("Login page displayed after logout");
        page.pause(); // Keeps browser open
        return true;
    }
    public boolean verifyLoginPageDisplayed() {
        try {
            pageobject.getOhrmUserName().waitFor(new Locator.WaitForOptions().setTimeout(5000));

            return pageobject.getOhrmUserName().isVisible() && pageobject.getOhrmPassword().isVisible();

        } catch (Exception e) {

            return false;
        }
    }

    public boolean enterUserName(String userName) {

        pageobject.getOhrmUserName().fill(userName);
        return true;
    }

    public boolean enterPassword(String password) {

        pageobject.getOhrmPassword().fill(password);
        return true;
    }

    public boolean clickLoginButton() {

        pageobject.getOhrmLoginButton().click();
        return true;
    }

    public boolean verifyDashboardDisplayed() {

        try {

            pageobject.getDashboard().waitFor(
                    new Locator.WaitForOptions().setTimeout(10000));

            extentTest.log(Status.INFO, "Dashboard displayed");

            return pageobject.getDashboard().isVisible();

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
    public boolean launchApplication(String url) {

        try {
            openWebApp(url);
            System.out.println("Opened URL : " + page.url());
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
}