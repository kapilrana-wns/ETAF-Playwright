package wns.automation.core.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.JavascriptExecutor;
import com.aventstack.extentreports.Status;
import wns.automation.core.ExtentTestManager;

import java.util.*;

public class DomSimilarityMatcher {

    private static final int MATCH_THRESHOLD = 40;

    public static class DomMatchResult {
        public final WebElement element;
        public final By matchedBy;
        public DomMatchResult(WebElement element, By matchedBy) {
            this.element = element;
            this.matchedBy = matchedBy;
        }
    }

    public static DomMatchResult findSimilarElement(WebDriver driver, By failedBy,
                                                  String fieldName, String pageName) {
        SearchHints hints = parseLocator(failedBy);
        if (hints == null) {
            System.out.println("[AUTO-HEAL DOM] parseLocator returned null for: " + failedBy);
            return null;
        }
        System.out.println("[AUTO-HEAL DOM] Parsed hints - id: " + hints.id + ", name: " + hints.name + ", tag: " + hints.tag);

        List<Map<String, String>> candidates = fetchAllElementAttrs(driver);
        System.out.println("[AUTO-HEAL DOM] Found " + candidates.size() + " candidates in DOM");
        if (candidates.isEmpty()) return null;

        Map<String, String> bestMatch = null;
        int bestScore = 0;

        for (Map<String, String> attrs : candidates) {
            int score = scoreElement(attrs, hints);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = attrs;
            }
        }

        System.out.println("[AUTO-HEAL DOM] Best score: " + bestScore + " (threshold: " + MATCH_THRESHOLD + ")");
        if (bestMatch != null) {
            System.out.println("[AUTO-HEAL DOM] Best match - tag: " + bestMatch.get("tag") + ", id: " + bestMatch.get("id") + ", name: " + bestMatch.get("name"));
        }

        if (bestMatch == null || bestScore < MATCH_THRESHOLD) return null;

