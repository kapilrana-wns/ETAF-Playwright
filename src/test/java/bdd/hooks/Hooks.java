package bdd.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.Properties;

import wns.automation.core.BDDTestContext;
import wns.automation.core.CoreTestManager;
import wns.automation.utilities.TestUtility;
import wns.automation.connectors.Tools.Defect;


public class Hooks {

    public static WebDriver driver;
    Properties props;

    private static boolean cycleCreated = false; // ✅ prevent duplicate test cycles

    // ✅ BEFORE: Browser + Load Props + Jira Init
    @Before(order = 0)
    public void setup(Scenario scenario) {

        System.out.println("========== HOOK STARTED ==========");
        System.out.println("Scenario: " + scenario.getName());

        try {
            // 🔹 Step 1: Browser Setup
            System.out.println("Initializing WebDriver...");
            driver = new ChromeDriver();

            if (driver == null) {
                throw new RuntimeException("WebDriver initialization FAILED");
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            System.out.println("WebDriver initialized successfully");

            // 🔹 Step 2: Load properties
            String propPath = System.getProperty("user.dir") + "\\src\\test\\java\\testconfig\\test.properties";
            System.out.println("Loading properties from: " + propPath);

            props = TestUtility.getTestConfig(propPath);

            if (props == null) {
                throw new RuntimeException("Properties file NOT loaded");
            }

            System.out.println("Properties loaded successfully");

            // 🔹 Debug important values
            System.out.println("AutoLoggingDefect = " + props.getProperty("AutoLoggingDefect"));
            System.out.println("autoTestResultUpdate = " + props.getProperty("autoTestResultUpdate"));
            System.out.println("TestManagementTool = " + props.getProperty("TestManagementTool"));

            boolean autoLog = Boolean.parseBoolean(props.getProperty("AutoLoggingDefect"));
            boolean autoUpdate = Boolean.parseBoolean(props.getProperty("autoTestResultUpdate"));

            // 🔹 Step 3: Initialize Jira Connector
            if (autoLog || autoUpdate) {

                System.out.println("Initializing Test Management Connector...");

                BDDTestContext.initialize(props);

                if (BDDTestContext.getConnector() == null) {
                    throw new RuntimeException("Connector initialization FAILED");
                }

                System.out.println("Connector initialized successfully");

                // ✅ Create Test Cycle only once
                if (autoUpdate && !cycleCreated) {
                    try {
                        System.out.println("Creating Test Cycle...");
                        BDDTestContext.getConnector().createTestCycle();
                        cycleCreated = true;
                        System.out.println("Test Cycle Created SUCCESSFULLY");
                    } catch (Exception e) {
                        System.out.println("ERROR while creating Test Cycle:");
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("========== ERROR IN HOOK ==========");
            e.printStackTrace();

            // 🚨 VERY IMPORTANT: Fail fast so Cucumber doesn't give misleading errors
            throw new RuntimeException("Hook setup failed. Check logs above.", e);
        }

        System.out.println("========== HOOK COMPLETED ==========");
    }

    // ✅ AFTER: Update Result + Log Defect + Close Browser
    @After(order = 1)
    public void teardown(Scenario scenario) {

        try {

            if (BDDTestContext.getConnector() != null) {

                boolean autoLog = Boolean.parseBoolean(props.getProperty("AutoLoggingDefect"));
                boolean autoUpdate = Boolean.parseBoolean(props.getProperty("autoTestResultUpdate"));

                String scenarioName = scenario.getName();

                // ✅ Update Result
                if (autoUpdate) {

                    int statusCode = scenario.isFailed() ? 2 : 1;

                    BDDTestContext.getConnector().updateTestCaseResult(
                            scenarioName,
                            statusCode,
                            "Executed via BDD"
                    );
                }

                // ❌ Create Defect
                if (scenario.isFailed() && autoLog) {

                    Defect defect = new Defect();
                    defect.setDefectSummary("BDD Failure: " + scenarioName);
                    defect.setDefectDescription("Scenario failed: " + scenarioName);

                    BDDTestContext.getConnector().createDefect(defect);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Step 4: Close Browser
        if (driver != null) {
            driver.quit();
        }
    }
}