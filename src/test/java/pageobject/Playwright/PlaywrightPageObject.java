package pageobject.Playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import wns.automation.core.playwright.AutoHealLocator;

import java.util.Properties;

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
        initializeElements();
    }
    
    public void initializeElements() {
        Properties properties = loadProperties();
        this.textUserName = AutoHealLocator.resolve(page, properties, "textUserName",
                "input[name='Email']", "input[type='email']", "input[placeholder*='Email']");
        this.textPassword = AutoHealLocator.resolve(page, properties, "textPassword",
                "input#Password", "input[name='Password']", "input[type='password']");
        this.btnSubmit = AutoHealLocator.resolve(page, properties, "btnSubmit",
                "//input[@class='btn btn-primary']", "input[type='submit']", "button:has-text('Log in')");
        this.textInvalidLoginAttempt = page.locator(
                "//li[contains(text(),'Invalid login attempt.')]");
        this.btnLogOff = page.locator("//a[contains(text(),'Log off')]");
        this.txtDashboard = AutoHealLocator.resolve(page, properties, "txtDashboard",
                "//h6[text()='Dashboard']", "//*[normalize-space()='Dashboard']");
        this.ohrmUserName = AutoHealLocator.resolve(page, properties, "ohrmUserName",
                "input[name='username']", "input[placeholder*='Username']", "input[type='text']");
        this.ohrmPassword = AutoHealLocator.resolve(page, properties, "ohrmPassword",
                "input[name='password']", "input[placeholder*='Password']", "input[type='password']");
        this.ohrmLoginButton = AutoHealLocator.resolve(page, properties, "ohrmLoginButton",
                "//button[@type='submit']", "button[type='submit']", "button:has-text('Login')");
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        String path = System.getProperty("user.dir")
                + "\\src\\test\\java\\testconfig\\test.properties";
        try (java.io.FileInputStream input = new java.io.FileInputStream(path)) {
            properties.load(input);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Unable to load auto-heal configuration: " + path, error);
        }
        return properties;
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