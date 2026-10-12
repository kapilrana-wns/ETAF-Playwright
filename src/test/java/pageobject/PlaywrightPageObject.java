package pageobject;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import wns.automation.core.playwright.AutoHealBy;
import wns.automation.core.playwright.AutoHealPageFactory;

public class PlaywrightPageObject {
    // Locators for Login Page
    @AutoHealBy(value = "input[name='Email']", alternatives = {
            "input#Email", "input[type='email']"
    })
    private Locator textUserName;
    @AutoHealBy(value = "input#Password", alternatives = {
            "input[name='Password']", "input[type='password']"
    })
    private Locator textPassword;
    @AutoHealBy(value = "//input[@class='btn btn-primary']", alternatives = {
            "input.btn.btn-primary", "input[type='submit']"
    })
    private Locator btnSubmit;
    @AutoHealBy(value = "//li[contains(text(),'Invalid login attempt.')]", alternatives = {
            "text=Invalid login attempt."
    })
    private Locator textInvalidLoginAttempt;
    @AutoHealBy(value = "//a[contains(text(),'Log off')]", alternatives = {
            "text=Log off"
    })
    private Locator btnLogOff;

    public PlaywrightPageObject(Page page) {
        AutoHealPageFactory.initElements(page, this);
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