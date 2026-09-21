package wns.automation.utilities;

import com.microsoft.playwright.Page;
import org.testng.IAttributes;
import org.testng.ITestResult;
import wns.automation.connectors.Tools.AzureDevOpsConnector;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.connectors.Tools.JiraConnector;
import wns.automation.core.CoreTestManager;
import wns.automation.core.constants.TestManagementTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Shared framework utilities that are independent of the browser implementation.
 */
public final class TestUtility {

    private static Properties props;

    private TestUtility() {
    }

    public static Properties getProps() {
        return props;
    }

    public static void setProps(Properties properties) {
        props = properties;
    }

    public static Properties getTestConfig(String filename) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load test configuration: " + filename, exception);
        }
        return properties;
    }

    public static String getScreenShot(ITestResult result, Properties properties) throws IOException {
        Object browserPage = result.getAttribute("driver");
        if (!(browserPage instanceof Page)) {
            throw new IllegalStateException("Playwright Page is not available for screenshot capture");
        }

        String outputDirectory = getCurrentReportDirectory(properties);
        Path directory = Paths.get(outputDirectory);
        Files.createDirectories(directory);
        String fileName = result.getName() + "_" + System.currentTimeMillis() + ".png";
        Path destination = directory.resolve(fileName);
        ((Page) browserPage).screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));
        return destination.toString();
    }

    public static String getScreenShot(IAttributes result, Properties properties, String testName) throws IOException {
        Object browserPage = result.getAttribute("driver");
        if (!(browserPage instanceof Page)) {
            throw new IllegalStateException("Playwright Page is not available for screenshot capture");
        }

        String outputDirectory = getCurrentReportDirectory(properties);
        Path directory = Paths.get(outputDirectory);
        Files.createDirectories(directory);
        Path destination = directory.resolve(testName + "_" + System.currentTimeMillis() + ".png");
        ((Page) browserPage).screenshot(new Page.ScreenshotOptions().setPath(destination).setFullPage(true));
        return destination.toString();
    }

    public static void waitUntilVisible(com.microsoft.playwright.Locator locator, int timeoutSeconds) {
        locator.waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setTimeout(timeoutSeconds * 1000L));
    }

    public static void sendMail(Properties properties) {
        String smtpServer = requiredProperty(properties, "emailSMTPServer");
        String recipient = requiredProperty(properties, "emailAddress");
        String reportDirectory = CoreTestManager.reportDirectpath;
        if (reportDirectory == null || reportDirectory.trim().isEmpty()) {
            reportDirectory = properties.getProperty("testResultOutputDirectory", "./test_result/");
        }
        Path reportPath = Paths.get(reportDirectory).toAbsolutePath().normalize();

        try {
            Properties mailProperties = new Properties();
            mailProperties.setProperty("mail.smtp.host", smtpServer);
            mailProperties.setProperty("mail.smtp.connectiontimeout",
                    properties.getProperty("emailSMTPConnectionTimeout", "10000"));
            mailProperties.setProperty("mail.smtp.timeout",
                    properties.getProperty("emailSMTPTimeout", "10000"));

            Session session = Session.getInstance(mailProperties);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                    properties.getProperty("emailFrom", recipient)));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(properties.getProperty(
                    "emailSubject", "Automation Test Execution Report - "
                            + TestResultListener.suiteName));

            MimeBodyPart body = new MimeBodyPart();
            body.setContent(buildExecutionSummary(properties), "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(body);
            createExecutionSummaryChart(reportPath);
            Path reportBundle = createReportBundle(reportPath);
            if (reportBundle == null) {
                throw new IllegalStateException(
                        "Current execution report directory does not exist: " + reportPath);
            }
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(reportBundle.toFile());
            attachment.setFileName(reportBundle.getFileName().toString());
            attachment.setDisposition(MimeBodyPart.ATTACHMENT);
            multipart.addBodyPart(attachment);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email notification sent to " + recipient);
        } catch (AddressException exception) {
            throw new IllegalStateException("Invalid email address configuration", exception);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to send email notification through SMTP server " + smtpServer,
                    exception);
        }
    }

    private static String buildExecutionSummary(Properties properties) {
        long duration = Math.max(0,
                TestResultListener.executionEndTime - TestResultListener.executionStartTime);
        long passed = TestResultListener.passed;
        long total = TestResultListener.total;
        double passPercentage = total == 0 ? 0 : passed * 100.0 / total;

        return "<html><body style='font-family:Arial,sans-serif;color:#222'>"
                + "<p>Dear Team,</p>"
                + "<p>The automation test suite has been executed. Please find the execution details below:</p>"
                + "<table style='border-collapse:collapse;width:650px'>"
                + row("Suite Name", TestResultListener.suiteName)
                + row("Application", TestResultListener.applicationName)
                + row("Environment URL", valueOrDefault(
                        TestResultListener.environmentUrl,
                        properties.getProperty("ApplicationUrl", "")))
                + row("Browser", valueOrDefault(
                        TestResultListener.browserName,
                        properties.getProperty("browser", "")))
                + row("Execution Start Time", formatTime(TestResultListener.executionStartTime))
                + row("Execution End Time", formatTime(TestResultListener.executionEndTime))
                + row("Total Duration", formatDuration(duration))
                + row("Report Type", properties.getProperty("testReporter", "Extent"))
                + "</table>"
                + "<h3 style='color:#1683c5;margin-top:28px'>Execution Summary</h3>"
                + "<table style='border-collapse:collapse'>"
                + headerCell("Total") + headerCell("Passed") + headerCell("Failed")
                + headerCell("Skipped") + "<tr>"
                + valueCell(String.valueOf(TestResultListener.total), "#000")
                + valueCell(String.valueOf(TestResultListener.passed), "#008000")
                + valueCell(String.valueOf(TestResultListener.failed), "#c00000")
                + valueCell(String.valueOf(TestResultListener.skipped), "#d58b00")
                + "</tr></table>"
                + "<p style='color:#1683c5;font-size:16px;font-weight:bold'>Pass Percentage : "
                + String.format(java.util.Locale.ROOT, "%.2f", passPercentage) + "%</p>"
                + "<p>Detailed report is attached as a ZIP file.</p>"
                + "<p>Please extract and open <b>"
                + escapeHtml(properties.getProperty("extentResultMainHtmlFileName", "Result.html"))
                + "</b> to view complete execution details.</p>"
                + "</body></html>";
    }

    private static String row(String name, String value) {
        return "<tr><td style='border:1px solid #222;padding:6px;font-weight:bold'>"
                + escapeHtml(name) + "</td><td style='border:1px solid #222;padding:6px'>"
                + escapeHtml(value == null ? "" : value) + "</td></tr>";
    }

    private static String headerCell(String value) {
        return "<th style='background:#1677ad;color:#fff;border:1px solid #222;padding:6px'>"
                + value + "</th>";
    }

    private static String valueCell(String value, String color) {
        return "<td style='border:1px solid #222;padding:6px;color:" + color
                + ";font-weight:bold'>" + value + "</td>";
    }

    private static String formatTime(long time) {
        if (time <= 0) {
            return "Unavailable";
        }
        return new SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new Date(time));
    }

    private static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m "
                + (seconds % 60) + "s";
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static Path createReportBundle(Path reportDirectory) throws IOException {
        if (!Files.isDirectory(reportDirectory)) {
            return null;
        }
        String runName = reportDirectory.getFileName() == null
                ? "Automation-Test-Execution-Report"
                : reportDirectory.getFileName().toString();
        Path bundle = reportDirectory.resolve(runName + ".zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(bundle))) {
            try (java.util.stream.Stream<Path> files = Files.walk(reportDirectory)) {
                for (Path file : (Iterable<Path>) files
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.equals(bundle))::iterator) {
                    String entryName = reportDirectory.relativize(file).toString()
                            .replace(File.separatorChar, '/');
                    zip.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file, zip);
                    zip.closeEntry();
                }
            }
        }
        return bundle;
    }

    private static String getCurrentReportDirectory(Properties properties) {
        String currentDirectory = CoreTestManager.reportDirectpath;
        if (currentDirectory != null && !currentDirectory.trim().isEmpty()) {
            return currentDirectory;
        }
        return properties.getProperty("testResultOutputDirectory", "./test_result/");
    }

    private static void createExecutionSummaryChart(Path reportDirectory) throws IOException {
        Files.createDirectories(reportDirectory);
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Passed", TestResultListener.passed);
        dataset.setValue("Failed", TestResultListener.failed);
        dataset.setValue("Skipped", TestResultListener.skipped);
        JFreeChart chart = ChartFactory.createPieChart(
                "Test Execution Summary", dataset, true, true, false);
        ChartUtils.saveChartAsPNG(
                reportDirectory.resolve("chart.png").toFile(), chart, 700, 450);
    }

    private static String requiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required email property: " + key);
        }
        return value.trim();
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static void attachMatchingFiles(
            Multipart multipart, Path directory, String extension) throws Exception {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            for (Path file : (Iterable<Path>) files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase()
                            .endsWith(extension))
                    .sorted()::iterator) {
                MimeBodyPart attachment = new MimeBodyPart();
                attachment.attachFile(file.toFile());
                multipart.addBodyPart(attachment);
            }
        }
    }

    public static IToolsConnector getTestManagementToolConnector(Properties properties) {
        String configuredTool = properties.getProperty("TestManagementTool", "None");
        TestManagementTools tool = TestManagementTools.valueOf(configuredTool);
        return switch (tool) {
            case Jira -> JiraConnector.getInstance(properties);
            case AzureDevOPS -> AzureDevOpsConnector.getInstance(properties);
            case None -> null;
        };
    }
}
