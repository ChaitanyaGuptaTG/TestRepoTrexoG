package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Single logging entry point for the framework.
 *
 * <p>{@code Log.info(...)} writes to BOTH:
 * <ul>
 *   <li>the IDE terminal / log file (via SLF4J - with timestamps and thread names)</li>
 *   <li>the Extent report (via the ExtentTest bound to the current thread)</li>
 * </ul>
 *
 * <p>{@code Log.debug(...)} writes to the terminal/file ONLY, so noisy diagnostic
 * dumps (zip manifests, candidate URLs) never clutter the Extent report.
 *
 * <p>Everything degrades gracefully: if no ExtentTest is bound to this thread
 * (e.g. running a page object outside TestNG), the Extent calls are skipped and
 * the terminal output still works. So this is a safe drop-in for System.out.println.
 *
 * <p><b>Wiring:</b> in your existing Extent TestNG listener:
 * <pre>
 *   public void onTestStart(ITestResult r)   { Log.setTest(extent.createTest(r.getName())); }
 *   public void onTestSuccess(ITestResult r) { Log.clear(); }
 *   public void onTestFailure(ITestResult r) { Log.clear(); }
 *   public void onTestSkipped(ITestResult r) { Log.clear(); }
 * </pre>
 */
public final class Log {

	private static final Logger LOGGER = LoggerFactory.getLogger("AutomationLog");
	private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

	private Log() {
		// utility class - no instances
	}

	// ───────────────── binding ─────────────────

	/** Bind an ExtentTest to the current thread. Call from onTestStart. */
	public static void setTest(ExtentTest test) {
		CURRENT_TEST.set(test);
	}

	/** Unbind. ALWAYS call this when a test finishes, or ThreadLocals leak across parallel runs. */
	public static void clear() {
		CURRENT_TEST.remove();
	}

	// ───────────────── logging ─────────────────

	/** Normal step. Terminal + Extent. */
	public static void info(String message, Object... args) {
		String text = format(message, args);
		LOGGER.info(text);
		toExtent(Status.INFO, text);
	}

	/** Successful verification. Terminal + Extent (shown green). */
	public static void pass(String message, Object... args) {
		String text = format(message, args);
		LOGGER.info(text);
		toExtent(Status.PASS, text);
	}

	/** Something suspicious but not fatal. Terminal + Extent (shown amber). */
	public static void warn(String message, Object... args) {
		String text = format(message, args);
		LOGGER.warn(text);
		toExtent(Status.WARNING, text);
	}

	/** Failure detail. Terminal + Extent (shown red). */
	public static void fail(String message, Object... args) {
		String text = format(message, args);
		LOGGER.error(text);
		toExtent(Status.FAIL, text);
	}

	/** Error detail. Terminal + Extent (shown red). Same as {@link #fail}, named to match callers that log exceptions. */
	public static void error(String message, Object... args) {
		fail(message, args);
	}

	/**
	 * Diagnostic detail. Terminal / log file ONLY - deliberately NOT sent to Extent,
	 * so the report stays a clean narrative while the full dump remains one
	 * log-level flag away when you need it.
	 */
	public static void debug(String message, Object... args) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format(message, args));
		}
	}

	// ───────────────── internals ─────────────────

	private static String format(String message, Object... args) {
		return (args == null || args.length == 0)
				? message
				: MessageFormatter.arrayFormat(message, args).getMessage();
	}

	private static void toExtent(Status status, String text) {
		ExtentTest test = CURRENT_TEST.get();
		if (test != null) {
			test.log(status, text);
		}
	}
}