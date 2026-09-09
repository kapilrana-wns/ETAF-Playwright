package wns.automation.core.selenium;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutoHealInvocationHandler implements InvocationHandler {

    private final AutoHealElementLocator locator;
    private final int maxRetries;

    public AutoHealInvocationHandler(AutoHealElementLocator locator) {
        this.locator = locator;
        this.maxRetries = AutoHealConfig.getInstance().getMaxRetries();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return "AutoHeal proxy for " + locator.getFieldName();
        }

        int retries = 0;
        while (true) {
            try {
                WebElement element = locator.findElement();
                return method.invoke(element, args);
            } catch (StaleElementReferenceException e) {
                retries++;
                if (retries > maxRetries) throw e;
                System.out.println("[AUTO-HEAL] StaleElementReferenceException on '"
                        + locator.getFieldName() + "', retry " + retries + "/" + maxRetries);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
