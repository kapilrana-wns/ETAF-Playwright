package wns.automation.core.playwright;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AutoHealConfig {

    private static AutoHealConfig instance;
    private final Properties props;

    private AutoHealConfig() {
        Path configPath = Paths.get(System.getProperty("user.dir"), "src", "test", "java",
                "testconfig", "test.properties");
        this.props = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                props.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("[AUTO-HEAL] Could not load " + configPath, e);
            }
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
        return positiveInt("autoHealMaxRetries", 3);
    }

    public boolean isReportEnabled() {
        return Boolean.parseBoolean(props.getProperty("autoHealReport", "true"));
    }

    public int getRetryPrimary() {
        return positiveInt("autoHealRetryPrimary", 2);
    }

    public int getWaitTimeout() {
        String configured = props.getProperty("autoHealWaitTimeout",
                props.getProperty("webDriverTimeDuraiton", "10"));
        return parsePositiveInt(configured, 10);
    }

    public String getProperty(String name, String defaultValue) {
        return props.getProperty(name, defaultValue);
    }

    private int positiveInt(String key, int defaultValue) {
        return parsePositiveInt(props.getProperty(key), defaultValue);
    }

    private static int parsePositiveInt(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
