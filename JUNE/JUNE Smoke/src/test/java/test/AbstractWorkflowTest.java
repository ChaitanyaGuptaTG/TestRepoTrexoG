package test;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import base.BaseTest;
import pages.JuneChatPage;
import pages.LoginPage;
import utils.Log;
import utils.OutputFileWorkflowManager;
import utils.PlatformRetryUtil;
import utils.ScreenshotUtil;
import utils.WaitUtils;

/**
 * Abstract base class for all JUNE workflow tests.
 * Consolidates the duplicated login, runScenario(), and logout logic
 * that was previously copy-pasted across 7 test classes.
 *
 * <p>Subclasses need to provide:</p>
 * <ul>
 *   <li>{@link #getWorkflowTag()} — log tag, e.g. "USPatentAllScenarios"</li>
 *   <li>{@link #getWorkflowConfigPrefix()} — config key prefix, e.g. "uspatent"</li>
 * </ul>
 *
 * @author Yash Shrivastava
 */
public abstract class AbstractWorkflowTest extends BaseTest {

	/** Log tag for this workflow, e.g. "USPatentAllScenarios" */
	protected abstract String getWorkflowTag();

	/** Config key prefix, e.g. "uspatent", "autrademark", "globalpatent" */
	protected abstract String getWorkflowConfigPrefix();

	// ──────────────────────────────────────────────────────────
	//  Login / Logout helpers (Item 8: DRY login)
	// ──────────────────────────────────────────────────────────

