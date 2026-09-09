package pageobject.OrangeHRM;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class LoginPage {

    @FindBy(how = How.NAME, using = "username")
    public WebElement textboxUsername;

    @FindBy(how = How.NAME, using = "password")
    public WebElement textboxPassword;

    @FindBy(how = How.XPATH, using = "//button[@type='submit']")
    public WebElement btnLogin;

    @FindBy(how = How.XPATH, using = "//p[contains(@class,'oxd-alert')]")
    public WebElement textInvalidCredential;

    @FindBy(how = How.XPATH, using = "//h5[contains(normalize-space(),'Login')]")
    public WebElement textLoginTitle;

    @FindBy(how = How.CSS, using = ".orangehrm-login-branding")
    public WebElement imgBranding;

    public LoginPage(WebDriver driver) {
        AutoHealPageFactory.initElements(driver, this);
    }
}