        try {
            String xpath = bestMatch.get("xpath");
            WebElement element = driver.findElement(By.xpath(xpath));
            System.out.println();
            System.out.println("[AUTO-HEAL DOM MATCH]");
            System.out.println("Element        : " + fieldName);
            System.out.println("Primary Locator: " + failedBy);
            System.out.println("Page           : " + pageName);
            System.out.println("Match Score    : " + bestScore + "%");
            System.out.println("Matched Tag    : " + bestMatch.get("tag"));
            System.out.println("Matched ID     : " + bestMatch.get("id"));
            System.out.println("Matched Name   : " + bestMatch.get("name"));

            String tag = bestMatch.get("tag");
            String id = bestMatch.get("id");
            String matchedDetail = tag;
            if (id != null && !id.isEmpty()) matchedDetail += "#" + id;
            ExtentTestManager.log(Status.WARNING,
                    "<span style='color:#E67E22;'>\u26A0 AUTO-HEAL DOM MATCH</span>"
                    + "<br><b>Element:</b> " + fieldName
                    + "<br><b>Original:</b> " + failedBy
                    + "<br><b>Matched:</b> &lt;" + matchedDetail + "&gt;"
                    + "<br><b>Score:</b> " + bestScore + "%"
                    + "<br><b>Page:</b> " + pageName);

            AutoHealMetrics.getInstance().recordHeal(
                    pageName, fieldName, "DOM-Similarity", 0);
            return new DomMatchResult(element, By.xpath(xpath));
        } catch (Exception e) {
            System.out.println("[AUTO-HEAL DOM] Exception in findSimilarElement: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    private static SearchHints parseLocator(By by) {
        String str = by.toString();
        SearchHints hints = new SearchHints();

        int colon = str.indexOf(':');
        if (colon < 0) return null;
        String type = str.substring(0, colon).trim();
        String value = str.substring(colon + 1).trim();

        switch (type) {
            case "By.id":
                hints.id = value;
                break;
            case "By.name":
                hints.name = value;
                break;
            case "By.className":
                hints.cssClass = value;
                break;
            case "By.tagName":
                hints.tag = value;
                break;
            case "By.linkText":
            case "By.partialLinkText":
                hints.tag = "a";
                hints.text = value;
                break;
            case "By.cssSelector":
                parseCssSelector(value, hints);
                break;
            case "By.xpath":
                parseXPath(value, hints);
                break;
            default:
                return null;
        }
        hints.originalBy = str;
        return hints;
    }

    private static void parseCssSelector(String css, SearchHints hints) {
        if (css.matches("^[a-zA-Z]+.*")) {
            String[] parts = css.split("[\\[.#]");
            hints.tag = parts[0];
        }
        if (css.contains("#")) {
            hints.id = css.replaceAll(".*#([a-zA-Z][\\w-]*).*", "$1");
        }
        if (css.contains(".")) {
            hints.cssClass = css.replaceAll(".*\\.([a-zA-Z][\\w-]*).*", "$1");
        }
        if (css.contains("type=")) {
            hints.type = css.replaceAll(".*type='([^']+)'.*", "$1");
            if (hints.type.equals(css)) hints.type = null;
        }
        if (css.contains("name=")) {
            hints.name = css.replaceAll(".*name='([^']+)'.*", "$1");
            if (hints.name.equals(css)) hints.name = null;
        }
        if (css.contains("placeholder=")) {
            hints.placeholder = css.replaceAll(".*placeholder='([^']+)'.*", "$1");
            if (hints.placeholder.equals(css)) hints.placeholder = null;
        }
    }

    private static void parseXPath(String xpath, SearchHints hints) {
        if (xpath.matches(".*//[a-zA-Z]+\\[.*")) {
            String tag = xpath.replaceAll(".*//([a-zA-Z]+)\\[.*", "$1");
            if (!tag.equals(xpath)) hints.tag = tag;
        } else if (xpath.matches(".*//[a-zA-Z]+")) {
            hints.tag = xpath.replaceAll(".*//([a-zA-Z]+)", "$1");
        } else if (xpath.matches(".*//\\*")) {
            hints.tag = null;
        }

        if (xpath.contains("@id='")) {
            hints.id = xpath.replaceAll(".*@id='([^']+)'.*", "$1");
        } else if (xpath.contains("@id=")) {
            hints.id = xpath.replaceAll(".*@id=\"([^\"]+)\".*", "$1");
        }

        if (xpath.contains("@name='")) {
            hints.name = xpath.replaceAll(".*@name='([^']+)'.*", "$1");
        } else if (xpath.contains("@name=")) {
            hints.name = xpath.replaceAll(".*@name=\"([^\"]+)\".*", "$1");
        }

        if (xpath.contains("contains(text(),'")) {
            hints.text = xpath.replaceAll(".*contains\\(text\\(\\),'([^']+)'\\).*", "$1");
        } else if (xpath.contains("text()='")) {
            hints.text = xpath.replaceAll(".*text\\(\\)='([^']+)'.*", "$1");
        }

        if (xpath.contains("@class='")) {
            hints.cssClass = xpath.replaceAll(".*@class='([^']+)'.*", "$1");
        } else if (xpath.contains("contains(@class,'")) {
            hints.cssClass = xpath.replaceAll(".*contains\\(@class,'([^']+)'\\).*", "$1");
        }

        if (xpath.contains("@type='")) {
            hints.type = xpath.replaceAll(".*@type='([^']+)'.*", "$1");
        }

        if (xpath.contains("@placeholder='")) {
            hints.placeholder = xpath.replaceAll(".*@placeholder='([^']+)'.*", "$1");
        }

        if (xpath.contains("@aria-label='")) {
            hints.ariaLabel = xpath.replaceAll(".*@aria-label='([^']+)'.*", "$1");
        }
    }

    private static List<Map<String, String>> fetchAllElementAttrs(WebDriver driver) {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script =
                "var items = [];" +
                "var all = document.querySelectorAll('input, select, textarea, button, a, label, li, span, h1, h2, h3, h4, p, td, th, div[role], *[aria-label], *[data-testid]');" +
                "for (var i = 0; i < all.length; i++) {" +
                "  var el = all[i];" +
                "  var tag = el.tagName ? el.tagName.toLowerCase() : '';" +
                "  var attrs = {};" +
                "  attrs.tag = tag;" +
                "  attrs.id = el.id || '';" +
                "  attrs.name = el.getAttribute('name') || '';" +
                "  attrs['class'] = el.getAttribute('class') || '';" +
                "  attrs.type = el.getAttribute('type') || '';" +
                "  attrs.placeholder = el.getAttribute('placeholder') || '';" +
                "  attrs['aria-label'] = el.getAttribute('aria-label') || '';" +
                "  attrs.label = '';" +
                "  if (el.labels && el.labels.length > 0) {" +
                "    attrs.label = el.labels[0].textContent.trim();" +
                "  }" +
                "  attrs.text = (el.textContent || '').trim();" +
                "  var xpath = getXPath(el);" +
                "  attrs.xpath = xpath;" +
                "  items.push(attrs);" +
                "}" +
                "function getXPath(el) {" +
                "  if (el.id) return '//*[@id=\\\"' + el.id + '\\\"]';" +
                "  var parts = [];" +
                "  while (el && el.nodeType === 1) {" +
                "    var idx = 1;" +
                "    var sibling = el.previousSibling;" +
                "    while (sibling) {" +
                "      if (sibling.nodeType === 1 && sibling.tagName === el.tagName) idx++;" +
                "      sibling = sibling.previousSibling;" +
                "    }" +
                "    parts.unshift(el.tagName.toLowerCase() + '[' + idx + ']');" +
                "    el = el.parentNode;" +
                "  }" +
                "  return '/' + parts.join('/');" +
                "}" +
                "return items;";

            Object raw = js.executeScript(script);
            if (raw instanceof List) {
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) raw;
                for (Map<String, Object> item : rawList) {
                    Map<String, String> attrs = new HashMap<>();
                    for (Map.Entry<String, Object> entry : item.entrySet()) {
                        Object val = entry.getValue();
                        attrs.put(entry.getKey(), val != null ? val.toString() : "");
                    }
                    result.add(attrs);
                }
            }
        } catch (Exception e) {
            System.out.println("[AUTO-HEAL] DOM scan error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private static int scoreElement(Map<String, String> attrs, SearchHints hints) {
        int score = 0;
        int maxScore = 0;

        if (hints.tag != null) {
            maxScore += 15;
            if (hints.tag.equalsIgnoreCase(attrs.get("tag"))) {
                score += 15;
            }
        }

        if (hints.id != null && !hints.id.isEmpty()) {
            maxScore += 25;
            String attrId = strVal(attrs.get("id"));
            if (hints.id.equals(attrId)) score += 25;
            else if (!attrId.isEmpty() && (attrId.contains(hints.id) || hints.id.contains(attrId))) score += 18;
        }

        if (hints.name != null && !hints.name.isEmpty()) {
            maxScore += 20;
            String attrName = strVal(attrs.get("name"));
            if (hints.name.equals(attrName)) score += 20;
            else if (!attrName.isEmpty() && (attrName.contains(hints.name) || hints.name.contains(attrName))) score += 12;
        }

        if (hints.cssClass != null && !hints.cssClass.isEmpty()) {
            maxScore += 15;
            String attrClass = strVal(attrs.get("class"));
            if (attrClass.contains(hints.cssClass)) score += 15;
            else {
                boolean anyPart = false;
                for (String part : hints.cssClass.split("\\s+")) {
                    if (attrClass.contains(part)) anyPart = true;
                }
                if (anyPart) score += 8;
            }
        }

        if (hints.type != null && !hints.type.isEmpty()) {
            maxScore += 10;
            String attrType = strVal(attrs.get("type"));
            if (hints.type.equalsIgnoreCase(attrType)) score += 10;
        }

        if (hints.text != null && !hints.text.isEmpty()) {
            maxScore += 15;
            String attrText = strVal(attrs.get("text"));
            if (attrText.equals(hints.text)) score += 15;
            else if (!attrText.isEmpty() && (attrText.contains(hints.text) || hints.text.contains(attrText))) score += 10;
        }

        if (hints.placeholder != null && !hints.placeholder.isEmpty()) {
            maxScore += 15;
            String attrPlaceholder = strVal(attrs.get("placeholder"));
            if (hints.placeholder.equals(attrPlaceholder)) score += 15;
            else if (!attrPlaceholder.isEmpty()
                    && (attrPlaceholder.contains(hints.placeholder) || hints.placeholder.contains(attrPlaceholder))) {
                score += 8;
            }
        }

        if (hints.ariaLabel != null && !hints.ariaLabel.isEmpty()) {
            maxScore += 15;
            String attrAria = strVal(attrs.get("aria-label"));
            if (hints.ariaLabel.equals(attrAria)) score += 15;
            else if (!attrAria.isEmpty()
                    && (attrAria.contains(hints.ariaLabel) || hints.ariaLabel.contains(attrAria))) {
                score += 8;
            }
        }

        if (hints.label != null && !hints.label.isEmpty()) {
            maxScore += 10;
            String attrLabel = strVal(attrs.get("label"));
            if (hints.label.equals(attrLabel)) score += 10;
            else if (!attrLabel.isEmpty()
                    && (attrLabel.contains(hints.label) || hints.label.contains(attrLabel))) {
                score += 5;
            }
        }

        if (maxScore == 0) return 0;
        return score * 100 / maxScore;
    }

    private static String strVal(String s) {
        return s != null ? s : "";
    }

    private static class SearchHints {
        String tag;
        String id;
        String name;
        String text;
        String cssClass;
        String type;
        String placeholder;
        String ariaLabel;
        String label;
        String originalBy;
    }
}
