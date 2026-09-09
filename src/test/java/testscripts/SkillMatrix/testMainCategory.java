package testscripts.SkillMatrix;

import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.aventstack.extentreports.Status;
import wns.automation.dataprovider.ExcelDataProviderCustom;
import testconfig.SeleniumTestManager;

@Test(groups = {"Regression", "Skills"})
public class testMainCategory extends SeleniumTestManager {

    @Test(description = "Login with valid credentials", priority=1, groups = {"Regression", "Positive", "Negative"}
            , dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSuccessfulLogin(Map<String, String> rowData) throws InterruptedException {
        String userName = rowData.get("UserName");
        String password = rowData.get("Password");
        extentTest.log(Status.INFO, "Test Data : UserName- " + userName);
        Assert.assertEquals(tc.Login(props.getProperty("ApplicationUrl"), userName, password), true);
    }

    @Test(description = "Create Main Category", priority = 2, groups = {"Positive", "Negative"},
            dependsOnMethods = {"testSuccessfulLogin"}, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testMainCategoryCreation(Map<String, String> rowData) {
        String businessUnitName = rowData.get("BusinessUnitName");
        String mainCategoryName = rowData.get("MainCategoryName");
        String description = rowData.get("Description");
        extentTest.log(Status.INFO, "Test Data : BusinessUnit - " + businessUnitName + "   MainCategoryName - " + mainCategoryName);
        Assert.assertEquals(tc.createMainCategory(businessUnitName, mainCategoryName, description), true);
        extentTest.log(Status.PASS, "Main Category Is Successfully Created");
    }

    @Test(description = "Delete Main Category", groups = {"Positive"},
            dependsOnMethods = {"testMainCategoryCreation"}, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testMainCategoryDelete(Map<String, String> rowData) {
        String mainCategoryName = rowData.get("MainCategoryName");
        extentTest.log(Status.INFO, "Test Data : mainCategoryName - " + mainCategoryName);
        Assert.assertEquals(deleteMainCategory(mainCategoryName), true);
        extentTest.log(Status.PASS, "Main Category Deleted Successfully");
    }

    private boolean deleteMainCategory(String findtext) {
        try {
            tc.delete(findtext);
            if (tc.isDisplayed(tc.commonPageObject.lnkCreate))
                return true;
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}