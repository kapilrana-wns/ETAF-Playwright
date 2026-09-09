package pageobject.Playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class PlaywrightPageObject {
    private Page page;
    
    // Locators for Login Page
    private Locator textUserName;
    private Locator textPassword;
    private Locator btnSubmit;
    private Locator textInvalidLoginAttempt;
    private Locator btnLogOff;
    private Locator txtDashboard;
    private Locator ohrmUserName;
    private Locator ohrmPassword;
    private Locator ohrmLoginButton;
    
    public PlaywrightPageObject(Page page) {
        this.page = page;
        initElements();
    }
    
    private void initElements() {
        this.textUserName = page.locator("input[name='Email']");
        this.textPassword = page.locator("input#Password");
        this.btnSubmit = page.locator("//input[@class='btn btn-primary']");
        this.textInvalidLoginAttempt = page.locator("//li[contains(text(),'Invalid login attempt.')]");
        this.btnLogOff = page.locator("//a[contains(text(),'Log off')]");
        this.txtDashboard = page.locator("//h6[text()='Dashboard']");
        this.ohrmUserName = page.locator("input[name='username']");
        this.ohrmPassword = page.locator("input[name='password']");
        this.ohrmLoginButton = page.locator("//button[@type='submit']");
    }
    
    // Getter methods for locators
    public Locator getTextUserName() {
        return textUserName;
    }
    
    public Locator getTextPassword()
    {
        return textPassword;
    }
    
    public Locator getTextInvalidLoginAttempt() {
        return textInvalidLoginAttempt;
    }

    public Locator getTextLogOff() {
        return btnLogOff;
    }
    public Locator getDashboard() {return txtDashboard;}
    public Locator getOhrmUserName() {return ohrmUserName;}

    public Locator getOhrmPassword() {return ohrmPassword;}

    public Locator getOhrmLoginButton() {return ohrmLoginButton;}
    
    // Action methods
    public void enterUsername(String username) {
        textUserName.fill(username);
    }
    
    public void enterPassword(String password) {
        textPassword.fill(password);
    }
    
    public boolean isInvalidLoginAttemptDisplayed() {
        return textInvalidLoginAttempt.isVisible();
    }
    
    public void Submit() {
        btnSubmit.click();
    }

    public boolean isLogOffDisplayed() {
        return btnLogOff.isVisible();
    }

    public void LogOff() {
        btnLogOff.click();
    }
}