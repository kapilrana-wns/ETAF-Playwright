package testscripts.OrangeHRM;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import testconfig.OrangeHRMTestManager;

public class OrangeHRMLandingPageTest extends OrangeHRMTestManager {

    @Test(description = "Validate OrangeHRM Login page is loaded with all elements")
    public void validateLandingPageTest() {
        extentTest.log(Status.INFO, "Validating OrangeHRM login page elements");
        boolean isDisplayed = tc.isLoginPageDisplayed();
        Assert.assertTrue(isDisplayed, "Login page should display title, branding, and username field");
        extentTest.log(Status.PASS, "OrangeHRM landing page validated successfully");
    }
}
