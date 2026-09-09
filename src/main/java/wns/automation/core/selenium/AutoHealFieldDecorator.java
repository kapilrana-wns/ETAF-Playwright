package wns.automation.core.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import wns.automation.utilities.LocatorHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class AutoHealFieldDecorator implements FieldDecorator {

    private final WebDriver driver;
    private final DefaultFieldDecorator defaultDecorator;

    public AutoHealFieldDecorator(WebDriver driver) {
        this.driver = driver;
        this.defaultDecorator =
                new DefaultFieldDecorator(new DefaultElementLocatorFactory(driver));
    }

    @Override
    public Object decorate(ClassLoader loader, Field field) {
        boolean isWebElement = WebElement.class.isAssignableFrom(field.getType());
        boolean isList = List.class.isAssignableFrom(field.getType());

        if (!isWebElement && !isList) {
            return null;
        }

        FindBy findBy = field.getAnnotation(FindBy.class);
        if (findBy == null) {
            return defaultDecorator.decorate(loader, field);
        }

        if (!AutoHealConfig.getInstance().isEnabled()) {
            return defaultDecorator.decorate(loader, field);
        }

        String fieldName = field.getName();
        String pageName = field.getDeclaringClass().getSimpleName();

        List<By> byList = new ArrayList<>();
        byList.add(LocatorHelper.buildByFromFindBy(findBy));
        byList.addAll(LocatorHelper.generateAlternateLocators(findBy));

        String healed = HealedLocatorStore.getInstance().getHealedLocator(pageName, fieldName);
        if (healed != null) {
            byList.add(parseByString(healed));
        }

        AutoHealElementLocator locator =
                new AutoHealElementLocator(driver, byList, fieldName, pageName);

        if (isWebElement) {
            InvocationHandler handler = new AutoHealInvocationHandler(locator);
            return Proxy.newProxyInstance(loader, new Class[]{WebElement.class}, handler);
        }

        return null;
    }

    private static By parseByString(String byString) {
        if (byString == null) return By.xpath("//*[@disabled]");
        int colon = byString.indexOf(':');
        if (colon < 0) return By.xpath(byString);
        String type = byString.substring(0, colon).trim();
        String value = byString.substring(colon + 1).trim();
        switch (type) {
            case "By.id": return By.id(value);
            case "By.name": return By.name(value);
            case "By.className": return By.className(value);
            case "By.cssSelector": return By.cssSelector(value);
            case "By.xpath": return By.xpath(value);
            case "By.linkText": return By.linkText(value);
            case "By.partialLinkText": return By.partialLinkText(value);
            case "By.tagName": return By.tagName(value);
            default: return By.xpath(value);
        }
    }
}
