package wns.automation.core.selenium;

import java.time.Duration;
import java.util.Properties;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import wns.automation.core.constants.*;

import wns.automation.core.IWebActionManager;
import wns.automation.utilities.*;
import wns.automation.core.Reporter;

public class SeleniumWebActionManager implements IWebActionManager<WebElement> {

	private WebDriver driver;
	public WebDriverWait wait;
	private Properties props;
	public Reporter Reporter;
	
	public SeleniumWebActionManager()
	{
		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";
		props = TestUtility.getTestConfig(propertyFile);
		Reporter = new Reporter();
	}
	
	@Override
	public void InitializeContext(Browser browser, Long waitduration, TestExecutionMode executionMode,
			String remoteURL) {
		this.driver = TestUtility.getWebDriver(browser, executionMode, remoteURL);
		Dimension screenDimension = new Dimension(Integer.parseInt(props.getProperty("browserHeight")),Integer.parseInt(props.getProperty("browserWidth")));
		driver.manage().window().setSize(screenDimension);
		wait = new WebDriverWait(driver, Duration.ofSeconds(waitduration));
	}
	
	public void setWebDriver(WebDriver driver) {
		this.driver = driver;
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}

	public WebDriver getDriver() {
		return driver;
	}

	@Override
	public void openWebApp(String url) {
		System.out.println(url);
		driver.get(url);
	}

	@Override
	public void Click(WebElement element) {
		element.click();
	}

	@Override
	public void Input(WebElement element, String text) {
		element.clear();
		element.sendKeys(text);
	}

	@Override
	public boolean IsDisabled(WebElement element) {
		if (element.isEnabled())
			return false;
		return true;
	}

	@Override
	public boolean IsEnabled(WebElement element) {
		if (element.isEnabled())
			return true;
		return false;
	}

	@Override
	public boolean isDisplayed(WebElement element) {
		try {
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (TimeoutException ex) {
			return false;
		}
		return true;
	}

	@Override
	public boolean selectValueFromDropDown(WebElement element, String text) {
		try {
			Select selectControl = new Select(element);
			selectControl.selectByVisibleText(text);

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean getTextFromElement(WebElement element, String text) {
		try {
			String status = element.getText();
			if (status.equals(text))
				return true;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean waitForElementToBeVisible(WebElement element, int timeOut) {
		try {
			WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds((long)timeOut));
			wait.until(ExpectedConditions.visibilityOf(element));
			return true;
		} catch (TimeoutException var4) {
			return false;
		}
	}

	public boolean waitForElementToBeClickable(WebElement element, int timeOut) {
		try {
			WebDriverWait wait = new WebDriverWait(this.driver, Duration.ofSeconds((long)timeOut));
			wait.until(ExpectedConditions.elementToBeClickable(element));
			return true;
		} catch (TimeoutException var4) {
			return false;
		}
	}

	@Override
	public void Cleanup() {
		driver.quit();
	}
}