package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class MainCategoryPageObject {
	@FindBy(how = How.LINK_TEXT, using = "Main Category")
	public WebElement menuMainCategory;
	
	@FindBy(how=How.ID, using = "AssociatedBusinessUnitID")
	public WebElement dropdownBusinessUnit;
	
	@FindBy(how = How.ID, using = "MainCategoryName")
	public WebElement textboxMainCategoryName;

	@FindBy(how = How.XPATH, using = "/html/body/div[2]/main/table/tbody/tr[2]/td[2]")
	public WebElement mainCategoryName;
	
	public MainCategoryPageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}
}
