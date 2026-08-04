package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.GlobalPatentAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;
import utils.PlatformRetryUtil;
import utils.ScreenshotUtil;

import java.time.Duration;

/**
 * Global Patent workflow smoke test — validates valid, invalid, alert, and mixed scenarios.
 *
 * <p>Extends {@link AbstractWorkflowTest} for shared logic but overrides scenario
 * execution to handle Global Patent-specific features: multi-line input processing,
 * alert/popup handling, and country code validation.</p>
 *
 * @author Yash Shrivastava
 */
public class GlobalPatentAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "GlobalPatentAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "globalpatent"; }

	@Test(priority = 8, description = "Global Patent: valid, invalid, alert, and mixed input scenarios")
	public void testGlobalPatentAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		GlobalPatentAllScenariosPage page = new GlobalPatentAllScenariosPage(getDriver());
		Log.info("[GlobalPatentAllScenarios] Navigating to Global Patent flow.");

		// Config
		String messageXpath = config.getProperty("globalpatent.messageXpath",
				"//*[contains(normalize-space(),'Your task with Request ID') and contains(normalize-space(),'Download')]");
		String invalidMessageXpath = config.getProperty("globalpatent.invalidMessageXpath",
				"//*[contains(normalize-space(),'We were unable to fetch') and contains(normalize-space(),'publication numbers')]");
		String successSnippet = config.getProperty("globalpatent.expectedMessageSnippet",
				"You can download the file using the following link: Download. An email notification has also been sent for your reference.");
		String invalidSnippet = config.getProperty("globalpatent.invalidMessageSnippet",
				"We were unable to fetch the data for the input provided. Please provide valid publication numbers and try again.");
		String downloadFileContains = config.getProperty("globalpatent.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("globalpatent.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "10")));
		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "globalpatent");

		try {
			page.clickDashboardTile("JUNE");
			page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "Global Patent");

			// Initial response validation
			String initialExpectedMsg = "Please enter the publication/patent numbers prefixed with the country code, separated by new line(s). (Maximum: 500)";
			Log.info("[GlobalPatentAllScenarios] Validating initial response message.");
			boolean initialMsgVerified = page.verifyInitialResponse(initialExpectedMsg, 20);
			softAssert.assertTrue(initialMsgVerified, "Initial response message was not displayed: " + initialExpectedMsg);
		} catch (Exception e) {
			String screenshot = utils.ScreenshotUtil.captureScreenshot(getDriver(), "GlobalPatent_Setup_Failure");
			Log.error("[GlobalPatentAllScenarios] Setup failed. Screenshot: " + screenshot + ". Error: " + e.getMessage());
			throw e;
		}

		String countryCodeValidationSnippet = "Please enter a valid country code for the publication number provided.";

		// Input data
		String s1Input = config.getProperty("globalpatent.scenario1.inputs",
				"EP3712170A1,WO2013184912A2,WO2015024060A1,US20180066055A1,WO2019030377A1,CN104292334A,CN104974253A,EP1137436A1");
		String s2Input = config.getProperty("globalpatent.scenario2.inputs",
				"qa876543a,ok9876545678v,yug098765,kojihugyionl365,tr06987656789k,ok054323,cv018530674");
		String s3Input = config.getProperty("globalpatent.scenario3.inputs",
				"23456h,09876543fgh");
		String s4Input = config.getProperty("globalpatent.scenario4.inputs",
				"EP3712170A1,WO2013184912A2,WO2015024060A1,US20180066055A1,WO2019030377A1,CN104292334A,CN104974253A,EP1137436A1,qa876543a,ok9876545678v,yug098765,kojihugyionl365,tr06987656789k,ok054323,cv018530674");

		// Scenario 1: Valid
		boolean s1 = runGlobalPatentScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input(s1Input)
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		// Scenario 2: Invalid
		boolean s2 = runGlobalPatentScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input(s2Input)
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		// Scenario 3: Validation response expected (in-chat message validation)
		By s3Locator = By.xpath(
				"//p[contains(normalize-space(), '" + countryCodeValidationSnippet + "')] | " +
				"//span[contains(normalize-space(), '" + countryCodeValidationSnippet + "')] | " +
				"//div[contains(normalize-space(), '" + countryCodeValidationSnippet + "')]");
		boolean s3 = runGlobalPatentScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (alert)")
				.input(s3Input)
				.messageLocator(s3Locator)
				.expectedSnippet(countryCodeValidationSnippet)
				.scenarioKey("scenario-3-alert")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.alertExpected(false)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		// Scenario 4: Mixed
		boolean s4 = runGlobalPatentScenario(page, softAssert, ScenarioConfig.builder("Scenario 4 (mixed)")
				.input(s4Input)
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-4-mixed")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);

		// Phase 2: Output verification
		if (s1 && s4) {
			try { workflowManager.verifyAll(); }
			catch (Exception | AssertionError e) {
				Log.error("[GlobalPatentAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[GlobalPatentAllScenarios] Skipping Phase 2 — one or both successful scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);

		softAssert.assertAll();
	}

	// ──────────────────────────────────────────────────────────
	//  Global Patent-specific scenario runner
	// ──────────────────────────────────────────────────────────

	/**
	 * Runs a Global Patent scenario with multi-line input processing and
	 * optional alert handling.
	 */
	private String sanitizeGlobalPatentInput(String input) {
		if (input == null) return "";
		String processed = input.replace(",|", "\n").replace("|", "\n").replace(",", "\n");
		String[] lines = processed.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) continue;
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(trimmed);
		}
		return sb.toString();
	}

	private boolean runGlobalPatentScenario(GlobalPatentAllScenariosPage page, SoftAssert softAssert,
			ScenarioConfig scenario, OutputFileWorkflowManager workflowManager) {
		try {
			final String processedInput = sanitizeGlobalPatentInput(scenario.getInput());

			PlatformRetryUtil.executeWithRetry(getDriver(), config,
					getWorkflowConfigPrefix(), scenario.getName(), () -> {

				Log.info(String.format("[%s] %s input: %s", getWorkflowTag(), scenario.getName(), processedInput));

				if (scenario.isAlertExpected()) {
					page.setAlertExpected(true);
					page.enterInputValueByLabel("InputBox", processedInput);
					Log.info("[GlobalPatentAllScenarios] Waiting for alert pop-up...");
					String alertText = page.handleAlertAndGetText(20);
					Log.info("[GlobalPatentAllScenarios] Alert received: " + alertText);
					softAssert.assertNotNull(alertText, scenario.getName() + ": Expected alert but it stayed empty.");
					if (alertText != null) {
						softAssert.assertTrue(alertText.contains(scenario.getExpectedSnippet()),
								scenario.getName() + ": Expected alert snippet was not found: " + scenario.getExpectedSnippet());
					}
				} else {
					page.setAlertExpected(false);
					int beforeCount = page.getMessageCount(scenario.getMessageLocator());
					String beforeLastText = page.getLastMessageText(scenario.getMessageLocator());
					PlatformRetryUtil.ResponseSnapshot snap =
							PlatformRetryUtil.snapshotAcceptedResponses(getDriver());

					page.enterInputValueByLabel("InputBox", processedInput);

					String message = PlatformRetryUtil.waitForExpectedMessageOrPlatformError(
							getDriver(), scenario.getMessageLocator(), scenario.getExpectedSnippet(),
							Math.max(scenario.getDownloadWaitSeconds(), 120),
							beforeCount, beforeLastText, snap,
							getWorkflowConfigPrefix(), scenario.getName(), processedInput);

					softAssert.assertNotNull(message, scenario.getName() + ": Expected message but it stayed empty.");
					if (message != null) {
						processGlobalPatentResponse(page, softAssert, scenario, workflowManager, message);
					}
				}
			});
			return true;
		} catch (Exception e) {
			String screenshot = ScreenshotUtil.captureScreenshot(getDriver(),
					"GlobalPatentAllScenarios_" + scenario.getName());
			Log.error(String.format("[%s] %s failed but continuing. Error: %s | Screenshot: %s",
					getWorkflowTag(), scenario.getName(), e.getMessage(), screenshot));
			softAssert.fail(scenario.getName() + " threw exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Response classification and download processing for Global Patent scenarios.
	 */
	private void processGlobalPatentResponse(GlobalPatentAllScenariosPage page, SoftAssert softAssert,
			ScenarioConfig scenario, OutputFileWorkflowManager workflowManager, String message) {

		boolean acceptedBusiness = PlatformRetryUtil.isAcceptedBusinessResponse(message);
		boolean alternative = PlatformRetryUtil.isAlternativeResponse(message);

		if (acceptedBusiness) {
			Log.info(String.format("[%s] %s: Accepted platform business response: %s",
					getWorkflowTag(), scenario.getName(), message));
		} else if (alternative) {
			Log.info(String.format("[%s] %s: Accepted alternative platform response: %s",
					getWorkflowTag(), scenario.getName(), message));
		} else {
			softAssert.assertTrue(message.contains(scenario.getExpectedSnippet()),
					scenario.getName() + ": Expected snippet was not found in message.");
		}

		if (message.contains("Request ID")) {
			if (scenario.isWaitBeforeRequestId()) {
				try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			}
			String requestId = page.extractRequestId(message);
			Log.info(String.format("[%s] %s Request ID: %s", getWorkflowTag(), scenario.getName(), requestId));
			downloadViaTrackTask(page, workflowManager, scenario, requestId);

		} else if (alternative) {
			String requestId = PlatformRetryUtil.extractAlternativeRequestId(message);
			Log.info(String.format("[%s] %s Alternative Request ID: %s",
					getWorkflowTag(), scenario.getName(), requestId));
			try {
				Log.info(String.format("[%s] Waiting 60 seconds before navigating to Track Task...", getWorkflowTag()));
				Thread.sleep(60000);
			} catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			downloadViaTrackTask(page, workflowManager, scenario, requestId);

		} else if (acceptedBusiness) {
			Log.info(String.format("[%s] %s: No Request ID in accepted response; continuing.",
					getWorkflowTag(), scenario.getName()));
		} else {
			Log.warn(String.format("[%s] %s: No Request ID found, skipping Track Task + Download.",
					getWorkflowTag(), scenario.getName()));
		}
	}

	private void downloadViaTrackTask(GlobalPatentAllScenariosPage page,
			OutputFileWorkflowManager workflowManager, ScenarioConfig scenario, String requestId) {
		page.openTrackTask("Track Task");
		page.searchTrackTaskByRequestId(requestId);
		OutputFileWorkflowManager.DownloadSnapshot snap = null;
		try {
			if (scenario.isCaptureDownload() && workflowManager != null) {
				snap = workflowManager.snapshotDownloads();
			}
		} catch (java.io.IOException e) {
			Log.error("[GlobalPatentAllScenarios] Failed to snapshot downloads: " + e.getMessage());
		}
		page.clickDownloadButton("Download");
		try {
			if (scenario.isCaptureDownload() && workflowManager != null && snap != null) {
				workflowManager.captureDownloadedFile(scenario.getScenarioKey(), requestId, snap,
						scenario.getDownloadFileContains(), scenario.getDownloadWaitSeconds());
			}
		} catch (java.io.IOException | InterruptedException e) {
			if (e instanceof InterruptedException) { Thread.currentThread().interrupt(); }
			Log.error("[GlobalPatentAllScenarios] Failed to capture downloaded file: " + e.getMessage());
		}
	}
}