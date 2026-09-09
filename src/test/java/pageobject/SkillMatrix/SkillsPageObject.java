package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class SkillsPageObject {
    @FindBy(how=How.ID, using = "AssociatedSubCategoryID")
    public WebElement dropdownSubCategory;

    @FindBy(how = How.XPATH, using = "//input[@id='Name']")
    public WebElement textboxSkillName;

    @FindBy(how=How.ID, using = "AssociatedBusinessUnitID")
    public WebElement dropdownBusinessUnit;

	@FindBy(how = How.XPATH, using = "/html/body/div[2]/main/table/tbody/tr[2]/td[5]")
	public WebElement skillName;
	
	public SkillsPageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}
}
