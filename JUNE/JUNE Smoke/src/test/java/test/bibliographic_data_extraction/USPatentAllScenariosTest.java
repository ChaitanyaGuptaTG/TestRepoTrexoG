package test.bibliographic_data_extraction;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import test.AbstractWorkflowTest;
import test.ScenarioConfig;
import pages.bibliographic_data_extraction.USPatentAllScenariosPage;
import utils.Log;
import utils.OutputFileWorkflowManager;

/**
 * US Patent workflow smoke test — validates valid, invalid, and mixed input scenarios.
 *
 * @author Yash Shrivastava
 */
public class USPatentAllScenariosTest extends AbstractWorkflowTest {

	@Override
	protected String getWorkflowTag() { return "USPatentAllScenarios"; }

	@Override
	protected String getWorkflowConfigPrefix() { return "uspatent"; }

	@Test(priority = 2, description = "US Patent: valid, invalid, and mixed input scenarios")
	public void testUSPatentAllScenarios() throws Exception {
		SoftAssert softAssert = new SoftAssert();
		performLogin();

		USPatentAllScenariosPage page = new USPatentAllScenariosPage(getDriver());
		Log.info("[USPatentAllScenarios] Navigating to US Patent flow.");

		// Config
		String messageXpath = config.getProperty("uspatent.messageXpath",
				"//p[contains(text() , 'Request ID')]");
		String invalidMessageXpath = config.getProperty("uspatent.invalidMessageXpath",
				"//p[contains(text(),'We were unable to fetch')]");
		String successSnippet = config.getProperty("uspatent.expectedMessageSnippet",
				"You can download the file using the following link: Download. An email notification has also been sent for your reference.");
		String invalidSnippet = config.getProperty("uspatent.invalidMessageSnippet",
				"We were unable to fetch the data for the input provided. Please provide valid application numbers and try again.");
		String downloadFileContains = config.getProperty("uspatent.downloadFileContains",
				config.getProperty("workflow.downloadFileContains", ""));
		int downloadWaitSeconds = Integer.parseInt(config.getProperty("uspatent.downloadWaitSeconds",
				config.getProperty("workflow.downloadWaitSeconds", "10")));
		OutputFileWorkflowManager workflowManager = new OutputFileWorkflowManager(config, "uspatent");

		page.clickDashboardTile("JUNE");
		page.selectDropdownOptionByLabel("Bibliographic Data Extraction", "US Patent");

		// Scenarios
		boolean s1 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 1 (valid)")
				.input("16520775,16994325,17031417,17096456,17140864,17152971,17159723,17164950,12341234,12345678")
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.scenarioKey("scenario-1-valid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s2 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 2 (invalid)")
				.input("1234,123412341234,312412432134,87654321")
				.messageLocator(By.xpath(invalidMessageXpath))
				.expectedSnippet(invalidSnippet)
				.scenarioKey("scenario-2-invalid")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.build(), workflowManager);
		page.backToIPAssistantChatAndConfirm();

		boolean s3 = runScenario(page, softAssert, ScenarioConfig.builder("Scenario 3 (mixed)")
				.input("17159723,17164950,12341234,12345678,1234,123412341234,312412432134,87654321")
				.messageLocator(By.xpath(messageXpath))
				.expectedSnippet(successSnippet)
				.waitBeforeRequestId(true)
				.scenarioKey("scenario-3-mixed")
				.downloadFileContains(downloadFileContains)
				.downloadWaitSeconds(downloadWaitSeconds)
				.captureDownload(true)
				.build(), workflowManager);

		// Phase 2: Output verification
		if (s1 && s2 && s3) {
			try { workflowManager.verifyAll(); }
			catch (Exception | AssertionError e) {
				Log.error("[USPatentAllScenarios] Output verification failed: " + e.getMessage());
				softAssert.fail("Output verification failed: " + e.getMessage());
			}
		} else {
			Log.warn("[USPatentAllScenarios] Skipping Phase 2 — one or more scenarios failed in Phase 1.");
		}

		performLogout(page, softAssert);
		softAssert.assertAll();
	}
}
