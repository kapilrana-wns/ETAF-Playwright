package wns.automation.core.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import wns.automation.utilities.LocatorHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class AutoHealFieldDecorator {

    private final Page page;

    public AutoHealFieldDecorator(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Playwright Page must not be null");
        }
        this.page = page;
    }

    public void decorate(Object pageObject) {
        if (pageObject == null) {
            throw new IllegalArgumentException("Page object must not be null");
        }

        for (Class<?> type = pageObject.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                AutoHealBy annotation = field.getAnnotation(AutoHealBy.class);
                if (annotation == null) {
                    continue;
                }
                try {
                    Object value = createFieldValue(field, annotation);
                    field.setAccessible(true);
                    field.set(pageObject, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(
                            "Could not initialize auto-heal field " + field.getDeclaringClass().getName()
                                    + "." + field.getName(), e);
                }
            }
        }
    }

    private Object createFieldValue(Field field, AutoHealBy annotation) {
        List<String> selectors = new ArrayList<>();
        selectors.add(LocatorHelper.normalize(annotation.value()));
        boolean enabled = AutoHealConfig.getInstance().isEnabled();
        if (enabled) {
            for (String alternative : annotation.alternatives()) {
                selectors.add(LocatorHelper.normalize(alternative));
            }
            selectors.addAll(LocatorHelper.generateAlternateLocators(annotation.value()));
        }
        if (enabled) {
            String healedSelector = HealedLocatorStore.getInstance()
                    .getHealedLocator(field.getDeclaringClass().getSimpleName(), field.getName());
            if (healedSelector != null) {
                selectors.add(LocatorHelper.normalize(healedSelector));
            }
        }

        String pageName = field.getDeclaringClass().getSimpleName();
        AutoHealElementLocator locator =
                new AutoHealElementLocator(page, selectors, field.getName(), pageName);
        Class<?> fieldType = field.getType();
        if (fieldType == Locator.class) {
            return locatorProxy(locator, field.getType().getClassLoader());
        }
        if (List.class.isAssignableFrom(fieldType)
                && fieldType.isAssignableFrom(AbstractList.class) && isLocatorList(field)) {
            return locatorList(locator);
        }
        throw new IllegalArgumentException("@AutoHealBy supports Locator and List<Locator> fields only: "
                + field.getDeclaringClass().getName() + "." + field.getName());
    }

    private static boolean isLocatorList(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }
        Type[] arguments = ((ParameterizedType) genericType).getActualTypeArguments();
        return arguments.length == 1 && arguments[0] == Locator.class;
    }

    private static Locator locatorProxy(AutoHealElementLocator locator, ClassLoader loader) {
        InvocationHandler handler = new AutoHealInvocationHandler(locator);
        return (Locator) Proxy.newProxyInstance(loader, new Class<?>[]{Locator.class}, handler);
    }

    private static List<Locator> locatorList(AutoHealElementLocator locator) {
        return new AbstractList<Locator>() {
            @Override
            public Locator get(int index) {
                int count = size();
                if (index < 0 || index >= count) {
                    throw new IndexOutOfBoundsException("Index " + index + ", size " + count);
                }
                return locatorProxy(locator.atIndex(index), Locator.class.getClassLoader());
            }

            @Override
            public int size() {
                return locator.count();
            }
        };
    }
}
