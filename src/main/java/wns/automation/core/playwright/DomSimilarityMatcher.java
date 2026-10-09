package wns.automation.core.playwright;

import com.aventstack.extentreports.Status;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import wns.automation.core.ExtentTestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DomSimilarityMatcher {

    private static final int MATCH_THRESHOLD = 55;

    private DomSimilarityMatcher() {
    }

    public static final class DomMatchResult {
        public final Locator locator;
        public final String selector;

        private DomMatchResult(Locator locator, String selector) {
            this.locator = locator;
            this.selector = selector;
        }
    }

    public static DomMatchResult findSimilarElement(Page page, String failedSelector,
                                                     String fieldName, String pageName) {
        SearchHints hints = parseSelector(failedSelector);
        if (hints == null) {
            return null;
        }

        List<Map<String, String>> candidates = fetchAllElementAttrs(page);
        Map<String, String> bestMatch = null;
        int bestScore = 0;
        for (Map<String, String> candidate : candidates) {
            int score = scoreElement(candidate, hints);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }
        if (bestMatch == null || bestScore < MATCH_THRESHOLD) {
            return null;
        }

        String selector = "xpath=" + bestMatch.get("xpath");
        Locator locator = page.locator(selector);
        if (locator.count() == 0) {
            return null;
        }

        String tag = bestMatch.get("tag");
        String id = bestMatch.get("id");
        String matchedDetail = (id == null || id.isEmpty()) ? tag : tag + "#" + id;
        System.out.println("[AUTO-HEAL DOM MATCH] " + fieldName + " on " + pageName
                + " matched <" + matchedDetail + "> with score " + bestScore + "%");
        if (AutoHealConfig.getInstance().isReportEnabled()) {
            ExtentTestManager.log(Status.WARNING,
                    "<span style='color:#E67E22;'>AUTO-HEAL DOM MATCH</span>"
                            + "<br><b>Element:</b> " + fieldName
                            + "<br><b>Original:</b> " + failedSelector
                            + "<br><b>Matched:</b> &lt;" + matchedDetail + "&gt;"
                            + "<br><b>Score:</b> " + bestScore + "%"
                            + "<br><b>Page:</b> " + pageName);
        }
        return new DomMatchResult(locator, selector);
    }

    private static SearchHints parseSelector(String selector) {
        SearchHints hints = new SearchHints();
        String value = selector;
        if (value.startsWith("xpath=")) {
            value = value.substring("xpath=".length());
            parseXPath(value, hints);
        } else if (value.startsWith("css=")) {
            value = value.substring("css=".length());
            parseCssSelector(value, hints);
        } else {
            return null;
        }
        return hints.hasIdentifyingHints() ? hints : null;
    }

    private static void parseCssSelector(String css, SearchHints hints) {
        if (css.matches("^[a-zA-Z][\\w-]*(?:[.#\\[].*)?$")) {
            hints.tag = css.replaceFirst("^([a-zA-Z][\\w-]*).*$", "$1");
        }
        if (css.contains("#")) {
            hints.id = css.replaceFirst(".*#([a-zA-Z][\\w-]*).*", "$1");
        }
        if (css.contains(".")) {
            hints.cssClass = css.replaceFirst(".*\\.([a-zA-Z][\\w-]*).*", "$1");
        }
        hints.name = attribute(css, "name");
        hints.type = attribute(css, "type");
        hints.placeholder = attribute(css, "placeholder");
        hints.ariaLabel = attribute(css, "aria-label");
    }

    private static String attribute(String selector, String name) {
        String pattern = ".*\\[" + name + "=['\"]([^'\"]+)['\"]\\].*";
        String value = selector.replaceFirst(pattern, "$1");
        return value.equals(selector) ? null : value;
    }

    private static void parseXPath(String xpath, SearchHints hints) {
        java.util.regex.Matcher tag = java.util.regex.Pattern
                .compile("^//([a-zA-Z][\\w-]*)").matcher(xpath);
        if (tag.find()) hints.tag = tag.group(1);
        hints.id = xpathAttribute(xpath, "id");
        hints.name = xpathAttribute(xpath, "name");
        hints.cssClass = xpathAttribute(xpath, "class");
        hints.type = xpathAttribute(xpath, "type");
        hints.placeholder = xpathAttribute(xpath, "placeholder");
        hints.ariaLabel = xpathAttribute(xpath, "aria-label");
        java.util.regex.Matcher text = java.util.regex.Pattern
                .compile("(?:contains\\(text\\(\\),|text\\(\\)=)['\"]([^'\"]+)['\"]").matcher(xpath);
        if (text.find()) hints.text = text.group(1);
    }

    private static String xpathAttribute(String xpath, String name) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("@" + name + "\\s*=\\s*['\"]([^'\"]+)['\"]").matcher(xpath);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static List<Map<String, String>> fetchAllElementAttrs(Page page) {
        String script = "() => {"
                + "const items = [];"
                + "const all = document.querySelectorAll('input,select,textarea,button,a,label,li,span,h1,h2,h3,h4,p,td,th,[role],[aria-label],[data-testid]');"
                + "const xpathFor = (el) => {"
                + "const parts = [];"
                + "while (el && el.nodeType === 1) {"
                + "let index = 1, sibling = el.previousElementSibling;"
                + "while (sibling) { if (sibling.tagName === el.tagName) index++; sibling = sibling.previousElementSibling; }"
                + "parts.unshift(el.tagName.toLowerCase() + '[' + index + ']'); el = el.parentElement;"
                + "}"
                + "return '/' + parts.join('/');"
                + "};"
                + "for (const el of all) {"
                + "items.push({tag:el.tagName.toLowerCase(),id:el.id||'',name:el.getAttribute('name')||'',"
                + "class:el.getAttribute('class')||'',type:el.getAttribute('type')||'',"
                + "placeholder:el.getAttribute('placeholder')||'',"
                + "'aria-label':el.getAttribute('aria-label')||'',"
                + "label:el.labels&&el.labels.length?el.labels[0].textContent.trim():'',"
                + "text:(el.textContent||'').trim(),xpath:xpathFor(el)});"
                + "}"
                + "return items;"
                + "}";

        Object result = page.evaluate(script);
        if (!(result instanceof List<?>)) {
            return List.of();
        }
        List<Map<String, String>> candidates = new ArrayList<>();
        for (Object item : (List<?>) result) {
            if (!(item instanceof Map<?, ?>)) continue;
            Map<String, String> attributes = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                Object value = entry.getValue();
                attributes.put(String.valueOf(entry.getKey()), value == null ? "" : String.valueOf(value));
            }
            candidates.add(attributes);
        }
        return candidates;
    }

    private static int scoreElement(Map<String, String> attrs, SearchHints hints) {
        int score = 0;
        int maxScore = 0;

        if (hints.tag != null) {
            maxScore += 15;
            if (hints.tag.equalsIgnoreCase(attrs.get("tag"))) score += 15;
        }
        score += scoreAttribute(hints.id, attrs.get("id"), 25, 18);
        if (hints.id != null) maxScore += 25;
        score += scoreAttribute(hints.name, attrs.get("name"), 20, 12);
        if (hints.name != null) maxScore += 20;
        score += scoreAttribute(hints.cssClass, attrs.get("class"), 15, 8);
        if (hints.cssClass != null) maxScore += 15;
        score += scoreAttribute(hints.type, attrs.get("type"), 10, 0);
        if (hints.type != null) maxScore += 10;
        score += scoreAttribute(hints.text, attrs.get("text"), 15, 10);
        if (hints.text != null) maxScore += 15;
        score += scoreAttribute(hints.placeholder, attrs.get("placeholder"), 15, 8);
        if (hints.placeholder != null) maxScore += 15;
        score += scoreAttribute(hints.ariaLabel, attrs.get("aria-label"), 15, 8);
        if (hints.ariaLabel != null) maxScore += 15;
        score += scoreAttribute(hints.label, attrs.get("label"), 10, 5);
        if (hints.label != null) maxScore += 10;
        return maxScore == 0 ? 0 : score * 100 / maxScore;
    }

    private static int scoreAttribute(String expected, String actual, int exact, int partial) {
        if (expected == null || expected.isEmpty()) return 0;
        String value = actual == null ? "" : actual;
        if (expected.equals(value)) return exact;
        if (partial > 0 && !value.isEmpty()
                && (value.contains(expected) || expected.contains(value))) return partial;
        return 0;
    }

    private static final class SearchHints {
        String tag;
        String id;
        String name;
        String text;
        String cssClass;
        String type;
        String placeholder;
        String ariaLabel;
        String label;

        boolean hasIdentifyingHints() {
            return id != null || name != null || text != null || cssClass != null
                    || type != null || placeholder != null || ariaLabel != null;
        }
    }
}
