package testscripts.SkillMatrix;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.aventstack.extentreports.Status;
import testconfig.SeleniumTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;
import java.util.Map;

@Test(groups = { "Regression", "Skills" })
public class testSkills extends SeleniumTestManager {

    @Test(description = "Login with valid credentials", priority=1, groups = {"Regression", "Positive", "Negative"}
            , dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSuccessfulLogin(Map<String, String> rowData) throws InterruptedException {
        String userName = rowData.get("UserName");
        String password = rowData.get("Password");
        extentTest.log(Status.INFO, "Test Data : UserName- " + userName);
        Assert.assertEquals(tc.Login(props.getProperty("ApplicationUrl"), userName, password), true);
    }

    @Test(description = "Create Skill", priority=2, groups = { "Positive" },
            dependsOnMethods = { "testSuccessfulLogin" }, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSkillCreation(Map<String, String> rowData) {
        String businessUnitName = rowData.get("BusinessUnitName");
        String mainCategoryName = rowData.get("MainCategory2Name");
        String subCategoryName = rowData.get("SubCategoryName");
        String skillName = rowData.get("SkillName");
        String description = rowData.get("Description");
        extentTest.log(Status.INFO,"Test Data : MainCategoryName - " + mainCategoryName + "   SubCategoryName - " + subCategoryName + "   Skill Name - " + skillName);
        Assert.assertEquals(tc.createSkills(businessUnitName, mainCategoryName, skillName, description), true);
        extentTest.log(Status.PASS,"Skill Is Created Successfully");
    }

    @Test(description = "Delete Skill", priority=3, groups = { "Positive" },
            dependsOnMethods = {"testSkillCreation"}, dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    void testSkillDelete(Map<String, String> rowData) {
        String skillName = rowData.get("SkillName");
        extentTest.log(Status.INFO, "Test Data : skillName - " + skillName);
        Assert.assertEquals(deleteContent(skillName),true);
        extentTest.log(Status.PASS, "Skill Deleted Successfully");
    }

    private boolean deleteContent(String findtext) {
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