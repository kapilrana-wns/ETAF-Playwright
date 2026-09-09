package wns.automation.core.selenium;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class HealedLocatorStore {

    private static final String FILE_NAME =
            System.getProperty("user.dir") + "/healed-locators.json";

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
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JSONObject json = new JSONObject(new JSONTokener(reader));
            for (String key : json.keySet()) {
                healedLocators.put(key, json.getString(key));
            }
            System.out.println("[AUTO-HEAL] Loaded " + healedLocators.size()
                    + " healed locators from " + FILE_NAME);
        } catch (Exception e) {
            System.out.println("[AUTO-HEAL] Could not load healed locators: " + e.getMessage());
        }
    }

    public synchronized void save() {
        try {
            JSONObject json = new JSONObject();
            for (java.util.Map.Entry<String, String> entry : healedLocators.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }

            try (FileWriter writer = new FileWriter(FILE_NAME)) {
                writer.write(json.toString(2));
            }
        } catch (Exception e) {
            System.out.println("[AUTO-HEAL] Could not save healed locators: " + e.getMessage());
        }
    }

    public String getHealedLocator(String pageName, String fieldName) {
        return healedLocators.get(key(pageName, fieldName));
    }

    public void setHealedLocator(String pageName, String fieldName, String byString) {
        if (byString == null || byString.trim().isEmpty()) return;
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
