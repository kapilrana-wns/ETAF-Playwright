package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class HomepagePageObject {

	//Home Page
	@FindBy(how =How.CLASS_NAME, using = "navbar-brand")
	public WebElement textHomePage;
	
	@FindBy(how =How.XPATH, using = "//h2[contains(text(),'Log in.')]")
	public WebElement textLogInPage;
	
	@FindBy(how =How.XPATH, using = "//a[contains(text(),'Log off')]")
	public WebElement btnLogoff;
	
	//Top Menus
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Skill Management')]")
	public WebElement menuSkillManagement;
	
	@FindBy(how = How.XPATH, using = "//a[contains(text(),'Admin')]")
	public WebElement menuAdmin;
	
	@FindBy(how = How.PARTIAL_LINK_TEXT, using = "Skill Evaluation Mg")
	public WebElement menuSkillEvalManagement;
	
	@FindBy(how = How.XPATH, using = "//body/div[1]/div[1]/div[2]/ul[1]/li[4]/a[1]")
	public WebElement menuReports;
	
	@FindBy(how = How.XPATH, using = "//body/div[1]/div[1]/div[2]/ul[1]/li[1]/a[1]")
	public WebElement menuSkills;
	
	public HomepagePageObject(WebDriver driver)
	{
		//PageFactory.initElements( driver, this);
		initElements(driver);

	}

	//@Override
	public  void initElements(WebDriver driver) {
		AutoHealPageFactory.initElements(driver, this);
	}
}
