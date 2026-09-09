package bdd.stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.testng.Assert.*;
import bdd.hooks.Hooks;
import pageobject.OrangeHRM.LoginPage;
import pageobject.OrangeHRM.DashboardPage;

public class OrangeHRMSteps {

    WebDriver driver;
    WebDriverWait wait;
    LoginPage loginPage;
    DashboardPage dashboardPage;

    @Given("User opens OrangeHRM application")
    public void open_application() {
        driver = Hooks.driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);

        driver.get("https://opensource-demo.orangehrmlive.com");
        driver.manage().window().maximize();
    }

    @When("User enters username {string} and password {string}")
    public void enter_credentials(String username, String password) {
        wait.until(ExpectedConditions.visibilityOf(loginPage.textboxUsername));
        loginPage.textboxUsername.sendKeys(username);
        loginPage.textboxPassword.sendKeys(password);
    }

    @When("User clicks on login button")
    public void click_login() {
        loginPage.btnLogin.click();
    }

    @Then("User should be navigated to dashboard page")
    public void validate_dashboard() {
        WebElement dashboard = wait.until(ExpectedConditions.visibilityOf(dashboardPage.headingDashboard));
        assertTrue(dashboard.isDisplayed(), "Dashboard not displayed");
    }

    @Then("Error message should be displayed")
    public void validate_error_message() {
        WebElement error = wait.until(ExpectedConditions.visibilityOf(loginPage.textInvalidCredential));
        assertTrue(error.isDisplayed(), "Error message not displayed");
    }
}