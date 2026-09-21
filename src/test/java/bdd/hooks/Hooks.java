package bdd.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public final class Hooks {
    private static Playwright playwright;
    private static Browser browser;
    private static Page page;

    @Before
    public void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(
                        Boolean.parseBoolean(System.getProperty("headless", "true"))));
        page = browser.newPage();
    }

    @After
    public void closeBrowser() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        page = null;
        browser = null;
        playwright = null;
    }

    public static Page page() {
        if (page == null) {
            throw new IllegalStateException("Playwright page is not initialized");
        }
        return page;
    }
}
