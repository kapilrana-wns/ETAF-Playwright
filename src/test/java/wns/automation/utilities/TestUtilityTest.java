package wns.automation.utilities;

import org.junit.jupiter.api.Test;
import wns.automation.core.CoreTestManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUtilityTest {

    @Test
    void reportBundleContainsOnlyTheCurrentRun() throws Exception {

        Path runDirectory = Files.createTempDirectory("etaf-report-");

        Path historicalFile = runDirectory.resolveSibling(runDirectory.getFileName() + "-previous.html");

        Files.writeString(runDirectory.resolve("Result.html"), "<html>current</html>");

        Files.createDirectories(runDirectory.resolve("screenshots"));

        Files.writeString(runDirectory.resolve("screenshots").resolve("failed.png"), "image");

        Files.writeString(historicalFile, "historical");

        Method createBundle = TestUtility.class.getDeclaredMethod("createReportBundle", Path.class);

        createBundle.setAccessible(true);

        Path bundle = (Path) createBundle.invoke(null, runDirectory);

        try (ZipFile zip = new ZipFile(bundle.toFile())) {

            Set<String> entries = zip.stream().map(entry -> entry.getName()).collect(Collectors.toSet());

            assertEquals(Set.of("Result.html", "screenshots/failed.png"), entries);

            assertTrue(entries.contains("Result.html"));

            assertTrue(entries.contains("screenshots/failed.png"));

            assertFalse(entries.contains(historicalFile.getFileName().toString()));

            assertFalse(entries.stream().anyMatch(name -> name.endsWith(".zip")));

        } finally {

            Files.deleteIfExists(bundle);
            Files.deleteIfExists(runDirectory.resolve("screenshots").resolve("failed.png"));
            Files.deleteIfExists(runDirectory.resolve("screenshots"));
            Files.deleteIfExists(runDirectory.resolve("Result.html"));
            Files.deleteIfExists(runDirectory);
            Files.deleteIfExists(historicalFile);
        }
    }

    @Test
    void getTestConfigLoadsPropertiesSuccessfully() throws Exception {

        Path tempFile = Files.createTempFile("test-config", ".properties");

        Files.writeString(tempFile, "browser=chromium\n" + "email=test@company.com\n" + "environment=QA");

        Properties properties = TestUtility.getTestConfig(tempFile.toString());

        assertEquals("chromium", properties.getProperty("browser"));

        assertEquals("test@company.com", properties.getProperty("email"));

        assertEquals("QA", properties.getProperty("environment"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void requiredPropertyReturnsConfiguredValue() throws Exception {

        Properties properties = new Properties();

        properties.setProperty("emailAddress", "automation@company.com");

        Method method = TestUtility.class.getDeclaredMethod("requiredProperty", Properties.class, String.class);

        method.setAccessible(true);

        String value = (String) method.invoke(null, properties, "emailAddress");

        assertEquals("automation@company.com", value);
    }

    @Test
    void requiredPropertyThrowsExceptionWhenPropertyMissing()
            throws Exception {Properties properties = new Properties();

        Method method = TestUtility.class.getDeclaredMethod("requiredProperty", Properties.class, String.class);

        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () -> method.invoke(null, properties, "emailAddress"));

        assertTrue(exception.getCause() instanceof IllegalStateException);

        assertTrue(exception.getCause().getMessage().contains("Missing required email property"));
    }

    @Test
    void formatDurationFormatsCorrectly() throws Exception {

        Method method = TestUtility.class.getDeclaredMethod("formatDuration", long.class);

        method.setAccessible(true);

        String duration = (String) method.invoke(null, 3661000L);

        assertEquals("1h 1m 1s", duration);
    }

    @Test
    void formatDurationReturnsZeroForZeroMilliseconds()
            throws Exception {

        Method method = TestUtility.class.getDeclaredMethod("formatDuration", long.class);

        method.setAccessible(true);

        String duration = (String) method.invoke(null, 0L);

        assertEquals("0h 0m 0s", duration);
    }

    @Test
    void getCurrentReportDirectoryUsesCoreTestManagerPath()
            throws Exception {

        CoreTestManager.reportDirectpath = "target/current-run-report";

        Properties properties = new Properties();

        Method method = TestUtility.class.getDeclaredMethod("getCurrentReportDirectory", Properties.class);

        method.setAccessible(true);

        String directory = (String) method.invoke(null, properties);

        assertEquals("target/current-run-report", directory);
    }

    @Test
    void getCurrentReportDirectoryFallsBackToProperties()
            throws Exception {

        CoreTestManager.reportDirectpath = null;

        Properties properties = new Properties();

        properties.setProperty("testResultOutputDirectory", "./test-results");

        Method method = TestUtility.class.getDeclaredMethod("getCurrentReportDirectory", Properties.class);

        method.setAccessible(true);

        String directory = (String) method.invoke(null, properties);

        assertEquals("./test-results", directory);
    }

    @Test
    void getCurrentReportDirectoryUsesDefaultValueWhenPropertyMissing()
            throws Exception {

        CoreTestManager.reportDirectpath = null;

        Properties properties = new Properties();

        Method method = TestUtility.class.getDeclaredMethod("getCurrentReportDirectory", Properties.class);

        method.setAccessible(true);

        String directory = (String) method.invoke(null, properties);

        assertEquals("./test_result/", directory);
    }

    @Test
    void valueOrDefaultReturnsOriginalValue()
            throws Exception {

        Method method = TestUtility.class.getDeclaredMethod("valueOrDefault", String.class, String.class);

        method.setAccessible(true);

        String result = (String) method.invoke(null, "Chromium", "Fallback");

        assertEquals("Chromium", result);
    }

    @Test
    void valueOrDefaultReturnsFallbackForNull()
            throws Exception {

        Method method = TestUtility.class.getDeclaredMethod("valueOrDefault", String.class, String.class);

        method.setAccessible(true);

        String result = (String) method.invoke(null, null, "Fallback");

        assertEquals("Fallback", result);
    }

    @Test
    void getTestManagementToolConnectorReturnsNullForNone() {

        Properties properties = new Properties();

        properties.setProperty("TestManagementTool", "None");

        assertEquals(null, TestUtility.getTestManagementToolConnector(properties));
    }

    @Test
    void getPropsAndSetPropsWorkCorrectly() {

        Properties properties = new Properties();

        properties.setProperty("browser", "chromium");

        TestUtility.setProps(properties);

        Properties result = TestUtility.getProps();

        assertNotNull(result);

        assertEquals("chromium", result.getProperty("browser"));
    }
}