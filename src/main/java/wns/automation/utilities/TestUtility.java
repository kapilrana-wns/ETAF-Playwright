package wns.automation.utilities;

import com.microsoft.playwright.Page;
import org.testng.IAttributes;
import org.testng.ITestResult;
import wns.automation.connectors.Tools.AzureDevOpsConnector;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.connectors.Tools.JiraConnector;
import wns.automation.core.constants.TestManagementTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Shared framework utilities that are independent of the browser implementation.
 */
public final class TestUtility {

    private static Properties props;

    private TestUtility() {
    }

    public static Properties getProps() {
        return props;
    }

    public static void setProps(Properties properties) {
        props = properties;
    }

    public static Properties getTestConfig(String filename) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load test configuration: " + filename, exception);
        }
        return properties;
    }

    public static String getScreenShot(ITestResult result, Properties properties) throws IOException {
        Object browserPage = result.getAttribute("driver");
        if (!(browserPage instanceof Page)) {
            return "";
        }

        String outputDirectory = properties.getProperty("testResultOutputDirectory", "./test_result/");
        Path directory = Paths.get(outputDirectory);
        Files.createDirectories(directory);
        String fileName = result.getName() + "_" + System.currentTimeMillis() + ".png";
        Path destination = directory.resolve(fileName);
        ((Page) browserPage).screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));
        return destination.toString();
    }

    public static String getScreenShot(IAttributes result, Properties properties, String testName) throws IOException {
        Object browserPage = result.getAttribute("driver");
        if (!(browserPage instanceof Page)) {
            return "";
        }

        String outputDirectory = properties.getProperty("testResultOutputDirectory", "./test_result/");
        Path directory = Paths.get(outputDirectory);
        Files.createDirectories(directory);
        Path destination = directory.resolve(testName + "_" + System.currentTimeMillis() + ".png");
        ((Page) browserPage).screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));
        return destination.toString();
    }

    public static void waitUntilVisible(com.microsoft.playwright.Locator locator, int timeoutSeconds) {
        locator.waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setTimeout(timeoutSeconds * 1000L));
    }

    public static void sendMail(Properties properties) {
        System.out.println("Email notification is configured for the Playwright run.");
    }

    public static IToolsConnector getTestManagementToolConnector(Properties properties) {
        String configuredTool = properties.getProperty("TestManagementTool", "None");
        TestManagementTools tool = TestManagementTools.valueOf(configuredTool);
        return switch (tool) {
            case Jira -> JiraConnector.getInstance(properties);
            case AzureDevOPS -> AzureDevOpsConnector.getInstance(properties);
            case None -> null;
        };
    }
}
