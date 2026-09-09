package wns.automation.core.selenium;

import wns.automation.utilities.TestUtility;

import java.util.Properties;

public class AutoHealConfig {

    private static AutoHealConfig instance;
    private final Properties props;

    private static final String PROPERTIES_PATH =
            System.getProperty("user.dir")
                    + "\\src\\test\\java\\testconfig\\test.properties";

    private AutoHealConfig() {
        this.props = TestUtility.getTestConfig(PROPERTIES_PATH);
        if (this.props == null) {
            throw new RuntimeException("[AUTO-HEAL] Could not load " + PROPERTIES_PATH);
        }
    }

    public static synchronized AutoHealConfig getInstance() {
        if (instance == null) {
            instance = new AutoHealConfig();
        }
        return instance;
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(props.getProperty("autoHealEnabled", "true"));
    }

    public boolean isScreenshotEnabled() {
        return Boolean.parseBoolean(props.getProperty("autoHealScreenshot", "false"));
    }

    public int getMaxRetries() {
        try {
            return Integer.parseInt(props.getProperty("autoHealMaxRetries", "3"));
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    public boolean isReportEnabled() {
        return Boolean.parseBoolean(props.getProperty("autoHealReport", "true"));
    }

    public int getRetryPrimary() {
        try {
            return Integer.parseInt(props.getProperty("autoHealRetryPrimary", "2"));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    public int getWaitTimeout() {
        try {
            return Integer.parseInt(props.getProperty("webDriverTimeDuraiton", "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}
