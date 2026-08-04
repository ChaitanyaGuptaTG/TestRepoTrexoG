package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.EPPatentAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;

/**
 * EP Patent workflow smoke test — validates valid, invalid, and mixed input scenarios.
 *
 * @author Yash Shrivastava
 */
public class EPPatentAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "EPPatentAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "eppatent"; }

	@Test(priority = 7, description = "EP Patent: valid, invalid, and mixed input scenarios")
	public void testEPPatentAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		EPPatentAllScenariosPage page = new EPPatentAllScenariosPage(getDriver());
		Log.info("[EPPatentAllScenarios] Navigating to EP Patent flow.");

		String messageXpath = config.getProperty("eppatent.messageXpath",
				"//*[contains(normalize-space(),'Your task with Request ID') and contains(normalize-space(),'Download')]");
		String invalidMessageXpath = config.getProperty("eppatent.invalidMessageXpath",
				"//*[contains(normalize-space(),'We were unable to fetch') and contains(normalize-space(),'publication numbers')]");
		String successSnippet = config.getProperty("eppatent.expectedMessageSnippet",
				"You can download the file using the following link: Download. An email notification has also been sent for your reference.");
		String invalidSnippet = config.getProperty("eppatent.invalidMessageSnippet",
				"We were unable to fetch the data for the input provided. Please provide valid publication numbers and try again.");
		String downloadFileContains = config.getProperty("eppatent.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("eppatent.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "30")));
		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "eppatent");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "EP Patent");

		boolean s1 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input(config.getProperty("eppatent.validInputs", "EP3278230,EP3278235,EP3079091,EP3433749,EP3679500"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s2 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input(config.getProperty("eppatent.invalidInputs", "ok9876,9876549876,567,pl9876542q2"))
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s3 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed)")
				.input(config.getProperty("eppatent.mixedInputs", "EP3278235,EP3079091,EP3433749,EP3679500,ok9876,EP9876549876"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-3-mixed")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);

		if (s1 && s2 && s3) {
			try { workflowManager.verifyAll(); }
			catch (Exception | AssertionError e) {
				Log.error("[EPPatentAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[EPPatentAllScenarios] Skipping Phase 2 — one or more scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);
		softAssert.assertAll();
	}
}
