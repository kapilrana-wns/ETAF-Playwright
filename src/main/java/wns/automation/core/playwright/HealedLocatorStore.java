package wns.automation.core.playwright;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class HealedLocatorStore {

    private static final Path FILE_PATH =
            Paths.get(System.getProperty("user.dir"), "healed-locators.json");

    private static HealedLocatorStore instance;

    private final ConcurrentHashMap<String, String> healedLocators = new ConcurrentHashMap<>();

    private HealedLocatorStore() {
        load();
    }

    public static synchronized HealedLocatorStore getInstance() {
        if (instance == null) {
            instance = new HealedLocatorStore();
        }
        return instance;
    }

    private void load() {
        if (!Files.exists(FILE_PATH)) return;

        try (java.io.Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            JSONObject json = new JSONObject(new JSONTokener(reader));
            for (String key : json.keySet()) {
                healedLocators.put(key, json.getString(key));
            }
            System.out.println("[AUTO-HEAL] Loaded " + healedLocators.size()
                    + " healed locators from " + FILE_PATH);
        } catch (IOException | JSONException e) {
            System.err.println("[AUTO-HEAL] Could not load healed locators from "
                    + FILE_PATH + ": " + e.getMessage());
        }
    }

    public synchronized void save() {
        try {
            JSONObject json = new JSONObject();
            for (java.util.Map.Entry<String, String> entry : healedLocators.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }

            Files.writeString(FILE_PATH, json.toString(2), StandardCharsets.UTF_8);
        } catch (IOException | JSONException e) {
            throw new IllegalStateException(
                    "[AUTO-HEAL] Could not save healed locators to " + FILE_PATH, e);
        }
    }

    public String getHealedLocator(String pageName, String fieldName) {
        return healedLocators.get(key(pageName, fieldName));
    }

    public void setHealedLocator(String pageName, String fieldName, String byString) {
        if (byString == null || byString.trim().isEmpty()) {
            throw new IllegalArgumentException("Healed selector must not be blank");
        }
        String k = key(pageName, fieldName);
        String existing = healedLocators.get(k);
        if (!byString.equals(existing)) {
            healedLocators.put(k, byString);
            save();
        }
    }

    private static String key(String pageName, String fieldName) {
        return pageName + "." + fieldName;
    }
}
