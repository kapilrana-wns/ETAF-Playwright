package testComponents.Playwright;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import pageobject.Playwright.PlaywrightPageObject;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.playwright.PlaywrightWebActionManager;
import java.util.regex.Pattern;

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

        public boolean createMainCategory(String businessUnit, String name, String description) {
            return createCategory("Main Category", businessUnit, name, description, "MainCategoryName",
                    "//table/tbody/tr[2]/td[2]");
        }

        public boolean createSubCategory(String businessUnit, String name, String description) {
            return createCategory("Sub Category", businessUnit, name, description, "SubCategoryName",
                    "//table/tbody/tr[2]/td[3]");
        }

        public boolean createSkills(String businessUnit, String mainCategory, String name, String description) {
            page.getByText("Skill Management", new Page.GetByTextOptions().setExact(true)).click();
            page.getByText("Skills", new Page.GetByTextOptions().setExact(true)).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New")).click();
            page.locator("#AssociatedBusinessUnitID").selectOption(new SelectOption().setLabel(businessUnit));
            page.locator("#AssociatedSubCategoryID").selectOption(new SelectOption().setLabel(mainCategory));
            page.locator("#Name").fill(name);
            page.locator("#Description").fill(description);
            page.locator("input[type='submit']").click();
            return page.locator("//table/tbody/tr[2]/td[5]").filter(
                    new Locator.FilterOptions().setHasText(Pattern.compile(Pattern.quote(name)))).isVisible();
        }

        private boolean createCategory(String menu, String businessUnit, String name, String description,
                                       String nameField, String resultLocator) {
            page.getByText("Skill Management", new Page.GetByTextOptions().setExact(true)).click();
            page.getByText(menu, new Page.GetByTextOptions().setExact(true)).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New")).click();
            page.locator("#AssociatedBusinessUnitID").selectOption(new SelectOption().setLabel(businessUnit));
            page.locator("#" + nameField).fill(name);
            page.locator("#Description").fill(description);
            page.locator("input[type='submit']").click();
            return page.locator(resultLocator).filter(
                    new Locator.FilterOptions().setHasText(Pattern.compile(Pattern.quote(name)))).isVisible();
        }

        public boolean deleteByName(String name) {
            Locator row = page.locator("table tbody tr").filter(
                    new Locator.FilterOptions().setHasText(Pattern.compile(Pattern.quote(name)))).first();
            row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Delete")).click();
            page.locator("input[type='submit']").click();
            return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create New")).isVisible();
        }
}