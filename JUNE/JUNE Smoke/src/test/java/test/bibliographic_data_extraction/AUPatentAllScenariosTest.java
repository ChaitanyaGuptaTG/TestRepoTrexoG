package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.AUPatentAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;

/**
 * AU Patent workflow smoke test — validates valid, invalid, and mixed input scenarios.
 *
 * @author Yash Shrivastava
 */
public class AUPatentAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "AUPatentAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "aupatent"; }

	@Test(priority = 5, description = "AU Patent: valid, invalid, and mixed input scenarios")
	public void testAUPatentAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		AUPatentAllScenariosPage page = new AUPatentAllScenariosPage(getDriver());
		Log.info("[AUPatentAllScenarios] Navigating to AU Patent flow.");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "AU Patent");

		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "aupatent");

		String messageXpath = config.getProperty("aupatent.messageXpath",
				"//p[contains(text() , 'Request ID')]");
		String successSnippet = config.getProperty("aupatent.expectedMessageSnippet",
				"You can download the file using the following link: Download. An email notification has also been sent for your reference.");
		String invalidMessageXpath = config.getProperty("aupatent.invalidMessageXpath",
				"//p[contains(text(),'We were unable to fetch')]");
		String invalidSnippet = config.getProperty("aupatent.invalidMessageSnippet",
				"We were unable to fetch the data for the input provided. Please provide valid application numbers and try again.");
		String downloadFileContains = config.getProperty("aupatent.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("aupatent.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "10")));

		boolean s1 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input(config.getProperty("aupatent.validInputs", "2005300257,2005299534,2009233829,2011258553,2005296086"))
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s2 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input(config.getProperty("aupatent.invalidInputs", "8767676545,2123345434,9999999999,1234"))
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s3 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed)")
				.input(config.getProperty("aupatent.mixedInputs", "2005300257,2005296086,2005299534,9999999999,1234"))
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
				Log.error("[AUPatentAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[AUPatentAllScenarios] Skipping Phase 2 — one or more scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);
		softAssert.assertAll();
	}
}
