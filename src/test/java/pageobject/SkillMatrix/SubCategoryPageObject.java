package pageobject.SkillMatrix;

import groovy.json.JsonOutput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class SubCategoryPageObject {
    @FindBy(how = How.LINK_TEXT, using = "Sub Category")
    public WebElement menuSubCategory;

    @FindBy(how=How.ID, using = "AssociatedMainCategoryID")
    public WebElement dropdownMainCategory;

    @FindBy(how = How.ID, using = "SubCategoryName")
    public WebElement textboxSubCategoryName;

    @FindBy(how=How.ID, using = "AssociatedBusinessUnitID")
    public WebElement dropdownBusinessUnit;

    @FindBy(how = How.XPATH, using = "/html/body/div[2]/main/table/tbody/tr[2]/td[3]")
    public WebElement subCategoryName;
	
	public SubCategoryPageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}
}
