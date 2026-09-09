package testComponents.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import wns.automation.core.ExtentTestManager;
import wns.automation.core.IApplicationActionManager;
import wns.automation.core.selenium.SeleniumWebActionManager;
import wns.automation.utilities.*;
import pageobject.SkillMatrix.SeleniumPageObject;
import pageobject.SkillMatrix.SkillsPageObject;
import pageobject.SkillMatrix.SubCategoryPageObject;
import pageobject.SkillMatrix.CommonPageObject;
import pageobject.SkillMatrix.HomepagePageObject;
import pageobject.SkillMatrix.LoginPageObject;
import pageobject.SkillMatrix.MainCategoryPageObject;

public class SeleniumTestComponents extends SeleniumWebActionManager implements IApplicationActionManager  {

	public  SeleniumPageObject pageobject;
	public LoginPageObject loginPageObject;
	public MainCategoryPageObject mainCategoryPageObject;
	public SubCategoryPageObject subCategoryPageObject;
	public SkillsPageObject skillsPageObject;
	public HomepagePageObject homepagePageObject;
	public CommonPageObject commonPageObject;
 	public  ExtentTest extentTest;
	
 	public SeleniumTestComponents(WebDriver driver)
	{		
		super.setWebDriver(driver);
		this.pageobject = new SeleniumPageObject(driver);
		this.loginPageObject = new LoginPageObject(driver);
		this.mainCategoryPageObject = new MainCategoryPageObject(driver);
		this.subCategoryPageObject = new SubCategoryPageObject(driver);
		this.skillsPageObject = new SkillsPageObject(driver);
		this.homepagePageObject = new HomepagePageObject(driver);
		this.commonPageObject = new CommonPageObject(driver);
	}

	@Override
	public void setReportObject(ExtentTest extentTest)
	{
		this.extentTest = extentTest;
		Reporter.setExtentTest(extentTest);
		ExtentTestManager.setExtentTest(extentTest);
	}

