package pageobject.OrangeHRM;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import wns.automation.core.selenium.AutoHealPageFactory;

public class DashboardPage {

    @FindBy(how = How.XPATH, using = "//h6[contains(normalize-space(),'Dashboard')]")
    public WebElement headingDashboard;

    @FindBy(how = How.CSS, using = ".oxd-userdropdown-name")
    public WebElement dropdownUser;

    @FindBy(how = How.XPATH, using = "//span[contains(@class,'oxd-main-menu-item--name') and normalize-space()='PIM']")
    public WebElement menuPIM;

    @FindBy(how = How.XPATH, using = "//span[contains(@class,'oxd-main-menu-item--name') and normalize-space()='Admin']")
    public WebElement menuAdmin;

    @FindBy(how = How.XPATH, using = "//span[contains(@class,'oxd-main-menu-item--name') and normalize-space()='Time']")
    public WebElement menuTime;

    @FindBy(how = How.XPATH, using = "//span[contains(@class,'oxd-main-menu-item--name') and normalize-space()='Leave']")
    public WebElement menuLeave;

    @FindBy(how = How.XPATH, using = "//span[contains(@class,'oxd-main-menu-item--name') and normalize-space()='My Info']")
    public WebElement menuMyInfo;

    @FindBy(how = How.CSS, using = ".oxd-userdropdown-name")
    public WebElement textProfileName;

    @FindBy(how = How.XPATH, using = "//a[contains(normalize-space(),'Logout')]")
    public WebElement linkLogout;

    public DashboardPage(WebDriver driver) {
        AutoHealPageFactory.initElements(driver, this);
    }
}
