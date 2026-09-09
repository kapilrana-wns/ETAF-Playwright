package wns.automation.utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.ArrayList;
import java.util.List;

public class LocatorHelper {

    private static How resolveHow(FindBy annotation) {
        How how = annotation.how();
        if (how == How.UNSET) {
            if (!annotation.id().isEmpty()) return How.ID;
            if (!annotation.name().isEmpty()) return How.NAME;
            if (!annotation.css().isEmpty()) return How.CSS;
            if (!annotation.xpath().isEmpty()) return How.XPATH;
            if (!annotation.className().isEmpty()) return How.CLASS_NAME;
            if (!annotation.tagName().isEmpty()) return How.TAG_NAME;
            if (!annotation.linkText().isEmpty()) return How.LINK_TEXT;
            if (!annotation.partialLinkText().isEmpty()) return How.PARTIAL_LINK_TEXT;
        }
        return how;
    }

    private static String resolveUsing(FindBy annotation) {
        How how = annotation.how();
        if (how != How.UNSET) {
            return annotation.using();
        }
        switch (resolveHow(annotation)) {
            case ID: return annotation.id();
            case NAME: return annotation.name();
            case CSS: return annotation.css();
            case XPATH: return annotation.xpath();
            case CLASS_NAME: return annotation.className();
            case TAG_NAME: return annotation.tagName();
            case LINK_TEXT: return annotation.linkText();
            case PARTIAL_LINK_TEXT: return annotation.partialLinkText();
            default: return annotation.using();
        }
    }

    public static By buildByFromFindBy(FindBy annotation) {
        How how = resolveHow(annotation);
        String using = resolveUsing(annotation);

        switch (how) {
            case ID: return By.id(using);
            case NAME: return By.name(using);
            case CLASS_NAME: return By.className(using);
            case CSS: return By.cssSelector(using);
            case XPATH: return By.xpath(using);
            case LINK_TEXT: return By.linkText(using);
            case PARTIAL_LINK_TEXT: return By.partialLinkText(using);
            case TAG_NAME: return By.tagName(using);
            default: return By.xpath(using);
        }
    }

    public static List<By> generateAlternateLocators(FindBy annotation) {
        How how = resolveHow(annotation);
        String using = resolveUsing(annotation);
        List<By> alternatives = new ArrayList<>();

        switch (how) {
            case ID:
                alternatives.add(By.name(using));
                alternatives.add(By.cssSelector("#" + using));
                alternatives.add(By.xpath("//*[@id='" + using + "']"));
                alternatives.add(By.cssSelector("input[id='" + using + "']"));
                alternatives.add(By.xpath("//*[contains(@id,'" + using + "')]"));
                break;

            case NAME:
                alternatives.add(By.id(using));
                alternatives.add(By.cssSelector("[name='" + using + "']"));
                alternatives.add(By.xpath("//*[@name='" + using + "']"));
                alternatives.add(By.xpath("//*[contains(@name,'" + using + "')]"));
                break;

            case CLASS_NAME:
                alternatives.add(By.cssSelector("." + using));
                alternatives.add(By.xpath("//*[contains(@class,'" + using + "')]"));
                break;

            case TAG_NAME:
                alternatives.add(By.cssSelector(using));
                alternatives.add(By.xpath("//" + using));
                alternatives.add(By.xpath("//*[contains(name(),'" + using + "')]"));
                break;

            case LINK_TEXT:
                alternatives.add(By.partialLinkText(using));
                alternatives.add(By.xpath("//a[contains(text(),'" + using + "')]"));
                break;

            case PARTIAL_LINK_TEXT:
                alternatives.add(By.linkText(using));
                alternatives.add(By.xpath("//a[contains(text(),'" + using + "')]"));
                break;

            case CSS:
                alternatives.add(By.xpath(toXPath(using)));
                break;

            case XPATH:
                // Try as CSS if the XPath is simple
                if (!using.contains("|") && !using.contains("(")) {
                    String css = using
                            .replaceAll("^//", "")
                            .replaceAll("/", " > ")
                            .replaceAll("\\[@([^=]+)='([^']+)'\\]", "[$1='$2']")
                            .replaceAll("\\[contains\\(@([^=]+),'([^']+)'\\)\\]", "[$1*='$2']")
                            .replaceAll("\\[@([^=]+)='([^']+)'\\]/", "[$1='$2'] > ");
                    if (!css.isEmpty() && !css.equals(using)) {
                        alternatives.add(By.cssSelector(css));
                    }
                }
                // Try tag-agnostic version
                alternatives.add(By.xpath(using.replaceFirst("^//[a-zA-Z0-9]+", "//*")));
                break;
        }

        return alternatives;
    }

    private static String toXPath(String cssSelector) {
        return cssSelector
                .replaceAll("input\\[type='([^']+)'\\]", "//input[@type='$1']")
                .replaceAll("#([a-zA-Z][\\w-]*)", "//*[@id='$1']")
                .replaceAll("\\.([a-zA-Z][\\w-]*)", "//*[contains(@class,'$1')]");
    }
}