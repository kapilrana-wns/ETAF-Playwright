package testscripts.SkillMatrix;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.aventstack.extentreports.Status;
import testconfig.SeleniumTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;
import java.util.Map;

@Test(groups = { "Regression", "Skills" })
public class testSubCategory extends SeleniumTestManager{

    @Test(description = "Login with valid credentials", priority=1, groups = {"Regression", "Positive", "Negative"}
            , dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSuccessfulLogin(Map<String, String> rowData) throws InterruptedException {
        String userName = rowData.get("UserName");
        String password = rowData.get("Password");
        extentTest.log(Status.INFO, "Test Data : UserName- " + userName);
        Assert.assertEquals(tc.Login(props.getProperty("ApplicationUrl"), userName, password), true);
    }

    @Test(description = "Create Sub Category",priority=2, groups = { "Positive" },
            dependsOnMethods = { "testSuccessfulLogin" }, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSubCategoryCreation(Map<String, String> rowData) {
        String businessUnitName = rowData.get("BusinessUnitName");
        String mainCategoryName = rowData.get("MainCategoryName");
        String subCategoryName = rowData.get("SubCategoryName");
        String description = rowData.get("Description");
        extentTest.log(Status.INFO,"Test Data : MainCategoryName - " + mainCategoryName + "   SubCategoryName - " + subCategoryName);
        Assert.assertEquals(tc.createSubCategory(businessUnitName, subCategoryName, description), true);
        extentTest.log(Status.PASS,"Sub Category Is Created Successfully");
    }

    @Test(description = "Delete Sub Category", groups = { "Positive" },
            dependsOnMethods = {"testSubCategoryCreation"}, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSubCategoryDelete(Map<String, String> rowData) {
        String subCategoryName = rowData.get("SubCategoryName");
        extentTest.log(Status.INFO, "Test Data : subCategoryName - " + subCategoryName);
        Assert.assertEquals(deleteSubCategory( subCategoryName),true);
        extentTest.log(Status.PASS, "Sub Category Deleted Successfully");
    }

    private boolean deleteSubCategory(String findtext) {
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