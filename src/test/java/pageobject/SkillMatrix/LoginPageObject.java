package pageobject.SkillMatrix;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;
import testComponents.SkillMatrix.SeleniumTestComponents;

public class LoginPageObject {
	public SeleniumTestComponents tc;
	//Login Page

	@FindBy(how =How.NAME, using = "Email")
	public WebElement textUserName; 
	
	@FindBy(how =How.ID, using = "Password")
	public WebElement textPassword; 
	
	@FindBy(how = How.XPATH, using = "//li[contains(text(),'Invalid login attempt.')]")
	public WebElement textInvalidLoginAttempt; 
	
		public LoginPageObject(WebDriver driver)
		{
			//PageFactory.initElements( driver, this);
			initElements(driver);

		}

		//@Override
		public  void initElements(WebDriver driver) {
			AutoHealPageFactory.initElements(driver, this);
		}


		
}
