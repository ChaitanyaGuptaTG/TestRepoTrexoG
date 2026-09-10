package utils;

import com.aventstack.extentreports.ExtentTest;

public class ExtentTestManager {

    // Holds the ExtentTest instance for safe parallel execution
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void setTest(ExtentTest extentTest) {
        test.set(extentTest);
    }
}