package wns.automation.core;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

public class ExtentTestManager {

    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();

    public static void setExtentTest(ExtentTest test) {
        extentTest.set(test);
    }

    public static ExtentTest getExtentTest() {
        return extentTest.get();
    }

    public static void log(Status status, String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.log(status, message);
        }
    }

    public static void remove() {
        extentTest.remove();
    }
}
