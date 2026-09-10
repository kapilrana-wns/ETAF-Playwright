package wns.automation.core.playwright;

import java.util.Properties;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.SelectOption;
import wns.automation.core.constants.*;

import wns.automation.core.IWebActionManager;
import wns.automation.utilities.*;
import wns.automation.core.Reporter;

public class PlaywrightWebActionManager implements IWebActionManager<Locator> {

	private Playwright driver;
	private BrowserType browserType; // = Playwright.chromium();
	public Page page;
	private Properties props;
	public Reporter Reporter;

	public PlaywrightWebActionManager() {
		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";
		props = TestUtility.getTestConfig(propertyFile);
		Reporter  = new Reporter();

	}

	public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
			String remoteURL) {

		this.driver = Playwright.create();
		try {
			switch (browser) {
			case FireFox: {
				this.browserType = driver.firefox();
				break;
			}
			default: {
				/*this.browserType = driver.chromium();

				this.page = browserType.launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(false))
									.newPage();*/
                this.browserType = driver.chromium();

                String browserName = props.getProperty("browser");

                BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(false);

                if ("edge".equalsIgnoreCase(browserName)) {

                    options.setChannel("msedge");

                } else {

                    options.setChannel("chrome");
                }

                this.page = browserType.launch(options).newPage();
				page.setViewportSize(Integer.parseInt(props.getProperty("browserWidth")),
						Integer.parseInt(props.getProperty("browserHeight")));
				break;
			}
			}
		} catch (Exception ex) {ex.printStackTrace();
		}
	}
	public void setWebDriver(Playwright driver, Page page) {
		this.driver = driver;
		this.page = page;
		// wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}
	public Page getDriver() {
        return page;
	}

	@Override
	public void openWebApp(String url) {
		try {
			page.navigate(url);
			//page.waitForSelector("id=Email");
            page.waitForLoadState();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void Click(Locator element) {
		element.click();
	}

	@Override
	public void Input(Locator element, String text) {
		element.fill("");
		// element.fill(text);
		element.type(text);
	}

	@Override
	public boolean isDisplayed(Locator element) {
		if (element.isVisible()) {
			return true;
		} else
			return false;
	}

	@Override
	public boolean selectValueFromDropDown(Locator element, String text) {
		// TODO Auto-generated method stub
		element.selectOption(new SelectOption().setLabel(text));
		return false;
	}

//	@Override
//	public File takeScreenShot(ITestResult Result) {
//		try {
//			Properties prop = (Properties) Result.getTestContext().getAttribute("props");
//			Path destination = Paths.get(prop.getProperty("testResultOutputDirectory") + Result.getName() + "_" + "screenshot.png");
//			byte[] screenshotfile = page.screenshot(new Page.ScreenshotOptions()
//					  .setPath(destination)
//					  .setFullPage(true));
//			
//			 FileOutputStream out = new FileOutputStream(destination.toString());
//			  out.write(screenshotfile);
//			  out.close();
//		  
//			return out.;
//		} catch (WebDriverException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}

	@Override
	public void Cleanup() {
		driver.close();
	}

	@Override
	public boolean IsDisabled(Locator element) {
		if (element.isEnabled())
			return false;
		return true;
	}

	@Override
	public boolean IsEnabled(Locator element) {
		if (element.isEnabled())
			return true;
		return false;
	}

	@Override
	public boolean getTextFromElement(Locator element, String text) {
		try {
			String status = element.textContent();
			// System.out.println(status);
			if (status.equals(text))
				return true;

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}