package wns.automation.utilities;

import com.microsoft.playwright.Page;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import wns.automation.connectors.Tools.AzureDevOpsConnector;
import wns.automation.connectors.Tools.IToolsConnector;
import wns.automation.connectors.Tools.JiraConnector;
import wns.automation.core.CoreTestManager;
import wns.automation.core.constants.Browser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestUtility {
    private static final Logger logger = LogManager.getLogger(TestUtility.class);
    private static Properties props;

    public static Properties getProps() {
        return props;
    }

    public static void setProps(Properties properties) {
        props = properties;
    }

    public static Properties getTestConfig(String filename) {
        Properties propertySet = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(filename))) {
            propertySet.load(inputStream);
        } catch (IOException ex) {
            logger.debug("Unable to load properties file: {}", filename, ex);
        }
        return propertySet;
    }

    public static Browser getBrowser(String browserName) {
        if (browserName == null) {
            return null;
        }
        try {
            return Browser.valueOf(browserName.trim());
        } catch (IllegalArgumentException ex) {
            logger.debug("Unsupported browser name: {}", browserName, ex);
            return null;
        }
    }

    private static String requiredProperty(Properties properties, String key) {
        String value = (properties == null) ? null : properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            String propertyName = key;
            if (propertyName.endsWith("Address")) {
                propertyName = propertyName.substring(0, propertyName.length() - "Address".length());
            }
            throw new IllegalStateException("Missing required " + propertyName + " property");
        }
        return value;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String getCurrentReportDirectory(Properties properties) {
        if (CoreTestManager.reportDirectpath != null && !CoreTestManager.reportDirectpath.isBlank()) {
            return CoreTestManager.reportDirectpath;
        }
        if (properties != null && properties.getProperty("testResultOutputDirectory") != null) {
            return properties.getProperty("testResultOutputDirectory");
        }
        return "./test_result/";
    }

    public static void configureApplicationMetadata(Properties properties, String testClassName) {
        if (properties == null || testClassName == null) {
            return;
        }
        String normalizedClassName = testClassName.toLowerCase(Locale.ROOT);
        String applicationName;
        String applicationUrlProperty;
        if (normalizedClassName.contains("ohrm") || normalizedClassName.contains("orangehrm")) {
            applicationName = "OrangeHRM";
            applicationUrlProperty = "OrangeHRMUrl";
        } else if (normalizedClassName.contains("skillmatrix")
                || normalizedClassName.contains("skill_matrix")
                || normalizedClassName.matches(".*\\bsm[a-z0-9_.$]*")) {
            applicationName = "Skill Matrix";
            applicationUrlProperty = "ApplicationUrl";
        } else {
            return;
        }

        properties.setProperty("applicationName", applicationName);
        properties.setProperty("suiteName", applicationName + " Suite");
        properties.setProperty(
                "environmentUrl", valueOrDefault(properties.getProperty(applicationUrlProperty), ""));
    }

    public static String formatDuration(long durationInMillis) {
        long seconds = Math.max(0L, durationInMillis) / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        seconds = seconds % 60L;
        minutes = minutes % 60L;
        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static String currentDate() {
        return new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date());
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static String extractRegex(String text, String regexPattern) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        Matcher matcher = Pattern.compile(regexPattern).matcher(text);
        return matcher.find() ? matcher.group(0) : "";
    }

    public static void waitFor(int timeInSeconds) {
        try {
            Thread.sleep(timeInSeconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static Path createReportBundle(Path runDirectory) throws IOException {
        if (runDirectory == null) {
            throw new IllegalArgumentException("runDirectory must not be null");
        }
        Path normalizedRunDirectory = runDirectory.toAbsolutePath().normalize();
        Path bundlePath = normalizedRunDirectory.resolve(normalizedRunDirectory.getFileName() + ".zip");
        Files.deleteIfExists(bundlePath);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(bundlePath));
             Stream<Path> paths = Files.walk(normalizedRunDirectory)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toAbsolutePath().normalize().equals(bundlePath))
                    .toList();

            for (Path file : files) {
                String relative = normalizedRunDirectory.relativize(file).toString().replace('\\', '/');
                zipOutputStream.putNextEntry(new ZipEntry(relative));
                Files.copy(file, zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }
        return bundlePath;
    }

    public static String takeScreenShot(String filename, Page page) {
        if (page == null) {
            return "";
        }
        try {
            Path directory = Paths.get(getCurrentReportDirectory(props));
            Files.createDirectories(directory);
            Path filePath = directory.resolve(filename + "_" + System.currentTimeMillis() + ".png");
            page.screenshot(new Page.ScreenshotOptions().setPath(filePath).setFullPage(true));
            return filePath.toString();
        } catch (IOException ex) {
            logger.debug("Unable to take screenshot for {}", filename, ex);
            return "";
        }
    }

    public static String fileUpload(Page page, String selector, String filePath) {
        if (page == null || selector == null || filePath == null) {
            return "";
        }
        page.locator(selector).setInputFiles(Paths.get(filePath));
        return filePath;
    }

    public static List<String> getFileNames(String directoryPath) {
        File directory = new File(directoryPath);
        List<String> names = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            for (File file : files) {
                names.add(file.getName());
            }
        }
        return names;
    }

    public static String generatePieChart() {
        try {
            Path reportDirectory = Paths.get(getCurrentReportDirectory(props)).toAbsolutePath().normalize();
            String reportFileName = valueOrDefault(
                    props == null ? null : props.getProperty("extentResultMainHtmlFileName"), "Result.html");
            return generatePieChart(reportDirectory.resolve(reportFileName)).toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate test execution chart", ex);
        }
    }

    public static IToolsConnector getTestManagementToolConnector(Properties properties) {
        if (properties == null) {
            return null;
        }
        String tool = properties.getProperty("TestManagementTool");
        if (tool == null || tool.isBlank() || tool.equalsIgnoreCase("None")) {
            return null;
        }
        return switch (tool.trim()) {
            case "Jira" -> JiraConnector.getInstance(properties);
            case "AzureDevOPS" -> AzureDevOpsConnector.getInstance(properties);
            default -> throw new IllegalArgumentException("Unsupported test management tool: " + tool);
        };
    }

    public static String getTestManagementProjectKey(Properties properties) {
        if (properties == null) {
            return "";
        }
        String projectKey = properties.getProperty("TestManagementProjectKey");
        if (projectKey == null || projectKey.isBlank()) {
            projectKey = properties.getProperty("TestManagementProejctKey");
        }
        return projectKey == null ? "" : projectKey.trim();
    }

    public static void sendMail(Properties mailProperties) throws IOException, javax.mail.MessagingException {
        String recipient = requiredProperty(mailProperties, "emailAddress");
        String sender = valueOrDefault(mailProperties.getProperty("emailSender"), "automation@localhost");
        String smtpHost = valueOrDefault(mailProperties.getProperty("emailSMTPServer"), "localhost");
        Path reportDirectory = Paths.get(getCurrentReportDirectory(mailProperties)).toAbsolutePath().normalize();
        String reportFileName = valueOrDefault(
                mailProperties.getProperty("extentResultMainHtmlFileName"), "Result.html");
        Path reportPath = reportDirectory.resolve(reportFileName);

        if (!Files.isRegularFile(reportPath)) {
            throw new IOException("Test execution report was not found: " + reportPath);
        }

        Path chartPath = generatePieChart(reportPath);
        Path reportBundle = createReportBundle(reportDirectory);
        java.util.Properties smtpProperties = new java.util.Properties();
        smtpProperties.put("mail.smtp.host", smtpHost);
        smtpProperties.put("mail.smtp.port", valueOrDefault(mailProperties.getProperty("emailSMTPPort"), "25"));
        smtpProperties.put("mail.smtp.auth", "false");

        Session session = Session.getInstance(smtpProperties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        String applicationName = getEmailApplicationName(mailProperties, reportDirectory);
        message.setSubject("Automation Test Execution Report - " + applicationName, "UTF-8");

        MimeMultipart multipart = new MimeMultipart();
        MimeMultipart related = new MimeMultipart("related");
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(buildEmailHtml(reportPath, mailProperties), "text/html; charset=UTF-8");
        related.addBodyPart(bodyPart);

        MimeBodyPart chartPart = new MimeBodyPart();
        chartPart.setDataHandler(new DataHandler(new FileDataSource(chartPath.toFile())));
        chartPart.setHeader("Content-ID", "<execution-summary-chart>");
        chartPart.setDisposition(MimeBodyPart.INLINE);
        related.addBodyPart(chartPart);

        MimeBodyPart relatedPart = new MimeBodyPart();
        relatedPart.setContent(related);
        multipart.addBodyPart(relatedPart);

        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new DataHandler(new FileDataSource(reportBundle.toFile())));
        attachment.setFileName(reportBundle.getFileName().toString());
        attachment.setDisposition(MimeBodyPart.ATTACHMENT);
        multipart.addBodyPart(attachment);

        message.setContent(multipart);
        Transport.send(message);
    }

    private static String getEmailApplicationName(Properties mailProperties, Path reportDirectory) {
        String applicationName = mailProperties.getProperty("applicationName");
        if (applicationName != null && !applicationName.isBlank()) {
            return applicationName;
        }
        return valueOrDefault(mailProperties.getProperty("suiteName"), reportDirectory.getFileName().toString())
                .replaceFirst("(?i)\\s+Suite$", "");
    }

    private static String buildEmailHtml(Path reportPath) throws IOException {
        return buildEmailHtml(reportPath, new Properties());
    }

    private static String buildEmailHtml(Path reportPath, Properties mailProperties) throws IOException {
        String report = Files.readString(reportPath);
        String started = extractReportValue(report, "Started");
        String ended = extractReportValue(report, "Ended");
        Pattern testPattern = Pattern.compile(
                "<li\\s+class=\"test-item\"\\s+status=\"([^\"]+)\"[^>]*>(.*?)</li>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher testMatcher = testPattern.matcher(report);
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        int total = 0;

        while (testMatcher.find()) {
            String status = testMatcher.group(1);
            switch (status.toLowerCase()) {
                case "pass" -> passed++;
                case "fail" -> failed++;
                case "skip" -> skipped++;
                default -> {
                }
            }
            total++;
        }

        String suiteName = valueOrDefault(mailProperties.getProperty("suiteName"),
                reportPath.getParent().getFileName().toString());
        String applicationName = valueOrDefault(mailProperties.getProperty("applicationName"),
                suiteName.replaceFirst("(?i)\\s+Suite$", ""));
        String environmentUrl = resolveEnvironmentUrl(mailProperties, applicationName);
        String browser = valueOrDefault(mailProperties.getProperty("browser"), "Unavailable");
        String reportType = valueOrDefault(mailProperties.getProperty("testReporter"), "Unavailable");
        String totalDuration = formatRunDuration(started, ended);
        String formattedStarted = formatExecutionTime(started);
        String formattedEnded = formatExecutionTime(ended);
        double passPercentage = total == 0 ? 0.0 : passed * 100.0 / total;
        String headerCellStyle = "border:1px solid #222;padding:8px;text-align:left;width:153px";
        String valueCellStyle = "border:1px solid #222;padding:8px;text-align:left";
        String detailRows = metadataRow("Suite Name", suiteName, headerCellStyle, valueCellStyle)
                + metadataRow("Application", applicationName, headerCellStyle, valueCellStyle)
                + "<tr><th style=\"" + headerCellStyle + "\">Environment URL</th><td style=\""
                + valueCellStyle + "\">" + formatUrl(environmentUrl) + "</td></tr>"
                + metadataRow("Browser", browser, headerCellStyle, valueCellStyle)
                + metadataRow("Execution Start Time", formattedStarted, headerCellStyle, valueCellStyle)
                + metadataRow("Execution End Time", formattedEnded, headerCellStyle, valueCellStyle)
                + metadataRow("Total Duration", totalDuration, headerCellStyle, valueCellStyle)
                + metadataRow("Report Type", reportType, headerCellStyle, valueCellStyle);
        String defectRows = buildJiraDefectRows(mailProperties, headerCellStyle, valueCellStyle);

        return "<html><body style=\"font-family:Arial,sans-serif;color:#222;font-size:14px\">"
                + "<p>Dear Team,</p>"
                + "<p>The automation test suite has been executed. Please find the execution details below:</p>"
                + "<table cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;width:650px;"
                + "border:1px solid #222\">" + detailRows + "</table>"
                + "<h3 style=\"color:#1686c9;margin-top:28px\">Execution Summary</h3>"
                + "<table cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse:collapse;"
                + "border:1px solid #222;text-align:center\">"
                + "<tr style=\"background:#087eae;color:#fff\"><th style=\"border:1px solid #222\">Total</th>"
                + "<th style=\"border:1px solid #222\">Passed</th><th style=\"border:1px solid #222\">Failed</th>"
                + "<th style=\"border:1px solid #222\">Skipped</th></tr>"
                + "<tr><td style=\"border:1px solid #222\"><b>" + total
                + "</b></td><td style=\"border:1px solid #222;color:#008000\"><b>" + passed
                + "</b></td><td style=\"border:1px solid #222;color:#c00\"><b>" + failed
                + "</b></td><td style=\"border:1px solid #222;color:#d99000\"><b>"
                + skipped + "</b></td></tr></table>"
                + String.format(Locale.ROOT,
                        "<p style=\"color:#1686c9;font-weight:bold;margin-top:20px\">Pass Percentage : %.2f%%</p>",
                        passPercentage)
                + defectRows
                + "<p><img alt=\"Execution summary chart\" src=\"cid:execution-summary-chart\" "
                + "style=\"max-width:700px;width:100%;height:auto\"></p>"
                + "<p>Detailed report is attached as a ZIP file.</p>"
                + "<p>Please extract and open <b>" + escapeHtml(valueOrDefault(
                        mailProperties.getProperty("extentResultMainHtmlFileName"), "Result.html"))
                + "</b> to view complete execution details.</p></body></html>";
    }

    private static String buildJiraDefectRows(
            Properties mailProperties, String headerCellStyle, String valueCellStyle) {
        if (TestResultListener.jiraDefectsByTest.isEmpty()) {
            return "";
        }
        String jiraUrl = valueOrDefault(mailProperties.getProperty("TestManagementToolURL"), "").trim();
        while (jiraUrl.endsWith("/")) {
            jiraUrl = jiraUrl.substring(0, jiraUrl.length() - 1);
        }
        StringBuilder rows = new StringBuilder();
        for (Map.Entry<String, String> defect : new java.util.TreeMap<>(
                TestResultListener.jiraDefectsByTest).entrySet()) {
            String defectId = escapeHtml(defect.getValue());
            String displayedId = jiraUrl.isEmpty()
                    ? defectId
                    : "<a href=\"" + escapeHtml(jiraUrl + "/browse/" + defect.getValue())
                            + "\">" + defectId + "</a>";
            rows.append("<tr><td style=\"").append(valueCellStyle).append("\">")
                    .append(escapeHtml(defect.getKey())).append("</td><td style=\"")
                    .append(valueCellStyle).append("\">").append(displayedId).append("</td></tr>");
        }
        return "<h3 style=\"color:#1686c9;margin-top:28px\">Jira Defects</h3>"
                + "<table cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse:collapse;"
                + "border:1px solid #222\"><tr><th style=\"" + headerCellStyle
                + "\">Test Script</th><th style=\"" + headerCellStyle + "\">Defect ID</th></tr>"
                + rows + "</table>";
    }

    private static String metadataRow(
            String label, String value, String headerCellStyle, String valueCellStyle) {
        return "<tr><th style=\"" + headerCellStyle + "\">" + escapeHtml(label)
                + "</th><td style=\"" + valueCellStyle + "\">" + escapeHtml(value) + "</td></tr>";
    }

    private static String resolveEnvironmentUrl(Properties properties, String applicationName) {
        String configuredUrl = properties.getProperty("environmentUrl");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return configuredUrl;
        }
        if ("OrangeHRM".equalsIgnoreCase(applicationName)) {
            return valueOrDefault(properties.getProperty("OrangeHRMUrl"), "");
        }
        if ("Skill Matrix".equalsIgnoreCase(applicationName)) {
            return valueOrDefault(properties.getProperty("ApplicationUrl"), "");
        }
        return "";
    }

    private static String formatUrl(String url) {
        if (url.isEmpty()) {
            return "Unavailable";
        }
        String escapedUrl = escapeHtml(url);
        return "<a href=\"" + escapedUrl + "\">" + escapedUrl + "</a>";
    }

    private static String formatRunDuration(String started, String ended) {
        Date start = parseReportDate(started);
        Date finish = parseReportDate(ended);
        if (start == null || finish == null) {
            return "Unavailable";
        }
        return formatDuration(finish.getTime() - start.getTime());
    }

    private static String formatExecutionTime(String timestamp) {
        Date date = parseReportDate(timestamp);
        return date == null
                ? valueOrDefault(timestamp, "Unavailable")
                : new SimpleDateFormat("dd MMM yyyy hh:mm:ss a", Locale.ENGLISH).format(date);
    }

    private static Date parseReportDate(String timestamp) {
        if (timestamp == null || timestamp.isBlank() || timestamp.equals("Unavailable")) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
            formatter.setLenient(false);
            return formatter.parse(timestamp.replace('\u202f', ' ').replace('\u00a0', ' ').trim());
        } catch (java.text.ParseException ex) {
            return null;
        }
    }

    private static Path generatePieChart(Path reportPath) throws IOException {
        if (!Files.isRegularFile(reportPath)) {
            throw new IOException("Test execution report was not found: " + reportPath);
        }

        int passed = 0;
        int failed = 0;
        int skipped = 0;
        Matcher statusMatcher = Pattern.compile(
                "<li\\s+class=\"test-item\"\\s+status=\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE).matcher(Files.readString(reportPath));
        while (statusMatcher.find()) {
            switch (statusMatcher.group(1).toLowerCase()) {
                case "pass" -> passed++;
                case "fail" -> failed++;
                case "skip" -> skipped++;
                default -> {
                }
            }
        }
        if (passed + failed + skipped == 0) {
            throw new IOException("No test outcomes were found in the report: " + reportPath);
        }

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Passed", passed);
        dataset.setValue("Failed", failed);
        dataset.setValue("Skipped", skipped);
        JFreeChart chart = ChartFactory.createPieChart(
                "Test Execution Summary", dataset, true, true, false);
        Path chartPath = reportPath.getParent().resolve("chart.png");
        ChartUtils.saveChartAsPNG(chartPath.toFile(), chart, 700, 450);
        return chartPath;
    }

    private static String extractReportValue(String report, String label) {
        Pattern valuePattern = Pattern.compile(
                "<p class=\"m-b-0\">\\s*" + Pattern.quote(label)
                        + "\\s*</p>\\s*<h3>(.*?)</h3>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = valuePattern.matcher(report);
        return matcher.find() ? matcher.group(1).trim() : "Unavailable";
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String getScreenShot(ITestResult result, Properties props) {
        if (result == null) {
            return "";
        }
        Object driver = result.getAttribute("driver");
        if (driver instanceof Page page) {
            return takeScreenShot(result.getName(), page);
        }
        return "";
    }
}
