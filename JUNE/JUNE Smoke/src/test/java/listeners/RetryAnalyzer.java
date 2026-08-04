package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import utils.Log;

/**
 * TestNG Retry Analyzer for handling transient UI failures.
 * Automatically retries failed tests up to {@value #MAX_RETRY} times before
 * marking them as failed. This is complementary to PlatformRetryUtil which
 * handles in-test platform-level retries.
 *
 * Usage:
 * <pre>
 *   {@literal @}Test(retryAnalyzer = RetryAnalyzer.class)
 *   public void myTest() { ... }
 * </pre>
 *
 * Or register globally via testng.xml listener.
 *
 * @author Yash Shrivastava
 */
public class RetryAnalyzer implements IRetryAnalyzer {

	private int retryCount = 0;
	private static final int MAX_RETRY = 2;

	@Override
	public boolean retry(ITestResult result) {
		if (retryCount < MAX_RETRY) {
			retryCount++;
			Log.warn(String.format("[RetryAnalyzer] Retrying test '%s' (attempt %d of %d)",
					result.getName(), retryCount + 1, MAX_RETRY + 1));
			return true;
		}
		return false;
	}
}
