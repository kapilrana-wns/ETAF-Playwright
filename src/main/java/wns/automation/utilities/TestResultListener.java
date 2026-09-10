package wns.automation.utilities;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TestResultListener<suiteName> implements ITestListener, ISuiteListener {

    public static int total = 0;
    public static int passed = 0;
    public static int failed = 0;
    public static int skipped = 0;

    public static long executionStartTime;
    public static long executionEndTime;

    public static String suiteName = "";
    public static String applicationName = "";

    public static List<String> jiraDefectIDs =
            Collections.synchronizedList(new ArrayList<>());

    // Accumulates failed+skipped methods across all <test> blocks: Map<className, Set<methodName>>
    private static final Map<String, Set<String>> allFailedByClass = new LinkedHashMap<>();

    @Override
    public void onStart(ISuite suite) {
        suiteName = suite.getName();
        allFailedByClass.clear();
    }

    @Override
    public void onFinish(ISuite suite) {
        // Write testng-failedcases.xml once after the entire suite finishes
        try {
            String projectDir = System.getProperty("user.dir");
            if (!allFailedByClass.isEmpty()) {
                StringBuilder xml = new StringBuilder();
                xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                xml.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n");
                xml.append("<suite name=\"Failed suite [").append(suiteName).append("]\">\n");
                xml.append("  <test name=\"Failed Tests (auto-generated)\">\n");
                xml.append("    <classes>\n");
                for (Map.Entry<String, Set<String>> entry : allFailedByClass.entrySet()) {
                    xml.append("      <class name=\"").append(entry.getKey()).append("\">\n");
                    xml.append("        <methods>\n");
                    for (String method : entry.getValue()) {
                        xml.append("          <include name=\"").append(method).append("\"/>\n");
                    }
                    xml.append("        </methods>\n");
                    xml.append("      </class>\n");
                }
                xml.append("    </classes>\n");
                xml.append("  </test>\n");
                xml.append("</suite>\n");

                Path dest = new File(projectDir + File.separator + "src"
                        + File.separator + "test" + File.separator + "resources"
                        + File.separator + "testng-failedcases.xml").toPath();
                Files.write(dest, xml.toString().getBytes());
                int count = allFailedByClass.values().stream().mapToInt(Set::size).sum();
                System.out.println("testng-failedcases.xml updated with " + count
                        + " failed/skipped test(s) from last run.");
            } else {
                Path dest = new File(projectDir + File.separator + "src"
                        + File.separator + "test" + File.separator + "resources"
                        + File.separator + "testng-failedcases.xml").toPath();
                Files.deleteIfExists(dest);
            }
        } catch (Exception e) {
            System.err.println("Could not generate testng-failedcases.xml: " + e.getMessage());
        }
    }

   /* @Override
    public void onStart(ITestContext context) {

        executionStartTime = System.currentTimeMillis();

        jiraDefectIDs.clear();
        System.out.println("LISTENER onStart EXECUTED");
    }*/

        @Override
        public void onStart(ITestContext context) {

            executionStartTime = System.currentTimeMillis();

            System.out.println("===== TEST START =====");
            System.out.println("Context Name = " + context.getName());

            String className = "";
            if (context.getAllTestMethods().length > 0) {
                className = context.getAllTestMethods()[0].getTestClass().getName();
                System.out.println("Class Name = " + className);
            }

            // Determine Application Name
            if (className.toLowerCase().contains("ohrm")|| className.toLowerCase().contains("orangehrm")) {
                applicationName = "OrangeHRM";
            }
            else if (className.toLowerCase().contains("sm")) {
                applicationName = "Skill Matrix";
            }
            else {
                applicationName = "Automation";
            }

            // Determine Suite Name
            suiteName = applicationName + " Suite";

            jiraDefectIDs.clear();

            System.out.println("Application Name = " + applicationName);
            System.out.println("Suite Name = " + suiteName);
            System.out.println("LISTENER onStart EXECUTED");
        }

    @Override
    public void onFinish(ITestContext context) {

        executionEndTime = System.currentTimeMillis();

        passed = context.getPassedTests().size();
        failed = context.getFailedTests().size();
        skipped = context.getSkippedTests().size();

        total = passed + failed + skipped;

        System.out.println("Execution Completed");
        System.out.println("Total = " + total);
        System.out.println("Passed = " + passed);
        System.out.println("Failed = " + failed);
        System.out.println("Skipped = " + skipped);

        try {

            Properties props = TestUtility.getTestConfig(
                    System.getProperty("user.dir")
                            + "\\src\\test\\java\\testconfig\\test.properties"
            );

            //TestUtility.sendMail(props);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Accumulate failed + skipped methods across <test> blocks
        for (ITestResult result : context.getFailedTests().getAllResults()) {
            allFailedByClass
                .computeIfAbsent(result.getTestClass().getName(), k -> new LinkedHashSet<>())
                .add(result.getMethod().getMethodName());
        }
        for (ITestResult result : context.getSkippedTests().getAllResults()) {
            allFailedByClass
                .computeIfAbsent(result.getTestClass().getName(), k -> new LinkedHashSet<>())
                .add(result.getMethod().getMethodName());
        }

        System.out.println("LISTENER onFinish EXECUTED");
    }
}