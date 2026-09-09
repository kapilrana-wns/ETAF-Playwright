package pageobject.Playwright;

import com.microsoft.playwright.Page;

public class AutoHealPage {

    private final Page page;

    public AutoHealPage(Page page) {
        this.page = page;
    }

    public void login(String username, String password) {
        page.locator("input[name='username']").fill(username);
        page.locator("input[name='password']").fill(password);
        page.locator("button[type='submit']").click();
    }
}