package wns.automation.core.playwright;

import com.microsoft.playwright.Page;

public final class AutoHealPageFactory {

    private AutoHealPageFactory() {
    }

    public static void initElements(Page page, Object pageObject) {
        new AutoHealFieldDecorator(page).decorate(pageObject);
    }
}
