package testscripts.Playwright;

import org.testng.Assert;
import org.testng.annotations.Test;
import testconfig.PlaywrightTestManager;
import wns.automation.dataprovider.ExcelDataProviderCustom;

import java.util.Map;

public class SkillMatrixSkillsTestPW extends PlaywrightTestManager {
    @Test(dataProvider = "ExcelDataUsingMaps", dataProviderClass = ExcelDataProviderCustom.class)
    public void createsSkill(Map<String, String> data) {
        Assert.assertTrue(tc.Login(props.getProperty("ApplicationUrl"), data.get("UserName"), data.get("Password")));
        Assert.assertTrue(tc.createSkills(data.get("BusinessUnitName"), data.get("MainCategory2Name"),
                data.get("SkillName"), data.get("Description")));
        Assert.assertTrue(tc.deleteByName(data.get("SkillName")));
    }
}
