package pageobject.OrangeHRM;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class AutoHealDemoPage {

    @FindBy(how = How.ID, using = "username")
    public WebElement textboxUsername;

    @FindBy(how = How.ID, using = "password")
    public WebElement textboxPassword;

    @FindBy(how = How.XPATH, using = "//button[@type='submit']")
    public WebElement btnLogin;

    public AutoHealDemoPage(WebDriver driver) {
        AutoHealPageFactory.initElements(driver, this);
    }
}
