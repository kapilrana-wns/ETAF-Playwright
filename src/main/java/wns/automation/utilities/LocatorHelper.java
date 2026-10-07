package wns.automation.utilities;

import java.util.ArrayList;
import java.util.List;

public class LocatorHelper {

    public static List<String> generateAlternateLocators(String selector) {
        String normalized = normalize(selector);
        List<String> alternatives = new ArrayList<>();

        if (normalized.startsWith("xpath=")) {
            String xpath = normalized.substring("xpath=".length());
            String tag = xpath.replaceFirst("^.*//([a-zA-Z][\\w-]*).*$", "$1");
            if (!tag.equals(xpath) && !xpath.contains("|")) {
                alternatives.add("xpath=" + xpath.replaceFirst("//" + tag, "//*"));
            }
        } else if (normalized.startsWith("css=")) {
            String css = normalized.substring("css=".length());
            if (css.matches("#[\\w-]+")) {
                String id = css.substring(1);
                alternatives.add("[id='" + id + "']");
                alternatives.add("xpath=//*[@id='" + id + "']");
            } else if (css.matches("\\.[\\w-]+")) {
                String className = css.substring(1);
                alternatives.add("[class~='" + className + "']");
                alternatives.add("xpath=//*[contains(concat(' ', normalize-space(@class), ' '), ' "
                        + className + " ')]");
            }
        }
        return alternatives;
    }

    public static String normalize(String selector) {
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("Auto-heal selectors must not be blank");
        }
        String value = selector.trim();
        if (value.startsWith("By.xpath:")) {
            return "xpath=" + value.substring("By.xpath:".length()).trim();
        }
        if (value.startsWith("By.cssSelector:")) {
            return "css=" + value.substring("By.cssSelector:".length()).trim();
        }
        if (value.startsWith("By.id:")) {
            return "css=#" + value.substring("By.id:".length()).trim();
        }
        if (value.startsWith("By.name:")) {
            return "css=[name='" + value.substring("By.name:".length()).trim() + "']";
        }
        if (value.startsWith("By.className:")) {
            return "css=." + value.substring("By.className:".length()).trim();
        }
        if (value.startsWith("By.tagName:")) {
            return "css=" + value.substring("By.tagName:".length()).trim();
        }
        if (value.startsWith("By.linkText:")) {
            return "text=" + value.substring("By.linkText:".length()).trim();
        }
        if (value.startsWith("By.partialLinkText:")) {
            return "text=" + value.substring("By.partialLinkText:".length()).trim();
        }
        if (value.startsWith("css=") || value.startsWith("xpath=") || value.startsWith("text=")) {
            return value;
        }
        return value.startsWith("/") || value.startsWith(".//") || value.startsWith("..")
                ? "xpath=" + value : "css=" + value;
    }
}