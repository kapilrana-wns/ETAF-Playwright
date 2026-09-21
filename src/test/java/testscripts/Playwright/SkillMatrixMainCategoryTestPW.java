package testscripts.Playwright;

import org.testng.Assert;
import org.testng.annotations.Test;
import testconfig.PlaywrightTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;

import java.util.Map;

public class SkillMatrixMainCategoryTestPW extends PlaywrightTestManager {
    @Test(dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    public void createsMainCategory(Map<String, String> data) {
        Assert.assertTrue(tc.Login(props.getProperty("ApplicationUrl"), data.get("UserName"), data.get("Password")));
        Assert.assertTrue(tc.createMainCategory(data.get("BusinessUnitName"), data.get("MainCategoryName"), data.get("Description")));
        Assert.assertTrue(tc.deleteByName(data.get("MainCategoryName")));
    }
}
