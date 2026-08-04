package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.USTMImageDownloadPage;
import utils.Log;
import utils.OutputFileWorkflowManager;
import utils.PlatformRetryUtil;
import utils.ScreenshotUtil;

/**
 * US TM Image Download workflow smoke test — validates PNG and JPG image download
 * across valid, invalid, and mixed input scenarios.
 *
 * <p>Extends {@link AbstractWorkflowTest} for shared logic but overrides
 * {@link #runImageScenario} to handle the additional format-selection step.</p>
 *
 * @author Yash Shrivastava
 */
public class USTMImageDownloadTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "USTMImageDownload"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "ustmimagedownload"; }

	@Test(priority = 4, description = "USTM Image Download: PNG and JPG format scenarios")
	public void testUSTMImageDownload() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		USTMImageDownloadPage page = new USTMImageDownloadPage(getDriver());
		Log.info("[USTMImageDownload] Navigating to US TM Image Download flow.");

		String messageXpath = config.getProperty("ustmimagedownload.messageXpath",
				config.getProperty("ustrademark.messageXpath", "//p[contains(text() , 'Request ID')]"));
		String invalidMessageXpath = config.getProperty("ustmimagedownload.invalidMessageXpath",
				"//*[contains(normalize-space(),'We were unable to fetch') or contains(normalize-space(),'All application numbers failed') or contains(normalize-space(),'Unable to retrieve images for the provided input.')]");
		String successSnippet = config.getProperty("ustmimagedownload.expectedMessageSnippet",
				config.getProperty("ustrademark.expectedMessageSnippet",
						"You can download the file using the following link: Download. An email notification has also been sent for your reference."));
		String invalidSnippet = config.getProperty("ustmimagedownload.invalidMessageSnippet",
				"Unable to retrieve images for the provided input.");
		String downloadFileContains = config.getProperty("ustmimagedownload.downloadFileContains",
				config.getProperty("ustrademark.downloadFileContains",
						config.getProperty("workflow.downloadFileContains", "")));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("ustmimagedownload.downloadWaitSeconds",
				config.getProperty("ustrademark.downloadWaitSeconds",
						config.getProperty("workflow.downloadWaitSeconds", "10"))));

		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "ustmimagedownload");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "US TM Image Download");

		String validInputs = config.getProperty("ustrademark.validInputs", "98795545,98746094,98612628,98607138,98604783");
		String invalidInputs = config.getProperty("ustrademark.invalidInputs", "9876543453432,213456,13454452346,242325,34");
		String mixedInputs = config.getProperty("ustrademark.mixedInputs",
				"98795545,98746094,98612628,98607138,98604783,98765432,12345,1234,123456789876543");

		// ── PNG scenarios ────────────────────────────────────
		Log.info("[USTMImageDownload] Starting .PNG execution run...");

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid) - PNG")
				.input(validInputs).format("PNG")
				.messageLocator(By.xpath(messageXpath)).expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid-png")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true).build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid) - PNG")
				.input(invalidInputs).format("PNG")
				.messageLocator(By.xpath(invalidMessageXpath)).expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid-png")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed) - PNG")
				.input(mixedInputs).format("PNG")
				.messageLocator(By.xpath(messageXpath)).expectedSnippet(successSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-3-mixed-png")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true).build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		// ── JPG scenarios ────────────────────────────────────
		Log.info("[USTMImageDownload] Starting .JPG execution run...");

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid) - JPG")
				.input(validInputs).format("JPG")
				.messageLocator(By.xpath(messageXpath)).expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid-jpg")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true).build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid) - JPG")
				.input(invalidInputs).format("JPG")
				.messageLocator(By.xpath(invalidMessageXpath)).expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid-jpg")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		runImageScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed) - JPG")
				.input(mixedInputs).format("JPG")
				.messageLocator(By.xpath(messageXpath)).expectedSnippet(successSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-3-mixed-jpg")
				.downloadFileContains(downloadFileContains).downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true).build(), workflowManager);

		Log.info("[USTMImageDownload] Verifying downloaded image files...");
		workflowManager.verifyAll();

		performLogout(page, softAssert);
		softAssert.assertAll();
	}

	// ──────────────────────────────────────────────────────────
	//  Image-specific scenario runner (adds format selection step)
	// ──────────────────────────────────────────────────────────

	/**
	 * Runs a scenario with an additional image format selection step.
	 * Delegates to the parent's message-handling and download logic.
	 */
	private boolean runImageScenario(USTMImageDownloadPage page, SoftAssert softAssert,
			ScenarioConfig scenario, OutputFileWorkflowManager workflowManager) {
		try {
			PlatformRetryUtil.executeWithRetry(getDriver(), config,
					getWorkflowConfigPrefix(), scenario.getName(), () -> {

				Log.info(String.format("[%s] %s (%s) input: %s",
						getWorkflowTag(), scenario.getName(), scenario.getFormat(), scenario.getInput()));

				int beforeCount = page.getMessageCount(scenario.getMessageLocator());
				String beforeLastText = page.getLastMessageText(scenario.getMessageLocator());
				PlatformRetryUtil.ResponseSnapshot snap =
						PlatformRetryUtil.snapshotAcceptedResponses(getDriver());

				page.enterInputValueByLabel("InputBox", scenario.getInput());

				// Image-specific: format selection step
				page.waitForFormatPromptAndSelect(scenario.getFormat());

				String message = PlatformRetryUtil.waitForExpectedMessageOrPlatformError(
						getDriver(), scenario.getMessageLocator(), scenario.getExpectedSnippet(),
						Math.max(scenario.getDownloadWaitSeconds(), 30),
						beforeCount, beforeLastText, snap,
						getWorkflowConfigPrefix(), scenario.getName(), scenario.getInput());

				softAssert.assertNotNull(message,
						scenario.getName() + " (" + scenario.getFormat() + "): Expected message but it stayed empty.");

				if (message != null) {
					processImageResponse(page, softAssert, scenario, workflowManager, message);
				}
			});
			return true;
		} catch (Exception e) {
			String screenshot = ScreenshotUtil.captureScreenshot(getDriver(),
					getWorkflowTag() + "_" + scenario.getName());
			Log.error(String.format("[%s] %s (%s) failed but continuing. Error: %s | Screenshot: %s",
					getWorkflowTag(), scenario.getName(), scenario.getFormat(), e.getMessage(), screenshot));
			softAssert.fail(scenario.getName() + " (" + scenario.getFormat() + ") threw exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Response classification and download processing for image scenarios.
	 * Same logic as AbstractWorkflowTest but with format in log messages.
	 */
	private void processImageResponse(USTMImageDownloadPage page, SoftAssert softAssert,
			ScenarioConfig scenario, OutputFileWorkflowManager workflowManager, String message) {

		boolean acceptedBusiness = PlatformRetryUtil.isAcceptedBusinessResponse(message);
		boolean alternative = PlatformRetryUtil.isAlternativeResponse(message);
		String fmt = scenario.getFormat();

		if (acceptedBusiness) {
			Log.info(String.format("[%s] %s (%s): Accepted platform business response: %s",
					getWorkflowTag(), scenario.getName(), fmt, message));
		} else if (alternative) {
			Log.info(String.format("[%s] %s (%s): Accepted alternative platform response: %s",
					getWorkflowTag(), scenario.getName(), fmt, message));
		} else {
			softAssert.assertTrue(message.contains(scenario.getExpectedSnippet()),
					scenario.getName() + " (" + fmt + "): Expected snippet was not found in message.");
		}

		if (message.contains("Request ID")) {
			if (scenario.isWaitBeforeRequestId()) {
				try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			}
			String requestId = page.extractRequestId(message);
			Log.info(String.format("[%s] %s (%s) Request ID: %s", getWorkflowTag(), scenario.getName(), fmt, requestId));
			downloadViaTrackTask(page, workflowManager, scenario, requestId);

		} else if (alternative) {
			String requestId = PlatformRetryUtil.extractAlternativeRequestId(message);
			Log.info(String.format("[%s] %s (%s) Alternative Request ID: %s",
					getWorkflowTag(), scenario.getName(), fmt, requestId));
			try {
				Log.info(String.format("[%s] Waiting 60 seconds before navigating to Track Task...", getWorkflowTag()));
				Thread.sleep(60000);
			} catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			downloadViaTrackTask(page, workflowManager, scenario, requestId);

		} else if (acceptedBusiness) {
			Log.info(String.format("[%s] %s (%s): No Request ID in accepted response; continuing.",
					getWorkflowTag(), scenario.getName(), fmt));
		} else {
			Log.warn(String.format("[%s] %s (%s): No Request ID found, skipping Track Task + Download.",
					getWorkflowTag(), scenario.getName(), fmt));
		}
	}

	private void downloadViaTrackTask(USTMImageDownloadPage page,
			OutputFileWorkflowManager workflowManager, ScenarioConfig scenario, String requestId) {
		page.openTrackTask("Track Task");
		page.searchTrackTaskByRequestId(requestId);
		OutputFileWorkflowManager.DownloadSnapshot snap = null;
		try {
			if (scenario.isCaptureDownload() && workflowManager != null) {
				snap = workflowManager.snapshotDownloads();
			}
		} catch (java.io.IOException e) {
			Log.error("[USTMImageDownload] Failed to snapshot downloads: " + e.getMessage());
		}
		page.clickDownloadButton("Download");
		try {
			if (scenario.isCaptureDownload() && workflowManager != null && snap != null) {
				workflowManager.captureDownloadedFile(scenario.getScenarioKey(), requestId, snap,
						scenario.getDownloadFileContains(), scenario.getDownloadWaitSeconds());
			}
		} catch (java.io.IOException | InterruptedException e) {
			if (e instanceof InterruptedException) { Thread.currentThread().interrupt(); }
			Log.error("[USTMImageDownload] Failed to capture downloaded file: " + e.getMessage());
		}
	}
}
