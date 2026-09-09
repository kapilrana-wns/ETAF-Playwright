package wns.automation.core;

import java.time.Duration;
import wns.automation.core.constants.Browser;
import wns.automation.core.constants.TestExecutionMode;
import wns.automation.utilities.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CoreSeleniumTestManager extends CoreTestManager{

	protected WebDriver driver;
	private WebDriverWait wait;

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}


 	public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
			String remoteURL) {
		this.driver = TestUtility.getWebDriver(browser, executionMode, remoteURL);
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, Duration.ofSeconds(waitduration));

	}


}
