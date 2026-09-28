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