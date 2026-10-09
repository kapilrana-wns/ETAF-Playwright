package wns.automation.core.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoHealFieldDecoratorTest {

    private static String originalUserDir;
    private static Path temporaryDirectory;

    @BeforeAll
    static void useIsolatedWorkingDirectory() throws Exception {
        originalUserDir = System.getProperty("user.dir");
        temporaryDirectory = Files.createTempDirectory("auto-heal-test");
        System.setProperty("user.dir", temporaryDirectory.toString());
    }

    @AfterAll
    static void restoreWorkingDirectory() throws Exception {
        System.setProperty("user.dir", originalUserDir);
        Files.deleteIfExists(temporaryDirectory.resolve("healed-locators.json"));
        Files.deleteIfExists(temporaryDirectory);
    }

    @Test
    void decoratesFieldsHealsWithFallbackAndFailsWhenNoCandidateExists() {
        PrimaryPageObject primaryPageObject = new PrimaryPageObject();
        AutoHealPageFactory.initElements(
                mockPage(Map.of("css=#present", 1)), primaryPageObject);
        assertTrue(primaryPageObject.element.isVisible());

        TestPageObject pageObject = new TestPageObject();
        Page page = mockPage(Map.of("css=#recovered", 1));

        AutoHealPageFactory.initElements(page, pageObject);

        assertTrue(pageObject.element.isVisible());
        assertEquals("css=#recovered", HealedLocatorStore.getInstance()
                .getHealedLocator(TestPageObject.class.getSimpleName(), "element"));

        AutoHealElementLocator missing = new AutoHealElementLocator(
                mockPage(Map.of()), List.of("css=#missing"), "missing", "TestPageObject");
        assertThrows(PlaywrightException.class, missing::findLocator);
    }

    @Test
    void domSimilarityDoesNotRecoverFromTagNameAlone() {
        assertNull(DomSimilarityMatcher.findSimilarElement(
                mockPage(Map.of()), "css=input", "element", "TestPageObject"));
    }

    private static Page mockPage(Map<String, Integer> selectorCounts) {
        return (Page) Proxy.newProxyInstance(
                Page.class.getClassLoader(),
                new Class<?>[]{Page.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "locator":
                            String selector = (String) args[0];
                            int count = selectorCounts.getOrDefault(selector, 0);
                            return mockLocator(selector, count);
                        case "evaluate":
                            return List.of();
                        case "waitForTimeout":
                            return null;
                        case "toString":
                            return "Mock Playwright Page";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private static Locator mockLocator(String selector, int count) {
        return (Locator) Proxy.newProxyInstance(
                Locator.class.getClassLoader(),
                new Class<?>[]{Locator.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "waitFor":
                            if (count == 0) {
                                throw new TimeoutError("No element for selector " + selector);
                            }
                            return null;
                        case "count":
                            return count;
                        case "isVisible":
                            return count > 0;
                        case "toString":
                            return "Mock Playwright Locator: " + selector;
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        return null;
    }

    private static final class TestPageObject {
        @AutoHealBy(value = "css=#missing", alternatives = "css=#recovered")
        private Locator element;
    }

    private static final class PrimaryPageObject {
        @AutoHealBy("css=#present")
        private Locator element;
    }
}
