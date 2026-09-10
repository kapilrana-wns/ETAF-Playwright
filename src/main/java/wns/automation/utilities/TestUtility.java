package wns.automation.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import com.microsoft.playwright.Locator;
import org.openqa.selenium.support.ui.WebDriverWait;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.connectors.Tools.JiraConnector;
import wns.automation.connectors.Tools.AzureDevOpsConnector; // if exists

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.ITestResult;
import com.microsoft.playwright.Page;
import io.github.bonigarcia.wdm.WebDriverManager;
import wns.automation.core.CoreTestManager;
import wns.automation.core.constants.AutomationTool;
import wns.automation.core.constants.Browser;
import wns.automation.core.constants.DBMSTYPE;
import wns.automation.core.constants.TestExecutionMode;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.testng.Assert;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class TestUtility {

	private static Logger logger = LogManager.getLogger(TestUtility.class.getName());
	private static Properties props;

	public static Properties getProps() {
		return props;
	}

	public static void setProps(Properties properties) {
		props = properties;
	}

	public static WebDriver getWebDriver(String browserName) {
		return WebDriverManager.getInstance(browserName).create();
	}

	// Method to Mouse over on the given object
	public static void mousehoverwithElement(WebElement st1, WebDriver driver) {
		Actions a = new Actions(driver);
		WebElement we = st1;
		a.moveToElement(we).build().perform();
	}

	// Custom Method to check if given element is visible until the give time expires
	public static void waitUntilVisible(WebElement element, int timeOutInSec, WebDriver driver) {
		try {
			FluentWait<WebDriver> fWait = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(timeOutInSec))
					.pollingEvery(Duration.ofMillis(50))
					.ignoring(org.openqa.selenium.NoSuchElementException.class)
					.ignoring(StaleElementReferenceException.class);
			fWait.until(ExpectedConditions.visibilityOf(element));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			throw new RuntimeException("element is either not displayed or enabled");
		}
	}

	// Wait for an element until it is clickable
	public static void waitUntilClickable(WebElement element, int timeOutInSec, WebDriver driver) {
		try {
			FluentWait<WebDriver> fWait = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(timeOutInSec))
					.pollingEvery(Duration.ofMillis(50))
					.ignoring(NoSuchElementException.class)
					.ignoring(StaleElementReferenceException.class);
			fWait.until(ExpectedConditions.elementToBeClickable(element));
		} catch (Exception e) {
			throw new RuntimeException("element is either not displayed or enabled " + e.getMessage());
		}
	}

	// Wait for an element until it is invisible
	public static void waitForNotVisible(WebElement element, int timeOutInSec, WebDriver driver) {
		try {
			FluentWait<WebDriver> fWait = new FluentWait<WebDriver>(driver)
					.withTimeout(Duration.ofSeconds(timeOutInSec))
					.pollingEvery(Duration.ofMillis(50));
			fWait.until(ExpectedConditions.invisibilityOf(element));
		} catch (org.openqa.selenium.NoSuchElementException ignored) {
		} catch (org.openqa.selenium.TimeoutException e) {
			throw new RuntimeException("element is either not displayed or enabled");
		}
	}

	// Method to return a value for a given attribute
	public String getAttribute(String attribute, WebElement we) {
		String attributeVal = null;
		attributeVal = we.getAttribute(attribute);
		return attributeVal;
	}

	// Method to scroll to a particular element in a page
	public static void scrollTo(WebElement we, WebDriver driver) {
		try {
			JavascriptExecutor jse = (JavascriptExecutor) driver;
			jse.executeScript("arguments[0].scrollIntoView()", we);

		} catch (Exception e) {

		}
	}

	// Method for page scrolling
	public void pagescroll(int i, WebDriver driver) {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("window.scrollBy(0," + i + ")", "");
	}

	// Create Pie Chart Method
	public static String generatePieChart() {
		try {
			DefaultPieDataset dataset = new DefaultPieDataset();

			dataset.setValue("Passed", TestResultListener.passed);
			dataset.setValue("Failed", TestResultListener.failed);
			dataset.setValue("Skipped", TestResultListener.skipped);

			JFreeChart chart = ChartFactory.createPieChart(
					"Test Execution Summary",
					dataset,
					true,
					true,
					false
			);

			String chartPath = System.getProperty("user.dir") + "/test_result/chart.png";
			ChartUtils.saveChartAsPNG(new File(chartPath), chart, 500, 400);

			return chartPath;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static double passPercent = (TestResultListener.total == 0) ? 0 :
			((double) TestResultListener.passed / TestResultListener.total) * 100;

	static String passPercentStr = String.format("%.2f", passPercent);

	// Method to switch to a new tab
	public static void switchTab(WebDriver driver, int tabNumber) {
		ArrayList<String> newTb = new ArrayList<String>(driver.getWindowHandles());
		// switch to new tab
		driver.switchTo().window(newTb.get(tabNumber));
		System.out.println("Page title of new tab: " + driver.getTitle());
	}

	// Method to verify if given text is present in the popup window
	public static boolean handlingAlert(String expectText, WebDriver driver) {
		Alert alert = driver.switchTo().alert();
		boolean status = false;
		// Capturing alert message.
		String alertMessage = driver.switchTo().alert().getText().toUpperCase();
		System.out.println(alertMessage);
		if (alertMessage.contains(expectText.toUpperCase())) {

			alert.accept();
			status = true;
		} else {
			alert.accept();
			status = false;

		}
		return status;
	}

	// Assertion related methods
	public void validateCondition(boolean actual, boolean expected) {
		Assert.assertEquals(actual, expected);
	}

	public void validateText(String actual, String expected, String msg) {
		Assert.assertEquals(actual, expected, msg);
	}

	public static void validateIfTrue(boolean actual) {
		Assert.assertTrue(actual);
	}

	public static void validateIfTrue(boolean actual, String message) {
		Assert.assertTrue(actual, message);
	}

	public void validateNotEquals(boolean actual, boolean expected) {
		Assert.assertNotEquals(actual, expected);
	}

	public void validateNotEquals(String actual, String expected, String msg) {
		Assert.assertNotEquals(actual, expected, msg);
	}

	public void validateIfFalse(boolean actual) {
		Assert.assertFalse(actual);
	}

	public void validateEquals(String actual, String expected) {
		Assert.assertEquals(actual, expected);
	}

	public void validateElement(WebElement we) {
		Assert.assertTrue(we.isDisplayed(), we.toString() + " element is not displaying");
	}

	// MEthod to get webdriver instance based on given parameters
	// SelfHealingDriver is a custom webdriver to handle self healing feature
	public static WebDriver bddDriver;
	public static WebDriver getWebDriver(Browser browserName, TestExecutionMode executionMode,
										 String remoteWebDriverURL) {

		WebDriver driver = null;
		try {
			switch (executionMode) {
				case Local: {

					if (browserName.equals(Browser.Chrome)) {

						//WebDriverManager.chromedriver().driverVersion("85.0.4183.38").setup();

						ChromeOptions chromeOptions = new ChromeOptions();
						setupBrowserCapability(chromeOptions);
						System.setProperty("webdriver.http.factory", "jdk-http-client");
						chromeOptions.addArguments("--remote-allow-origins=*");
                 /*     // ✅ ADD THESE For Headless mode run
                        chromeOptions.addArguments("--headless=new");
                        chromeOptions.addArguments("--no-sandbox");
                        chromeOptions.addArguments("--disable-dev-shm-usage");
                        chromeOptions.addArguments("--disable-gpu");
                        chromeOptions.addArguments("--window-size=1920,1080");*/

						WebDriverManager.chromedriver().setup();
						driver = new ChromeDriver(chromeOptions);

					}

					else if (browserName.equals(Browser.Edge)) {
						EdgeOptions edgeOptions = new EdgeOptions();
						edgeOptions.addArguments("--remote-allow-origins=*");
						setupBrowserCapability(edgeOptions);
						edgeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);

						try {
							WebDriverManager.edgedriver()
									.avoidBrowserDetection()   // 🔥 KEY FIX
									.setup();

							driver = new EdgeDriver(edgeOptions);

						} catch (Exception e) {

							System.out.println("⚠️ WebDriverManager failed for Edge: " + e.getMessage());
							System.out.println("👉 Falling back to local driver...");

							File edgeDriverFile = new File("drivers/msedgedriver.exe");

							if (!edgeDriverFile.exists()) {
								throw new RuntimeException("❌ Local Edge driver not found at: " + edgeDriverFile.getAbsolutePath());
							}

							System.setProperty("webdriver.edge.driver", edgeDriverFile.getAbsolutePath());
							driver = new EdgeDriver(edgeOptions);
						}

						if (driver == null) {
							throw new RuntimeException("❌ Driver is NULL for Edge browser");
						}

					}
					else if (browserName.equals(Browser.FireFox)) {
						FirefoxOptions firefoxoptions = new FirefoxOptions();
						firefoxoptions.addArguments("--remote-allow-origins=*");
						driver = WebDriverManager.firefoxdriver().capabilities(firefoxoptions).create();
					}

					break;
				}

				case Remote: {
					if (browserName.equals(Browser.Chrome)) {

						ChromeOptions chromeOptions = new ChromeOptions();
						setupBrowserCapability(chromeOptions);

						chromeOptions.addArguments("--remote-allow-origins=*");
						// driver =
						// WebDriverManager.chromedriver().capabilities(chromeOptions).create();
						driver = new RemoteWebDriver(new URL(props.getProperty("remoteURL")), chromeOptions);


					}

					if (browserName.equals(Browser.Edge)) {
						EdgeOptions edgeOptions = new EdgeOptions();

						setupBrowserCapability(edgeOptions);
						edgeOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);

						// driver = WebDriverManager.edgedriver().capabilities(edgeOptions).create();
						driver = new RemoteWebDriver(new URL(props.getProperty(remoteWebDriverURL)), edgeOptions);


					}
					break;
				}
				case Grid: {
					DesiredCapabilities dc = new DesiredCapabilities();
					dc.setCapability("browser", browserName);
					try {
						driver = new RemoteWebDriver(new URL(remoteWebDriverURL), dc);

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
				default:
					driver = WebDriverManager.getInstance(browserName.toString()).create();

					break;
			}
		} catch (Exception ex) {
			System.out.println("There was error while creating web driver.");
			ex.printStackTrace();
			// throw ex;
		}
		return driver;
	}

	// Method to setup browser capability for mobile emulator testing
	public static void setupBrowserCapability(ChromiumOptions<?> chromeOptions) {
		if (Boolean.parseBoolean(props.getProperty("mobileEmulator")) == true) {
			Map<String, Object> mobileEmulation = new HashMap<>();
			mobileEmulation.put("deviceName", props.getProperty("mobileEmulatorType"));
			chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
		}
	}

	// Method to read properties file and return Properties object
	public static Properties getTestConfig(String filename) {
		Properties prop = new Properties();
		try {
			FileInputStream file = new FileInputStream(filename);
			try {
				prop.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				file.close();
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			logger.debug(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			logger.debug(ex);
		}
		return prop;
	}

	// Method to return a value from properties file based on key
	public String GetPropertyValue(Properties properties, String key) {
		return properties.getProperty(key);
	}

	// Method to take screenshot and save it to given path
	public static void takeScreenShot(ITestResult result, Path Destination) throws Exception {
		ITestContext context = result.getTestContext();
		WebDriver driver = (WebDriver) context.getAttribute("driver");
		// System.out.println("getName() " + result.getName());
		TakesScreenshot ts = (TakesScreenshot) driver;
		File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
		try {
			Files.move(screenshotFile.toPath(), Destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug(ex);
		}

	}

	// Method to get browser enum based on given string
	public static Browser getBrowser(String browserName) {

		try {
			browserName = browserName.toLowerCase();
			return Browser.valueOf(browserName);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	// Method to take screenshot and store it in a given path
	public static String getScreenShot(ITestResult Result, Properties props) throws Exception {
		try {
			AutomationTool automationTool = (AutomationTool) Result.getTestContext().getAttribute("AutomationTool");
			Path destination;
			destination = Paths
					.get(props.getProperty("testResultOutputDirectory") + Result.getName() + "_" + "screenshot.png");

			System.out.println(CoreTestManager.reportDirectpath);
			//	destination = Paths.get(CoreTestManager.reportDirectpath);

			// TestUtility.takeScreenShot(Result, destination);
			File destFile = null;
			String path = null;
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
			String name=Result.getMethod().getMethodName();
			String filename = "";
			switch (automationTool) {
				case SELENIUM:
					// WebDriver driver = (WebDriver) Result.getAttribute("driver");
					// TakesScreenshot ts = (TakesScreenshot) driver;
					WebDriver driver = (WebDriver) Result.getAttribute("driver");
					TakesScreenshot ts = (TakesScreenshot) driver;
					File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
					//File screenshotFile = ((TakesScreenshot) driver.getDelegate()).getScreenshotAs(OutputType.FILE);

					try {
						String reportDirectory = new File(System.getProperty("user.dir")).getAbsolutePath()
								+ "\\test_result\\";
						//	String reportDirectory = String.valueOf(new File(TestManager.reportDirectpath));
						filename =  name+"_"+formater.format(calendar.getTime()) + ".png";
						//	path = reportDirectory + filename;
						path = CoreTestManager.reportDirectpath + "/" + filename;
						destFile = new File(
								(String) path);
						destination = Paths.get(path);

						FileUtils.copyFile(screenshotFile, destFile);
					} catch (Exception e) {
						throw e;
					}
					break;
				case PLAYWRIGHT: {
					try {
						// Page page = (Page) Result.getTestContext().getAttribute("driver");
						Page page = (Page) Result.getAttribute("driver");
						/*destination = Paths.get(
								props.getProperty("testResultOutputDirectory") + Result.getName() + "_"
										+ "screenshot.png");*/
                        destination = Paths.get(
                                CoreTestManager.reportDirectpath
                                        + File.separator
                                        + Result.getName()
                                        + "_screenshot.png");
						byte[] screenshotfile = page
								.screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));

						FileOutputStream out = new FileOutputStream(destination.toString());
						out.write(screenshotfile);
						out.close();
						destination = Paths.get(Result.getName() + "_" + "screenshot.png");

					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
						logger.debug(ex);
					}
					break;
				}
				default: {

				}

			}
			return filename;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug(ex);
			return "";
		}
	}

	// Method to take screenshot and store it in a given path
	public static String getScreenShot(IAttributes Result, Properties props, String testName) throws Exception {
		AutomationTool automationTool = (AutomationTool) Result.getAttribute("AutomationTool");
		Path destination;
		destination = Paths.get(props.getProperty("testResultOutputDirectory") + testName + "_" + "screenshot.png");
		// TestUtility.takeScreenShot(Result, destination);

		switch (automationTool) {
			case SELENIUM: {
				WebDriver driver = (WebDriver) Result.getAttribute("driver");
				TakesScreenshot ts = (TakesScreenshot) driver;
				File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
				try {
					Files.move(screenshotFile.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					throw e;
				}
			}
			break;
			case PLAYWRIGHT: {
				try {
					// Page page = (Page) Result.getTestContext().getAttribute("driver");
					Page page = (Page) Result.getAttribute("driver");
					destination = Paths
							.get(props.getProperty("testResultOutputDirectory") + testName + "_" + "screenshot.png");
					byte[] screenshotfile = page
							.screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));

					FileOutputStream out = new FileOutputStream(destination.toString());
					out.write(screenshotfile);
					out.close();
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					logger.debug(ex);
				}
				break;
			}
			default: {
				WebDriver driver = (WebDriver) Result.getAttribute("driver");
				TakesScreenshot ts = (TakesScreenshot) driver;
				File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
				try {
					Files.move(screenshotFile.toPath(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					throw e;
				}
				break;
			}

		}
		return destination.toString();
	}

	// Method to store screenshots in a zip file
	public static void zipFolder(File sourceFolder, String zipFilePath) throws IOException {

		FileOutputStream fos = new FileOutputStream(zipFilePath);
		java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(fos);

		zipFile(sourceFolder, sourceFolder.getName(), zos);

		zos.close();
		fos.close();
	}

	private static void zipFile(File fileToZip, String fileName, java.util.zip.ZipOutputStream zos) throws IOException {

		if (fileToZip.isHidden()) {
			return;
		}

		if (fileToZip.isDirectory()) {

			if (!fileName.endsWith("/")) {
				fileName += "/";
			}

			zos.putNextEntry(new java.util.zip.ZipEntry(fileName));
			zos.closeEntry();

			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + childFile.getName(), zos);
			}
			return;
		}

		FileInputStream fis = new FileInputStream(fileToZip);
		java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		fis.close();
	}

	// Method to send a mail by using malkom.AI Graph API and given properties
	/*public static void sendMail(Properties props) {
		try {

			// ================================
			// Values from your Python Script
			// ================================
			String CLIENT_ID = "c69b4a7d-70f7-4d6e-b161-ff34cdc1bae8";
			String CLIENT_SECRET = "IZE8Q~UZQqBEtlBmHnYtSdQCa6OqcR1bpyCERb7Q";
			String TENANT_ID = "23f8ecfb-6a90-46c2-96ca-4a362721b82c";
			String SENDER_EMAIL = "lokeshsingh.rajawat@wns.com";

			String recipient = props.getProperty("emailAddress");

			// ================================
			// Get Access Token
			// ================================
			String authority = "https://login.microsoftonline.com/" + TENANT_ID;

			ConfidentialClientApplication app =
					ConfidentialClientApplication.builder(
									CLIENT_ID,
									ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
							.authority(authority)
							.build();

			ClientCredentialParameters parameters =
					ClientCredentialParameters.builder(
									Collections.singleton("https://graph.microsoft.com/.default"))
							.build();

			IAuthenticationResult result = app.acquireToken(parameters).join();
			String accessToken = result.accessToken();

			// ================================
			// HTML EMAIL BODY
			// ================================
			String currentTime = new SimpleDateFormat("dd MMM yyyy hh:mm a").format(new Date());

			String htmlBody =
					"<html>" +
							"<body style='font-family:Segoe UI, Arial;'>" +

							"<h2 style='color:#2E86C1;'>Automation Test Execution Report</h2>" +

							"<p>Dear Team,<br><br>" +
							"The automation test suite has been executed successfully. Please find the details below:</p>" +

							"<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>" +
							"<tr style='background-color:#2E86C1; color:white;'>" +
							"<th>Parameter</th><th>Details</th></tr>" +

							"<tr><td><b>Application</b></td><td>Skill Matrix</td></tr>" +
							"<tr><td><b>Environment</b></td><td>Local Execution</td></tr>" +
							"<tr><td><b>Browser</b></td><td>" + props.getProperty("browser") + "</td></tr>" +
							"<tr><td><b>Execution Time</b></td><td>" + currentTime + "</td></tr>" +
							"<tr><td><b>Report Type</b></td><td>Extent Report</td></tr>" +

							"</table>" +

							"<br>" +

							*//*	"<h3 style='color:#28B463;'>Execution Summary</h3>" +

							"<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>" +
							"<tr style='background-color:#28B463; color:white;'>" +
							"<th>Total</th><th>Passed</th><th>Failed</th><th>Skipped</th></tr>" +

						"<tr>" +
							"<td>[AUTO]</td>" +
							"<td style='color:green;'><b>[AUTO]</b></td>" +
							"<td style='color:red;'><b>[AUTO]</b></td>" +
							"<td style='color:orange;'><b>[AUTO]</b></td>" +
							"</tr>" +
							"</table>" +*//*

							"<br>" +

							"<p>" +
							"Detailed report is attached as ZIP.<br>" +
							"Please extract and open <b>Result.html</b> to view full report with screenshots." +
							"</p>" +

							"<br>" +

							"<p>Thanks & Regards,<br>" +
							"<b>Automation Team</b></p>" +

							"</body></html>";


			// ================================
			// Prepare Email JSON
			// ================================
			JSONObject message = new JSONObject();
			message.put("subject", "Automation Test Execution Report - Skill Matrix");

			JSONObject body = new JSONObject();
			body.put("contentType", "HTML");
			body.put("content",htmlBody);
			message.put("body", body);
			// ================================
			// RECIPIENTS
			// ================================

			JSONArray toRecipients = new JSONArray();


			// send email to multiple address
			String[] recipients = recipient.split(",");

			for (String r : recipients) {
				JSONObject recipientObj = new JSONObject();
				JSONObject emailAddress = new JSONObject();
				emailAddress.put("address", r.trim());
				recipientObj.put("emailAddress", emailAddress);
				toRecipients.put(recipientObj);
			}

			message.put("toRecipients", toRecipients);
// ================================
// Attachment Handling (ZIP Latest Result Folder)
// ================================

			try {
				String resultDirPath = props.getProperty("testResultOutputDirectory"); // ./test_result/

				File resultDir = new File(resultDirPath);

				// Get latest result folder
				File[] resultFolders = resultDir.listFiles(File::isDirectory);

				if (resultFolders != null && resultFolders.length > 0) {

					// Sort by last modified (latest first)
					Arrays.sort(resultFolders, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

					File latestFolder = resultFolders[0];
					System.out.println("Latest Result Folder: " + latestFolder.getAbsolutePath());

					// ZIP file path
					String zipFilePath = latestFolder.getAbsolutePath() + ".zip";

					// Create ZIP
					zipFolder(latestFolder, zipFilePath);

					// Read ZIP file
					File zipFile = new File(zipFilePath);
					byte[] fileBytes = Files.readAllBytes(zipFile.toPath());
					String encodedFile = Base64.getEncoder().encodeToString(fileBytes);

					JSONArray attachments = new JSONArray();
					JSONObject attachment = new JSONObject();

					attachment.put("@odata.type", "#microsoft.graph.fileAttachment");
					attachment.put("name", zipFile.getName());
					attachment.put("contentBytes", encodedFile);

					attachments.put(attachment);
					message.put("attachments", attachments);

					System.out.println("ZIP attachment added successfully");

				} else {
					System.out.println("No result folders found to attach.");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			JSONObject payload = new JSONObject();
			payload.put("message", message);

			// ================================
			// Send via Microsoft Graph
			// ================================
			HttpClient client = HttpClientBuilder.create().build();

			HttpPost request = new HttpPost(
					"https://graph.microsoft.com/v1.0/users/" + SENDER_EMAIL + "/sendMail");

			request.setHeader("Authorization", "Bearer " + accessToken);
			request.setHeader("Content-Type", "application/json");
			request.setEntity(new StringEntity(payload.toString()));

			HttpResponse response = client.execute(request);

			System.out.println("Email sent successfully. Response Code: "
					+ response.getStatusLine().getStatusCode());

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug(ex);
		}

	}
	*/

	// Send Email method using SMTP Server
	public static void sendMail(Properties props) {

		try {

			// ==========================================
			// FETCH EXECUTION DETAILS
			// ==========================================

			//String suiteName = TestResultListener.suiteName;

            String suiteName;

            if (TestResultListener.suiteName != null && !TestResultListener.suiteName.trim().isEmpty()) {
                suiteName = TestResultListener.suiteName;
            }
            else {
                suiteName = TestResultListener.applicationName + " Suite";
            }

//			String environment = props.getProperty("ApplicationUrl");
            String environment = "";
            if ("OrangeHRM".equals(TestResultListener.applicationName)) {
                environment = props.getProperty("OrangeHRMUrl");
            }
            else if ("Skill Matrix".equals(TestResultListener.applicationName)) {
                environment = props.getProperty("ApplicationUrl");
            }
			String executionStart =
					formatTime(TestResultListener.executionStartTime);

			String executionEnd =
					formatTime(TestResultListener.executionEndTime);

			String executionDuration =
					formatDuration(
							TestResultListener.executionStartTime,
							TestResultListener.executionEndTime
					);

			int passedCount = TestResultListener.passed;
			int failedCount = TestResultListener.failed;
			int skippedCount = TestResultListener.skipped;
			int totalCount = TestResultListener.total;

			double passPercent = 0;

			if (totalCount > 0) {
				passPercent =
						((double) passedCount / totalCount) * 100;
			}

			String passPercentStr =
					String.format("%.2f", passPercent);

			// ==========================================
			// DEBUG LOGS
			// ==========================================

			System.out.println("========== EMAIL DEBUG ==========");
			System.out.println("Suite Name = " + suiteName);
			System.out.println("Total = " + totalCount);
			System.out.println("Passed = " + passedCount);
			System.out.println("Failed = " + failedCount);
			System.out.println("Skipped = " + skippedCount);
			System.out.println("Start Time = " + executionStart);
			System.out.println("End Time = " + executionEnd);
			System.out.println("Duration = " + executionDuration);

			// ==========================================
			// EMAIL DETAILS
			// ==========================================

			String recipient =
					props.getProperty("emailAddress");

			String sender =
					"kapil.rana@wns.com";

			// ==========================================
			// SMTP CONFIG
			// ==========================================

			Properties smtpProps = new Properties();

			smtpProps.put(
					"mail.smtp.host",
					props.getProperty("emailSMTPServer")
			);

			smtpProps.put("mail.smtp.port", "25");

			smtpProps.put("mail.smtp.auth", "false");

			Session session =
					Session.getInstance(smtpProps);

			MimeMessage message =
					new MimeMessage(session);

			message.setFrom(new InternetAddress(sender));

			/*message.setSubject(
					"Automation Test Execution Report - Skill Matrix"
			);*/
            message.setSubject(
                    "Automation Test Execution Report - "
                            + TestResultListener.applicationName
            );

			message.setRecipients(
					Message.RecipientType.TO,
					InternetAddress.parse(recipient)
			);

			// ==========================================
			// PIE CHART
			// ==========================================

			String chartPath = generatePieChart();

			MimeBodyPart chartPart =
					new MimeBodyPart();

			chartPart.attachFile(chartPath);

			chartPart.setHeader(
					"Content-ID",
					"<chartImage>"
			);

			chartPart.setDisposition(
					MimeBodyPart.INLINE
			);

			// ==========================================
			// JIRA BUG LINKS
			// ==========================================

			String jiraDefectsHtml = "";
			String jiraUrl = props.getProperty("TestManagementToolURL");
			if (!TestResultListener.jiraDefectIDs.isEmpty() && jiraUrl != null && !jiraUrl.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("<br>");
				sb.append("<h3 style='color:#2E86C1;'>Jira Bugs Logged</h3>");
				sb.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>");
				sb.append("<tr style='background-color:#2E86C1; color:white;'><th>Defect ID</th><th>Link</th></tr>");
				for (String id : TestResultListener.jiraDefectIDs) {
					sb.append("<tr>");
					sb.append("<td>").append(id).append("</td>");
					sb.append("<td><a href='").append(jiraUrl).append("/browse/").append(id).append("'>")
							.append(jiraUrl).append("/browse/").append(id).append("</a></td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				jiraDefectsHtml = sb.toString();
			}

			// ==========================================
			// HTML BODY
			// ==========================================

			String htmlBody =

					"<html>" +

							"<body style='font-family:Segoe UI, Arial;'>"

							+

							"<h2 style='color:#2E86C1;'>"
							+
							"Automation Test Execution Report"
							+
							"</h2>"

							+

							"<p>"
							+
							"Dear Team,<br><br>"
							+
							"The automation test suite has been executed."
							+
							" Please find the execution details below:"
							+
							"</p>"

							+

							"<table border='1' cellpadding='8' cellspacing='0' "
							+
							"style='border-collapse:collapse;'>"

							+

							"<tr style='background-color:#2E86C1; color:white;'>"
							+
							"<th>Parameter</th>"
							+
							"<th>Details</th>"
							+
							"</tr>"

							+

							"<tr><td><b>Suite Name</b></td><td>"
							+ suiteName +
							"</td></tr>"

							+

							/*"<tr><td><b>Application</b></td><td>"
							+
							"Skill Matrix"
							+
							"</td></tr>"*/
                            "<tr><td><b>Application</b></td><td>"
                            +
                            TestResultListener.applicationName
                            +
                            "</td></tr>"

							+

							"<tr><td><b>Environment URL</b></td><td>"
							+ environment +
							"</td></tr>"

							+

							"<tr><td><b>Browser</b></td><td>"
							+ props.getProperty("browser") +
							"</td></tr>"

							+

							"<tr><td><b>Execution Start Time</b></td><td>"
							+ executionStart +
							"</td></tr>"

							+

							"<tr><td><b>Execution End Time</b></td><td>"
							+ executionEnd +
							"</td></tr>"

							+

							"<tr><td><b>Total Duration</b></td><td>"
							+ executionDuration +
							"</td></tr>"

							+

							"<tr><td><b>Report Type</b></td><td>"
							+
							"Extent Report"
							+
							"</td></tr>"

							+

							"</table>"

							+

							"<br>"

							+

							"<h3 style='color:#2E86C1;'>"
							+
							"Execution Summary"
							+
							"</h3>"

							+

							"<table border='1' cellpadding='8' cellspacing='0' "
							+
							"style='border-collapse:collapse;'>"

							+

							"<tr style='background-color:#2E86C1; color:white;'>"
							+
							"<th>Total</th>"
							+
							"<th>Passed</th>"
							+
							"<th>Failed</th>"
							+
							"<th>Skipped</th>"
							+
							"</tr>"

							+

							"<tr>"

							+

							"<td>"
							+ totalCount +
							"</td>"

							+

							"<td style='color:green;'>"
							+
							"<b>" + passedCount + "</b>"
							+
							"</td>"

							+

							"<td style='color:red;'>"
							+
							"<b>" + failedCount + "</b>"
							+
							"</td>"

							+

							"<td style='color:orange;'>"
							+
							"<b>" + skippedCount + "</b>"
							+
							"</td>"

							+

							"</tr>"

							+

							"</table>"

							+

							"<br>"

							+

							"<h3 style='color:#2E86C1;'>"
							+
							"Pass Percentage : "
							+ passPercentStr +
							"%"
							+
							"</h3>"

							+

							"<br>"

							+

							"<img src='cid:chartImage' width='400' height='300'/>"

							+

							"<br><br>"

							+

							"<p>"
							+
							"Detailed report is attached as ZIP.<br>"
							+
							"Please extract and open "
							+
							"<b>Result.html</b>"
							+
							" to view complete execution details."
							+
							"</p>"

							+
							jiraDefectsHtml
							+

							"<br>"

							+

							"<p>"
							+
							"Thanks & Regards,<br>"
							+
							"<b>Automation Team</b>"
							+
							"</p>"

							+

							"</body></html>";

			// ==========================================
			// HTML BODY PART
			// ==========================================

			MimeBodyPart bodyPart =
					new MimeBodyPart();

			bodyPart.setContent(
					htmlBody,
					"text/html; charset=utf-8"
			);

			// ==========================================
			// ZIP ATTACHMENT
			// ==========================================

			MimeBodyPart attachmentPart =
					new MimeBodyPart();

			String resultDirPath =
					props.getProperty(
							"testResultOutputDirectory"
					);

			File resultDir =
					new File(resultDirPath);

			File[] resultFolders =
					resultDir.listFiles(File::isDirectory);

			if (resultFolders != null
					&& resultFolders.length > 0) {

				Arrays.sort(
						resultFolders,
						(f1, f2) ->
								Long.compare(
										f2.lastModified(),
										f1.lastModified()
								)
				);

				File latestFolder =
						resultFolders[0];

				System.out.println(
						"Latest Result Folder: "
								+ latestFolder.getAbsolutePath()
				);

				String zipFilePath =
						latestFolder.getAbsolutePath()
								+ ".zip";

				zipFolder(
						latestFolder,
						zipFilePath
				);

				attachmentPart.attachFile(
						new File(zipFilePath)
				);

				System.out.println(
						"ZIP attachment added successfully"
				);

			} else {

				System.out.println(
						"No result folders found to attach."
				);
			}

			// ==========================================
			// MULTIPART
			// ==========================================

			Multipart multipart =
					new MimeMultipart();

			multipart.addBodyPart(bodyPart);

			multipart.addBodyPart(chartPart);

			multipart.addBodyPart(attachmentPart);

			message.setContent(multipart);

			// ==========================================
			// SEND EMAIL
			// ==========================================

			Transport.send(message);

			System.out.println(
					"Email sent successfully via SMTP"
			);

		} catch (Exception ex) {

			ex.printStackTrace();

			logger.debug(ex);
		}
	}

	// Method to fetch a web element from a html table
	public static WebElement getWebElementFromTable(WebDriver driver, String tableXpath, int startRowNum, int endRowNum,
													int startcolNum, int endColNum, String textTobeFound) {
		WebElement webelement = null;
		boolean elementFound = false;
		try {
			for (int rownum = startRowNum; rownum <= endRowNum; rownum++) {
				for (int colnum = startcolNum; colnum <= endColNum; colnum++) {
					String element = tableXpath + "/tr[" + rownum + "]/td[" + colnum + "]";
					webelement = driver.findElement(By.xpath(element));
					try {
						if (webelement != null
								&& webelement.getText().toUpperCase().equals(textTobeFound.toUpperCase())) {
							elementFound = true;
							break;
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new Exception("Invalid data");
					}
				}
				if (elementFound)
					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// throw new Exception("Invalid data");
		}
		return webelement;
	}

	//****************Send Email Finished********************

	//  Add Time Formatting Utility
	private static String formatTime(long millis) {
		return new SimpleDateFormat("dd MMM yyyy hh:mm:ss a")
				.format(new Date(millis));
	}

	private static String formatDuration(long start, long end) {

		long duration = end - start;

		long seconds = duration / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;

		return hours + "h " + minutes + "m " + seconds + "s";
	}
	// return webelement;

	// Method to fetch a web element from a html table for Playwright based test
	public static Locator getWebElementFromTable(Page page, String tableXpath, int startRowNum, int endRowNum,
												 int startcolNum, int endColNum, String textTobeFound) {
		Locator webelement = null;
		boolean elementFound = false;
		for (int rownum = startRowNum; rownum <= endRowNum; rownum++) {
			for (int colnum = startcolNum; colnum <= endColNum; colnum++) {
				String element = tableXpath + "/tr[" + rownum + "]/td[" + colnum + "]";
				// System.out.println(element);
				webelement = page.locator("xpath=" + element);
				// if (webelement != null) {
				// System.out.println("given text is : " + textTobeFound.toUpperCase() + " text
				// found in the app is "
				// + webelement.textContent().toUpperCase());
				// System.out
				// .println(webelement.textContent().trim().toUpperCase().equals(textTobeFound.toUpperCase()));
				// }

				try {
					if (webelement != null && webelement.textContent().trim().toUpperCase()
							.equals(textTobeFound.toUpperCase()) == true) {
						elementFound = true;
						// System.out.println("element found");
						break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}
			}
			if (elementFound)
				break;
		}
		return webelement;
	}


	// Connect to the database and execute the given SQL statement
	public static ResultSet executeSQL(Connection conn, String SQL) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(SQL);
			return res;
		} catch (SQLException ex) {
			System.out.println("Failure in executing SQL statement");
			return null;
		}
	}

	// To get the record count based on given db connection and sql
	public static int getRecordCount(Connection conn, String SQL) throws SQLException {

		ResultSet res = executeSQL(conn, SQL);
		int count = 0;
		if (res != null) {
			if (res.next()) {
				count = res.getInt(1);
			}
			res.close();
			// System.out.println("Table has " + count + " row(s).");
		}
		return count;

	}

	// Method to establish database connection
	public static Connection getDBConnection() throws ClassNotFoundException {
		Connection conn = null;
		String DB_URL = "";
		Object userdir = System.getProperty("user.dir");
		String propertyFile = userdir + "\\src\\test\\java\\testconfig\\test.properties";
		Properties props = TestUtility.getTestConfig(propertyFile);
		DBMSTYPE DBMSName = DBMSTYPE.valueOf(props.getProperty("DBMS_TYPE").toUpperCase());
		switch (DBMSName) {
			case SQLSERVER: {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				DB_URL = "jdbc:" + DBMSName.toString().toLowerCase() + "://" + props.getProperty("DBMSServerIP") + ":"
						+ props.getProperty("DBMSServerPort") + ";" + "DatabaseName="
						+ props.getProperty("DatabaseName")
						+ ";" + "user=" + props.getProperty("DBUserName") + ";" + "password="
						+ props.getProperty("DBPassword") + ";" + "trustServerCertificate=true";
				System.out.println(DB_URL);
				// System.out.println("Expected url "+
				// "jdbc:sqlserver://10.31.5.4:1433;DatabaseName=skilldb_live;user=sa;password=wnsuser@12345;trustServerCertificate=true");
				try {
					conn = DriverManager.getConnection(DB_URL);
					System.out.println("The connection from utlity class is successful");
					return conn;

				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("DB connectivity failed");
					return null;
				}

			}
			default: {
				return null;
			}

		}

		// String
		// Connectionurl="jdbc:sqlserver://10.31.5.4:1433;DatabaseName=skilldb_live;user=sa;password=wnsuser@12345;trustServerCertificate=true";

	}

	// Method to wait for a given time
	public static void waitFor(int time) {
		try {
			Thread.sleep(time * 1000);
		} catch (Exception e) {

		}
	}

	/**
	 * Captures a screenshot when screenshot capture is enabled and saves it
	 * with the test status, filename, and timestamp for reporting/debugging.
	 */
	public static String takeScreenShot(String filename, WebDriver driver) {
		String path = "";
		try {
			boolean getScreenshot = Boolean.parseBoolean(props.getProperty("getScreenshot"));
			if (getScreenshot) {
				File destFile = null;
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat formater = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss");
				File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				path = CoreTestManager.reportDirectpath + "/" + "PASS_" + filename + "_" + formater.format(calendar.getTime()) + ".png";
				destFile = new File((String) path);
				FileUtils.copyFile(scrFile, destFile);
			}
		} catch (Exception e) {

		}
		return path;
	}


	/**
	 * Generates and returns a random integer within the specified minimum and maximum range.
	 * Useful for generating dynamic test data or unique values during test execution.
	 */
	public static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}


	/**
	 * Switches the WebDriver context from the current page to the iframe
	 * identified by the provided XPath.
	 */
	public void switchToIframe(String xpath, WebDriver driver){
		WebElement we = driver.findElement(By.xpath(xpath));
		driver.switchTo().frame(we);
	}


	/**
	 * Switches the WebDriver context back from the current frame to the main page content.
	 */
	public void switchToMainFrame(WebDriver driver){
		driver.switchTo().parentFrame();
		driver.switchTo().defaultContent();
	}


	/**
	 * Searches the provided list of options for the specified text and selects
	 * the matching option after ensuring it is ready for interaction.
	 */
	public void optionIterator(List<WebElement> options, String text, WebDriver driver) {
		for (WebElement option : options) {
			if (option.getText().equalsIgnoreCase(text)) {
				waitUntilClickable(option, 20,driver);

				try {
					waitFor(2);
					mousehoverwithElement(option,driver);
					option.click();
					waitFor(2);
				} catch (ElementClickInterceptedException e) {
					option.click();
				}
				waitFor(3);
				break;
			}
		}
	}


	/**
	 * Iterates through all provided options and clicks each option sequentially.
	 */
	public void clickOnAlloptionIterator(List<WebElement> options){

		for (WebElement option : options) {
			option.click();
			waitFor(1);
		}
	}


	/**
	 * Validates whether the provided value matches the specified date format.
	 * Returns true when the value represents a valid date in the expected format.
	 */
	public boolean isValidFormat(String format, String value) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(value);
			if (!value.equals(sdf.format(date))) {
				date = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return date != null;
	}


	/**
	 * Converts the provided month, day, and year into the MM/dd/yyyy date format.
	 */
	public String changeDateFormat(String month, String year, String day){
		Date date = new Date(month+" "+day+" "+year);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String format = formatter.format(date);
		System.out.println(format);
		return format;
	}


	/**
	 * Selects an option from a standard HTML dropdown using its visible text.
	 */
	public void selectFromDropdownByText(String txt, WebElement we){
		Select drp = new Select(we);
		drp.selectByVisibleText(txt);
	}


	/**
	 * Returns the current system date and time in MM/dd/yyyy hh:mm:ss a format.
	 */
	public static String currentDate(){
		SimpleDateFormat formDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		// String strDate = formDate.format(System.currentTimeMillis()); // option 1
		Date date2 = new Date();
		String strDate = formDate.format(date2);
		return strDate;
	}


	/**
	 * Performs a click on the specified web element using JavaScript.
	 * Useful when a standard Selenium click is not successful.
	 */
	public void clickOnJS(WebElement element, WebDriver driver){
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			String scrpt = "arguments[0].click();";
			js.executeScript(scrpt, element);
		} catch (Exception e) {
			System.out.println("Not working11");
		}
	}


	/**
	 * Adds or updates a key-value pair in the runtime test data properties file.
	 * Used for storing test data generated or modified during execution.
	 */
	public static void setPropertyFileData(String key, String value) {

		String path = System.getProperty("user.dir") + "/src/test/java/testData/runTimeTestData.properties";
		try {
			FileInputStream in = new FileInputStream(path);
			Properties props = new Properties();
			props.load(in);
			in.close();

			FileOutputStream out = new FileOutputStream(path);
			props.setProperty(key, value);
			props.store(out, null);
			out.close();
		} catch (Exception e) {

		}
	}


	/**
	 * Reads and returns the value associated with the specified key
	 * from the runtime test data properties file.
	 */
	public static String getPropertyFileData(String key) {

		Properties prop = new Properties();
		try {
			FileInputStream file = new FileInputStream(System.getProperty("user.dir") + "/src/test/java/testData/runTimeTestData.properties");
			try {
				prop.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			logger.debug(ex);
		}
		return prop.getProperty(key);
	}


	/**
	 * Uploads a file to the specified web element by providing its absolute file path.
	 */
	public static void fileUpload(WebElement element, String filepath) {
		filepath = System.getProperty("user.dir") + filepath;
		filepath = filepath.replace("/", "\\");
		element.sendKeys(filepath);
	}


	/**
	 * Reads test data from the specified Excel sheet for the row identified by the given key
	 * and returns the data as a key-value map using the Excel headers as keys.
	 */
	public Map<String, String> readDataFromExcel(String sheetName, String key) {
		Map<String, String> rowData = null;

		rowData = new LinkedHashMap<String, String>();

		Map<Integer, Map<String, String>> sheetData = new LinkedHashMap<Integer, Map<String, String>>();
		Workbook book;

		try {
			book=getWorkbook();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		;
		Sheet sheet = book.getSheet(sheetName);
		String value = "";
		for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
			Row currRow = sheet.getRow(i);
			if (null == currRow)
				continue;
			int colCount = sheet.getRow(0).getLastCellNum();
			for (int j = 0; j < colCount; j++) {
				String header = sheet.getRow(0).getCell(j).getStringCellValue();
				Cell currCell = currRow.getCell(j);
				if (currRow.getCell(0).getStringCellValue().equalsIgnoreCase(key)) {

					if (null != currCell) {
						currCell.setCellType(CellType.STRING);

						value = currCell.getStringCellValue();
						rowData.put(header, value);
					}

				}

			}
			sheetData.put(Integer.valueOf(i), rowData);
		}

		return rowData;
	}


	/**
	 * Loads and returns the Excel workbook configured in the test properties.
	 */
	public static Workbook getWorkbook() throws IOException {
		File f = new File(props.getProperty("excelFile"));

		InputStream is = null;
		try {
			is = new FileInputStream(f);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Workbook book = null;
		try {
			book = new HSSFWorkbook(is);
		} catch (IOException e) {
			e.printStackTrace();

		}
		return book;
	}


	/**
	 * Initializes the configured test management tool connector and returns
	 * the corresponding connector implementation for Jira or Azure DevOps.
	 */
	public static IToolsConnector getTestManagementToolConnector(Properties props) {

		IToolsConnector connector = null;

		try {
			String tool = props.getProperty("TestManagementTool");

			if (tool != null && !tool.isEmpty()) {

				if (tool.equalsIgnoreCase("Jira")) {

					connector = JiraConnector.getInstance(props);
					System.out.println("Jira Connector Initialized Successfully");

				} else if (tool.equalsIgnoreCase("AzureDevOPS")) {

					connector = AzureDevOpsConnector.getInstance(props);
					System.out.println("Azure DevOps Connector Initialized Successfully");

				} else {

					System.out.println("Invalid Test Management Tool configured.");
				}
			} else {
				System.out.println("TestManagementTool property not found.");
			}

		} catch (Exception e) {
			System.out.println("Error initializing Test Management Connector: " + e.getMessage());
			e.printStackTrace();
		}

		return connector;
	}

	// TP-Pro methods

	// Captures the alert message and accepts the browser alert.
	public String handlingAlertAccept(WebDriver driver) {
		waitFor(2);
		Alert alert = driver.switchTo().alert();
		boolean status = false;
		String txt = alert.getText();
		System.out.println(txt);
		// Capturing alert message.
		alert.accept();
		waitFor(2);
		return txt;
	}

	// Switches WebDriver focus to the newly opened browser tab.
	public void switchNewTab(WebDriver driver){
		ArrayList<String> newTb = new ArrayList<String>(driver.getWindowHandles());
		//switch to new tab
		driver.switchTo().window(newTb.get(1));
		System.out.println("Page title of new tab: " + driver.getTitle());
	}

	// Switches WebDriver focus back to the original browser tab.
	public void switchOldTab(WebDriver driver){
		ArrayList<String> newTb = new ArrayList<String>(driver.getWindowHandles());
		//switch to new tab
		driver.switchTo().window(newTb.get(0));
		System.out.println("Page title of new tab: " + driver.getTitle());
	}

	// Opens a new browser tab using JavaScript and prepares it for further actions.
	public void openNewTab(WebDriver driver){
		try {
			Thread.sleep(5000);
			JavascriptExecutor js = (JavascriptExecutor)driver;
			js.executeScript("window.open();");
			Thread.sleep(5000);
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	// Scrolls to an element and performs a JavaScript-based click when a normal click is unreliable.
	public static void jsClick(WebElement element, WebDriver driver) {
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;

			js.executeScript("arguments[0].scrollIntoView(true);", element);
			js.executeScript("arguments[0].click();", element);

		} catch (Exception e) {
			throw new RuntimeException("JS click failed on element: " + element, e);
		}
	}

	// Scrolls the browser page directly to the bottom.
	public static void scrollToBottom(WebDriver driver) {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
	}

	// Scrolls the browser page back to the top with smooth scrolling.
	public static void ScrollToTop(WebDriver driver) {
		((JavascriptExecutor) driver).executeScript("window.scrollTo({top: 0, behavior: 'smooth'});");
	}

	// Extracts and returns the first text match based on the supplied regular expression.
	public static String extractRegex(String text, String regexPattern) {
		String result = "";
		try {
			if (text != null && !text.isEmpty()) {
				Pattern pattern = Pattern.compile(regexPattern);
				Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {
					result = matcher.group(0); // group(0) returns the entire match
				}
			}
		} catch (Exception e) {
			System.err.println("Error during Regex Extraction: " + e.getMessage());
		}
		return result;
	}

	// Waits until the specified element is visible and enabled before continuing.
	public void waitUntilEnabled(WebElement element, int timeoutInSeconds, WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
		wait.until(d ->
				element.isDisplayed() &&
						element.isEnabled()
		);
	}

	// Validates the alert text and dismisses the alert when the expected text is present.
	public boolean handlingAlertDismiss(String expectText, WebDriver driver) {
		Alert alert = driver.switchTo().alert();
		boolean status = false;
		// Capturing alert message.
		String alertMessage = driver.switchTo().alert().getText().toUpperCase();
		System.out.println(alertMessage);
		if (alertMessage.contains(expectText.toUpperCase())) {
			alert.dismiss();
			status = true;
		}
		return status;
	}
}