	public boolean Login(String url, String UserName, String Password) throws InterruptedException {
		try {
            System.out.println(url);
            System.out.println("From Login method");
            openWebApp(url);
            Reporter.log(Status.INFO,"Application launched");
            Input(loginPageObject.textUserName, UserName);
            Reporter.log(Status.INFO,"Username input is provided");
            Input(loginPageObject.textPassword, Password);
            Reporter.log(Status.INFO,"Password input is provided");
            Click(commonPageObject.btnSubmit);
            Reporter.log(Status.INFO,"Submit button is clicked");
            return isDisplayed(homepagePageObject.btnLogoff);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean createMainCategory(String BusinessUnit, String MainCategoryName, String Description) {
        try {
            InputMainCategoryData(BusinessUnit, MainCategoryName, Description);
            Search(MainCategoryName);
            String mainCategoryName = mainCategoryPageObject.mainCategoryName.getText();
            if (mainCategoryName.equalsIgnoreCase(MainCategoryName)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Reporter.log(Status.FAIL,"Main Category Creation Has Failed");
            return false;
        }
    }

    public void delete(String findtext) throws InterruptedException {
        //	Search(findtext);
        Thread.sleep(2000);
        WebElement element = TestUtility.getWebElementFromTable(getDriver(),"//table/tbody", 2, 10,1,4, findtext);
        System.out.println("For Delete - " + element.getText());
        Click(commonPageObject.lnkDelete);
        Thread.sleep(2000);
        Click(commonPageObject.btnSubmit);
        Thread.sleep(5000);
    }

    /**
     * @param BusinessUnit
     * @param MainCategoryName
     * @param Description
     */
    public void InputMainCategoryData(String BusinessUnit, String MainCategoryName, String Description) throws InterruptedException {
        Reporter.log(Status.INFO,"Click On Skill Management Menu");
        Click(homepagePageObject.menuSkillManagement);
        Reporter.log(Status.INFO,"Select Main Category From The Skill Management DropDown");
        Thread.sleep(2000);
        Click(mainCategoryPageObject.menuMainCategory);
        if (isDisplayed(commonPageObject.lnkCreate)) {
            Reporter.log(Status.INFO,"Click On Create New Button");
            commonPageObject.lnkCreate.click();
            Thread.sleep(2000);
            Reporter.log(Status.INFO,"Select Business Unit From The DropDown");
            selectValueFromDropDown(mainCategoryPageObject.dropdownBusinessUnit, BusinessUnit);
            Reporter.log(Status.INFO,"Provide Main Category Input");
            Input(mainCategoryPageObject.textboxMainCategoryName, MainCategoryName);
            Reporter.log(Status.INFO,"Provide Description");
            Input(commonPageObject.textboxDescription, Description);
            Reporter.log(Status.INFO,"Click On Submit Button");
            Click(commonPageObject.btnSubmit);
            Thread.sleep(2000);
        }
    }

    public void Search(String text) {
        Input(commonPageObject.textSearch,text);
        Click(commonPageObject.btnSearch);
    }

    public boolean createSubCategory(String BusinessUnitName, String SubCategoryName, String Description) {
        try {
            InputSubCategoryData(BusinessUnitName, SubCategoryName, Description);
            Search(SubCategoryName);
            String subCategoryName = subCategoryPageObject.subCategoryName.getText();
            if (subCategoryName.equalsIgnoreCase(SubCategoryName)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Reporter.log(Status.FAIL,"Sub Category Creation Has Failed");
            return false;
        }
    }

    /**
     * @param SubCategoryName
     * @param Description
     */
    public void InputSubCategoryData(String BusinessUnitName, String SubCategoryName, String Description) throws InterruptedException {
        Reporter.log(Status.INFO,"Click On Skill Management Menu");
        Click(homepagePageObject.menuSkillManagement);
        Reporter.log(Status.INFO,"Select Sub Category From The Skill Management DropDown");
        Click(subCategoryPageObject.menuSubCategory);
        Thread.sleep(2000);
        if (isDisplayed(commonPageObject.lnkCreate)) {
            Reporter.log(Status.INFO,"Click On Create New Button");
            commonPageObject.lnkCreate.click();
            Thread.sleep(2000);
            Reporter.log(Status.INFO,"Select Business Unit From The DropDown");
            selectValueFromDropDown(subCategoryPageObject.dropdownBusinessUnit, BusinessUnitName);
            Reporter.log(Status.INFO,"Provide SubCategoryName Input");
            Input(subCategoryPageObject.textboxSubCategoryName, SubCategoryName);
            Reporter.log(Status.INFO,"Provide Description");
            Input(commonPageObject.textboxDescription, Description);
            Reporter.log(Status.INFO,"Click On Submit Button");
            Click(commonPageObject.btnSubmit);
            Thread.sleep(2000);
        }
    }

    public boolean createSkills(String businessUnitName, String mainCategoryName, String skillName, String description) {
        try {
            InputSkillData(businessUnitName, mainCategoryName, skillName, description);
            Search(skillName);
            String newSkillName = skillsPageObject.skillName.getText();
            if (newSkillName.equalsIgnoreCase(skillName)) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Reporter.log(Status.FAIL,"Skill Creation Has Failed");
            return false;
        }
    }

    public void InputSkillData(String businessUnitName, String mainCategoryName, String skillName, String description) throws InterruptedException {
        Reporter.log(Status.INFO,"Click On Skill Management Menu");
        Click(homepagePageObject.menuSkillManagement);
        Reporter.log(Status.INFO,"Select Skills From The Skill Management DropDown");
        Click(pageobject.subMenuSkills);
        Thread.sleep(2000);
        if (isDisplayed(commonPageObject.lnkCreate)) {
            Reporter.log(Status.INFO,"Click On Create New Button");
            commonPageObject.lnkCreate.click();
            Thread.sleep(2000);
            Reporter.log(Status.INFO,"Select Business Unit From The DropDown");
            selectValueFromDropDown(skillsPageObject.dropdownBusinessUnit, businessUnitName);
            Reporter.log(Status.INFO,"Select Main Category Name From The DropDown");
            selectValueFromDropDown(subCategoryPageObject.dropdownMainCategory, mainCategoryName);
            Reporter.log(Status.INFO,"Provide Skill Name Input");
            Input(skillsPageObject.textboxSkillName, skillName);
            Reporter.log(Status.INFO,"Provide Description");
            Input(commonPageObject.textboxDescription, description);
            Reporter.log(Status.INFO,"Click On Submit Button");
            Click(commonPageObject.btnSubmit);
            Thread.sleep(2000);
        }
    }

    public void goToMainCategoryScreen() throws InterruptedException {
        Reporter.log(Status.INFO,"Click On Skill Management Menu");
        Click(homepagePageObject.menuSkillManagement);
        Reporter.log(Status.INFO,"Select Main Category From The Skill Management DropDown");
        Thread.sleep(2000);
        Click(mainCategoryPageObject.menuMainCategory);
        Thread.sleep(2000);
    }

    public void goToSubCategoryScreen() throws InterruptedException {
        Reporter.log(Status.INFO,"Click On Skill Management Menu");
        Click(homepagePageObject.menuSkillManagement);
        Reporter.log(Status.INFO,"Select Sub Category From The Skill Management DropDown");
        Thread.sleep(2000);
        Click(subCategoryPageObject.menuSubCategory);
        Thread.sleep(2000);
    }
}