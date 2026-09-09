package wns.automation.core.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class AutoHealPageFactory {

    public static void initElements(WebDriver driver, Object page) {
        PageFactory.initElements(new AutoHealFieldDecorator(driver), page);
    }
}