	/**
	 * Performs login and waits for the page to load.
	 * Eliminates the 5 duplicated login blocks across all test classes.
	 */
	protected void performLogin() {
		LoginPage login = new LoginPage(getDriver());
		Log.info(String.format("[%s] Logging in.", getWorkflowTag()));
		login.enterUsername(config.getProperty("username"));
		login.enterPassword(config.getProperty("password"));
		login.clickSignin();
		WaitUtils.waitForPageToLoadCompletely(getDriver(), 5);
		
		// Explicitly wait for the platform welcome text to be visible to ensure successful landing on the home page
		try {
			new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(30))
				.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
					org.openqa.selenium.By.xpath("//*[contains(text(),'Welcome to the TREXO Platform')]")
				));
		} catch (Exception e) {
			String path = ScreenshotUtil.captureScreenshot(getDriver(), getWorkflowTag() + "_LoginFailure");
			Log.error("[" + getWorkflowTag() + "] Timeout waiting for platform welcome screen to load: " + e.getMessage() + " | Screenshot: " + path);
			throw new RuntimeException("Login failed: platform welcome screen not visible after 30 seconds.", e);
		}
	}

	/**
	 * Performs logout and asserts the login button is visible.
	 */
	protected void performLogout(JuneChatPage page, SoftAssert softAssert) {
		Log.info(String.format("[%s] Logging out.", getWorkflowTag()));
		page.logoutFromProfileMenu("Logout");
		org.openqa.selenium.WebElement submitBtn = new org.openqa.selenium.support.ui.WebDriverWait(getDriver(), java.time.Duration.ofSeconds(20))
				.until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@type='submit']")));
		Assert.assertTrue(submitBtn.isDisplayed(), "Login button is not visible after logout.");
	}

	// ──────────────────────────────────────────────────────────
	//  Shared runScenario() (Items 4 & 2.3: consolidated logic)
	// ──────────────────────────────────────────────────────────

	/**
	 * Executes a single scenario using the provided {@link ScenarioConfig}.
	 * Handles input submission, message waiting, response classification,
	 * Request ID extraction, Track Task navigation, and file download.
	 *
	 * @param page            the Page Object for this workflow
	 * @param softAssert      the SoftAssert collector
	 * @param scenario        the scenario configuration
	 * @param workflowManager the download workflow manager (may be null)
	 * @return true if the scenario completed without exceptions
	 */
	protected boolean runScenario(JuneChatPage page, SoftAssert softAssert, ScenarioConfig scenario,
			OutputFileWorkflowManager workflowManager) {
		try {
			PlatformRetryUtil.executeWithRetry(getDriver(), config,
					getWorkflowConfigPrefix(), scenario.getName(), () -> {

				Log.info(String.format("[%s] %s input: %s", getWorkflowTag(), scenario.getName(), scenario.getInput()));

				int beforeCount = page.getMessageCount(scenario.getMessageLocator());
				String beforeLastText = page.getLastMessageText(scenario.getMessageLocator());
				PlatformRetryUtil.ResponseSnapshot snapshot =
						PlatformRetryUtil.snapshotAcceptedResponses(getDriver());

				page.enterInputValueByLabel("InputBox", scenario.getInput());

				String message = PlatformRetryUtil.waitForExpectedMessageOrPlatformError(
						getDriver(), scenario.getMessageLocator(), scenario.getExpectedSnippet(),
						Math.max(scenario.getDownloadWaitSeconds(), 90),
						beforeCount, beforeLastText, snapshot,
						getWorkflowConfigPrefix(), scenario.getName(), scenario.getInput());

				softAssert.assertNotNull(message,
						scenario.getName() + ": Expected message but it stayed empty.");

				if (message != null) {
					classifyAndProcessResponse(page, softAssert, scenario, workflowManager, message);
				}
			});
			return true;
		} catch (Exception e) {
			String screenshotPath = ScreenshotUtil.captureScreenshot(getDriver(),
					getWorkflowTag() + "_" + scenario.getName());
			Log.error(String.format("[%s] %s failed but continuing. Error: %s | Screenshot: %s",
					getWorkflowTag(), scenario.getName(), e.getMessage(), screenshotPath));
			softAssert.fail(scenario.getName() + " threw exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Classifies the response (accepted, alternative, or normal) and processes
	 * the Request ID + download if applicable. Consolidates the duplicated
	 * if/else-if/else chain and the download logic that was previously
	 * copy-pasted in two branches per test class.
	 */
	private void classifyAndProcessResponse(JuneChatPage page, SoftAssert softAssert,
			ScenarioConfig scenario, OutputFileWorkflowManager workflowManager, String message) {

		boolean acceptedBusiness = PlatformRetryUtil.isAcceptedBusinessResponse(message);
		boolean alternative = PlatformRetryUtil.isAlternativeResponse(message);

		// Log response type
		if (acceptedBusiness) {
			Log.info(String.format("[%s] %s: Accepted platform business response without retry: %s",
					getWorkflowTag(), scenario.getName(), message));
		} else if (alternative) {
			Log.info(String.format("[%s] %s: Accepted alternative platform response: %s",
					getWorkflowTag(), scenario.getName(), message));
		} else {
			softAssert.assertTrue(message.contains(scenario.getExpectedSnippet()),
					scenario.getName() + ": Expected snippet was not found in message.");
		}

		// Process Request ID → Track Task → Download
		if (message.contains("Request ID")) {
			stabilizeBeforeRequestId(scenario);
			String requestId = page.extractRequestId(message);
			Log.info(String.format("[%s] %s Request ID: %s", getWorkflowTag(), scenario.getName(), requestId));
			processTrackTaskAndDownload(page, workflowManager, scenario, requestId);

		} else if (alternative) {
			String requestId = PlatformRetryUtil.extractAlternativeRequestId(message);
			Log.info(String.format("[%s] %s Alternative Request ID: %s",
					getWorkflowTag(), scenario.getName(), requestId));
			waitForAlternativeResponse();
			processTrackTaskAndDownload(page, workflowManager, scenario, requestId);

		} else if (acceptedBusiness) {
			Log.info(String.format("[%s] %s: Accepted response did not include a Request ID; continuing.",
					getWorkflowTag(), scenario.getName()));
		} else {
			Log.warn(String.format("[%s] %s: No Request ID found, skipping Track Task + Download.",
					getWorkflowTag(), scenario.getName()));
		}
	}

	/**
	 * Consolidated Track Task + Download logic. Previously duplicated in
	 * two branches (normal Request ID and alternative Request ID) in every test class.
	 */
	private void processTrackTaskAndDownload(JuneChatPage page,
			OutputFileWorkflowManager workflowManager, ScenarioConfig scenario, String requestId) {

		page.openTrackTask("Track Task");
		page.searchTrackTaskByRequestId(requestId);

		OutputFileWorkflowManager.DownloadSnapshot snap = null;
		try {
			if (scenario.isCaptureDownload() && workflowManager != null) {
				snap = workflowManager.snapshotDownloads();
			}
		} catch (java.io.IOException e) {
			Log.error(String.format("[%s] Failed to snapshot downloads: %s", getWorkflowTag(), e.getMessage()));
		}

		page.clickDownloadButton("Download");

		try {
			if (scenario.isCaptureDownload() && workflowManager != null && snap != null) {
				workflowManager.captureDownloadedFile(
						scenario.getScenarioKey(), requestId, snap,
						scenario.getDownloadFileContains(), scenario.getDownloadWaitSeconds());
			}
		} catch (java.io.IOException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			Log.error(String.format("[%s] Failed to capture downloaded file: %s", getWorkflowTag(), e.getMessage()));
		}
	}

	/** Optional wait before extracting Request ID (mixed scenarios). */
	private void stabilizeBeforeRequestId(ScenarioConfig scenario) {
		if (scenario.isWaitBeforeRequestId()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/** Wait before Track Task for alternative responses. */
	private void waitForAlternativeResponse() {
		try {
			Log.info(String.format("[%s] Pause briefly before navigating to Track Task...", getWorkflowTag()));
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
