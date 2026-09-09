package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class CommonPageObject{
	

	@FindBy(how =How.CSS, using = "input[type=submit]")
	public WebElement btnSubmit; 
	
	@FindBy(how =How.ID, using = "SearchString")
	public WebElement textSearch; 
	
	@FindBy(how = How.XPATH, using = "//body/div[2]/main[1]/form[1]/div[1]/input[2]")
	public WebElement  btnSearch;
	
	@FindBy(how = How.LINK_TEXT, using = "Create New")
	public WebElement lnkCreate;
	
	@FindBy(how = How.LINK_TEXT, using = "Delete")
	public WebElement lnkDelete;
	
	@FindBy(how = How.ID, using = "Description")
	public WebElement textboxDescription;

	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Back to List')]")
	public WebElement lnkBackToList;
	
	@FindBy(how=How.LINK_TEXT,using="Edit")
	public WebElement lnkEdit;
	
	@FindBy(how = How.XPATH, using = "/html/body/div[2]/main/script/text()")
	public WebElement alertMsg;
	

	
	
	public CommonPageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}
}