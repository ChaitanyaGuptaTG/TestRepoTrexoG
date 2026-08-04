package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.AUTrademarkAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;

/**
 * AU Trademark workflow smoke test — validates valid, invalid, and mixed input scenarios.
 *
 * @author Yash Shrivastava
 */
public class AUTrademarkAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "AUTrademarkAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "autrademark"; }

	@Test(priority = 6, description = "AU Trademark: valid, invalid, and mixed input scenarios")
	public void testAUTrademarkAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		AUTrademarkAllScenariosPage page = new AUTrademarkAllScenariosPage(getDriver());
		Log.info("[AUTrademarkAllScenarios] Navigating to AU Trademark flow.");

		String messageXpath = config.getProperty("autrademark.messageXpath",
				"//*[contains(normalize-space(), 'Request ID')]");
		String invalidMessageXpath = config.getProperty("autrademark.invalidMessageXpath",
				"//p[contains(text(),'We were unable to fetch')]");
		String successSnippet = config.getProperty("autrademark.expectedMessageSnippet",
				"Your task with Request ID");
		String mixedSuccessSnippet = config.getProperty("autrademark.mixedExpectedMessageSnippet",
				"Request ID");
		String invalidSnippet = config.getProperty("autrademark.invalidMessageSnippet",
				"We were unable to fetch the data for the input provided. Please provide valid application numbers and try again.");
		String downloadFileContains = config.getProperty("autrademark.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("autrademark.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "10")));
		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "autrademark");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "AU Trademark");

		boolean s1 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input(config.getProperty("autrademark.validInputs", "138174,153119,158999,159917,160236"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s2 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input(config.getProperty("autrademark.invalidInputs", "234567654323456765,24252,2465235325,324523452345,23452"))
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s3 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed)")
				.input(config.getProperty("autrademark.mixedInputs", "56653,56654,77780,81223,81881,234567654323456765,24252"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(mixedSuccessSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-3-mixed")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);

		if (s1 && s2 && s3) {
			try { workflowManager.verifyAll(); }
			catch (Exception | AssertionError e) {
				Log.error("[AUTrademarkAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[AUTrademarkAllScenarios] Skipping Phase 2 — one or more scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);
		softAssert.assertAll();
	}
}
