package wns.automation.core.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import wns.automation.core.CoreTestManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Resolves a locator using an explicit primary selector followed by safe,
 * element-specific fallback selectors.
 */
public final class AutoHealLocator {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private AutoHealLocator() {
    }

    public static Locator resolve(Page page, Properties properties, String elementName,
                                  String primarySelector, String... fallbackSelectors) {
        Locator primary = page.locator(primarySelector);
        if (!isEnabled(properties)) {
            return primary;
        }

        int primaryRetries = intProperty(properties, "autoHealRetryPrimary", 1);
        int maxRetries = Math.max(primaryRetries, intProperty(properties, "autoHealMaxRetries", 3));
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            Locator candidate = attempt < primaryRetries
                    ? primary
                    : fallbackLocator(page, fallbackSelectors, attempt - primaryRetries);
            if (candidate == null) {
                break;
            }
            try {
                if (candidate.count() == 0) {
                    continue;
                }
                if (attempt >= primaryRetries) {
                    recordHealing(page, properties, elementName, primarySelector,
                            fallbackSelectors[attempt - primaryRetries]);
                }
                return candidate;
            } catch (RuntimeException error) {
                if (attempt == maxRetries - 1) {
                    throw error;
                }
            }
        }
        return primary;
    }

    private static Locator fallbackLocator(Page page, String[] selectors, int index) {
        return index >= 0 && index < selectors.length
                ? page.locator(selectors[index])
                : null;
    }

    private static void recordHealing(Page page, Properties properties, String elementName,
                                      String primary, String replacement) {
        String message = "Auto-healed locator [" + elementName + "] from ["
                + primary + "] to [" + replacement + "]";
        System.out.println(message);
        if (!Boolean.parseBoolean(properties.getProperty("autoHealReport", "true"))) {
            return;
        }

        try {
            Path directory = healingDirectory();
            Files.createDirectories(directory);
            String timestamp = LocalDateTime.now().format(FILE_TIME);
            if (Boolean.parseBoolean(properties.getProperty("autoHealScreenshot", "true"))) {
                Path screenshot = directory.resolve(elementName + "_" + timestamp + ".png");
                page.screenshot(new Page.ScreenshotOptions().setPath(screenshot).setFullPage(true));
            }
            Files.writeString(directory.resolve("healing.log"),
                    timestamp + " " + message + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException | RuntimeException error) {
            System.err.println("Auto-heal artifact creation failed: " + error.getMessage());
        }
    }

    private static Path healingDirectory() {
        String runDirectory = CoreTestManager.reportDirectpath;
        if (runDirectory == null || runDirectory.trim().isEmpty()) {
            runDirectory = Paths.get("auto-heal-screenshots").toString();
        } else {
            runDirectory = Paths.get(runDirectory, "auto-heal-screenshots").toString();
        }
        return Paths.get(runDirectory);
    }

    private static boolean isEnabled(Properties properties) {
        return Boolean.parseBoolean(properties.getProperty("autoHealEnabled", "false"));
    }

    private static int intProperty(Properties properties, String key, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(properties.getProperty(key,
                    String.valueOf(fallback))));
        } catch (NumberFormatException error) {
            return fallback;
        }
    }

}
