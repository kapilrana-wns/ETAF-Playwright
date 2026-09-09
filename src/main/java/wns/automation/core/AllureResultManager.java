package wns.automation.core;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.microsoft.playwright.Page;


//@Listeners(AllureTestNg.class)
public class AllureResultManager {//implements ITestResultManager {

	@Attachment(value = "Page screenshot", type = "image/png")
	public byte[] saveScreenshotPNG(WebDriver driver) {
		return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
	   //return ((TakesScreenshot) driver.getDelegate()).getScreenshotAs(OutputType.BYTES);
	}
	
	 
	
	@Attachment(value = "Page screenshot", type = "image/png")
	public byte[] saveScreenshotPNG(Page page) {
		
		return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
	}

	//Text attachments for Allure
	@Attachment(value = "{0}", type = "text/plain")
	public  String saveTextlog(String message) {
		return message;
	}

	//HTML attachments for Allure
	@Attachment(value = "{0}", type = "text/html")
	public  String attachHtml(String html) {
		return html;
	}

	

}
//https://stackoverflow.com/questions/55133944/using-allure-report-in-java-without-any-test-framework
//https://github.com/allure-framework/allure-java/blob/master/allure-java-commons/src/main/java/io/qameta/allure/Allure.java
//https://www.youtube.com/watch?v=d5gjK6hZHE4