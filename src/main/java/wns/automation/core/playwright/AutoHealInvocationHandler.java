package wns.automation.core.playwright;

import com.microsoft.playwright.Locator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AutoHealInvocationHandler implements InvocationHandler {

    private final AutoHealElementLocator locator;

    public AutoHealInvocationHandler(AutoHealElementLocator locator) {
        this.locator = locator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            switch (method.getName()) {
                case "toString":
                    return "AutoHeal Locator for " + locator.getPageName() + "." + locator.getFieldName();
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                default:
                    throw new UnsupportedOperationException("Unsupported Object method: " + method.getName());
            }
        }

        Locator target = locator.findLocator();
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
