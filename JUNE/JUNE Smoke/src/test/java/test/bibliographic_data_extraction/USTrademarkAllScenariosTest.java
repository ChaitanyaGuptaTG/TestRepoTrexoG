package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.USTrademarkAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;

/**
 * US Trademark workflow smoke test — validates valid, invalid, and mixed input scenarios.
 *
 * @author Yash Shrivastava
 */
public class USTrademarkAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "USTrademarkAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "ustrademark"; }

	@Test(priority = 3, description = "US Trademark: valid, invalid, and mixed input scenarios")
	public void testUSTrademarkAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		USTrademarkAllScenariosPage page = new USTrademarkAllScenariosPage(getDriver());
		Log.info("[USTrademarkAllScenarios] Navigating to US Trademark flow.");

		String messageXpath = config.getProperty("ustrademark.messageXpath",
				"//p[contains(text() , 'Request ID')]");
		String invalidMessageXpath = config.getProperty("ustrademark.invalidMessageXpath",
				"//*[contains(normalize-space(),'All application numbers failed to fetch data.')]");
		String successSnippet = config.getProperty("ustrademark.expectedMessageSnippet",
				"You can download the file using the following link: Download. An email notification has also been sent for your reference.");
		String invalidSnippet = config.getProperty("ustrademark.invalidMessageSnippet",
				"All application numbers failed to fetch data.");
		String downloadFileContains = config.getProperty("ustrademark.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("ustrademark.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "10")));
		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "ustrademark");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "US Trademark");

		boolean s1 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input(config.getProperty("ustrademark.validInputs", "98795545,98746094,98612628,98607138,98604783"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s2 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input(config.getProperty("ustrademark.invalidInputs", "9876543453432,213456,13454452346,242325,34"))
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s3 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed)")
				.input(config.getProperty("ustrademark.mixedInputs",
						"98795545,98746094,98612628,98607138,98604783,98765432,12345,1234,123456789876543"))
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
				Log.error("[USTrademarkAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[USTrademarkAllScenarios] Skipping Phase 2 — one or more scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);
		softAssert.assertAll();
	}
}
