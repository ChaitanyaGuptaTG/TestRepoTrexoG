package base;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import utils.ConfigReader;
import utils.Log;
import utils.ScreenshotUtil;
import utils.PlatformRetryReportStore;

/**
 * Registered here (not only in testng.xml) so failure screenshots and Extent
 * reporting still fire when a test class is run directly from the IDE, which
 * builds its own temp suite and ignores testng.xml's &lt;listeners&gt; block.
 */
@Listeners(listeners.ExtentTestListener.class)
public class BaseTest {


	// 🔥 Thread-safe WebDriver for parallel tests
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	protected Properties config;

	/**
	 * Get WebDriver for current thread
	 */
	public WebDriver getDriver() {
		return driver.get();
	}
	
	/**
	 * Get active WebDriver for screenshot captures on errors
	 */
	public static WebDriver getActiveDriver() {
		return driver.get();
	}
	
	 
	
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
	    ScreenshotUtil.cleanScreenshotsDir();
	    ScreenshotUtil.cleanRetryArtifactsDir();
	    PlatformRetryReportStore.clear();
	    utils.DataComparisonReportStore.clear();
	}

	/**
	 * Initialize WebDriver before each test Supports a browser parameter (default:
	 * Chrome)
	 */
	@BeforeClass(alwaysRun = true)
	@Parameters("browser")
	public void setup(@Optional("chrome") String browser) throws Exception {
		
		// ✅ Safe default if browser is null or empty
		if (browser == null || browser.isBlank()) {
			Log.warn("No browser parameter provided, defaulting to Chrome");
			browser = "chrome";
		}

		// Load configuration
		config = ConfigReader.loadConfig();

		Log.info("Starting browser: " + browser);

		switch (browser.toLowerCase()) {
		case "chrome":
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();

			// Route downloads directly to execution-run or legacy directory
			Map<String, Object> prefs = new HashMap<>();
			String downloadPath = utils.ExecutionRunManager.isRunSpecificEnabled()
					? utils.ExecutionRunManager.getDownloadsDir().resolve("incoming").toAbsolutePath().toString()
					: new File("output/incoming").getAbsolutePath();
			prefs.put("download.default_directory", downloadPath);
			prefs.put("download.prompt_for_download", false);
			prefs.put("download.directory_upgrade", true);
			prefs.put("safebrowsing.enabled", true);
			options.setExperimentalOption("prefs", prefs);

			boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));

			if (isHeadless) {
				options.addArguments("--headless=new");
				// GPU is unavailable in headless mode on Linux, so disable it there.
				options.addArguments("--disable-gpu");
			} else {
				options.addArguments("--window-size=1920,1080");
			}

			boolean runNoSandbox = Boolean.parseBoolean(System.getProperty("chrome.nosandbox", "false"));
			if (runNoSandbox) {
				Log.warn("Browser security sandbox is disabled (--no-sandbox) via system property!");
				options.addArguments("--no-sandbox");
			}
			options.addArguments("--disable-extensions");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--disable-session-crashed-bubble");
			// Tutorial videos are started programmatically during verification; Chrome's
			// default autoplay policy blocks that without a fresh user gesture.
			options.addArguments("--autoplay-policy=no-user-gesture-required");
		
			

			driver.set(new ChromeDriver(options));
			break;

		// ✅ Future-proof: add other browsers here
		// case "firefox":
		// driver.set(new FirefoxDriver());
		// break;

		default:
			Log.warn("Unsupported browser: " + browser + ". Defaulting to Chrome.");
			WebDriverManager.chromedriver().setup();
			driver.set(new ChromeDriver());
		}

		// Safety check
		if (getDriver() == null) {
			throw new RuntimeException("WebDriver initialization failed!");
		}

		// Common setup
		getDriver().manage().deleteAllCookies();
		// NOTE: Implicit wait intentionally removed to prevent wait-stacking with explicit waits.
		// All waits are now handled through explicit WebDriverWait in BasePage/JuneChatPage.

		// Navigate to base URL from config
		String url = config.getProperty("url");
		if (url == null || url.isBlank()) {
			throw new RuntimeException("URL not found in config!");
		}
		getDriver().get(url);
	}

	/**
	 * Quit WebDriver after each test
	 */
	@AfterClass(alwaysRun = true)
	public void tearDown() 
	{
		if (getDriver() != null) {
			Log.info("Closing browser");
			getDriver().quit();
			driver.remove(); // ✅ Very important for ThreadLocal cleanup
		}
		
}
	
}
