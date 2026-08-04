package listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;

import base.BaseTest;
import org.openqa.selenium.WebDriver;
import utils.Log;
import utils.ScreenshotUtil;

/**
 * TestNG Listener that automatically captures a screenshot on test failure.
 * Eliminates the need for manual screenshot capture in every test's catch block.
 *
 * Register in testng.xml:
 * <pre>
 * {@code
 *   <listeners>
 *     <listener class-name="listeners.ScreenshotOnFailureListener"/>
 *   </listeners>
 * }
 * </pre>
 *
 * @author Yash Shrivastava
 */
public class ScreenshotOnFailureListener implements ITestListener {

	@Override
	public void onTestFailure(ITestResult result) {
		WebDriver driver = BaseTest.getActiveDriver();
		if (driver != null) {
			String testName = result.getMethod().getMethodName();
			String screenshotPath = ScreenshotUtil.captureScreenshot(driver, "FAIL_" + testName);
			Log.error(String.format("[ScreenshotListener] Test '%s' FAILED | Screenshot: %s",
					testName, screenshotPath));
		}
	}

	@Override
	public void onTestStart(ITestResult result) {
		Log.info("[ScreenshotListener] Starting: " + result.getMethod().getMethodName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		Log.info("[ScreenshotListener] PASSED: " + result.getMethod().getMethodName());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		Log.warn("[ScreenshotListener] SKIPPED: " + result.getMethod().getMethodName());
	}
}
