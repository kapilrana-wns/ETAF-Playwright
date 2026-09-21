package bdd.stepdefinitions;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import bdd.hooks.Hooks;

public class OrangeHRMSteps {
    @Given("User opens OrangeHRM application")
    public void openApplication() {
        Page page = Hooks.page();
        String url = System.getProperty(
                "OrangeHRMUrl",
                "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        page.navigate(url);
        page.locator("input[name='username']").waitFor();
    }

    @When("User enters username {string} and password {string}")
    public void enterCredentials(String username, String password) {
        Page page = Hooks.page();
        page.locator("input[name='username']").fill(username);
        page.locator("input[name='password']").fill(password);
    }

    @When("User clicks on login button")
    public void clickLogin() {
        Page page = Hooks.page();
        page.locator("button[type='submit']").click();
    }

    @Then("User should be navigated to dashboard page")
    public void verifyDashboard() {
        Page page = Hooks.page();
        Assert.assertTrue(
                page.getByText("Dashboard", new Page.GetByTextOptions().setExact(true)).isVisible(),
                "Dashboard should be displayed");
    }

    @Then("Error message should be displayed")
    public void verifyErrorMessage() {
        Page page = Hooks.page();
        Assert.assertTrue(
                page.locator(".oxd-alert-content-text").isVisible(),
                "Invalid login error should be displayed");
    }
}
