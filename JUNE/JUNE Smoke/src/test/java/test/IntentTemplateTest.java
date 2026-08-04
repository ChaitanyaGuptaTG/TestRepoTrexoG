package test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import base.BaseTest;
import pages.IntentTemplatePage;
import pages.LoginPage;
import utils.Log;
import utils.WaitUtils;

public class IntentTemplateTest extends BaseTest {

	private static final Path REQUEST_ID_FILE = Path.of("target", "intent-request-ids.txt");

	@Test(priority = 200)
	public void runIntentsAndStoreRequestIds() throws Exception {
		resetRequestIdFile();
		loginIfNeeded();

		IntentTemplatePage page = new IntentTemplatePage(getDriver());
		page.clickDashboardTile();

		Map<String, List<String>> intentPrompts = buildIntentPromptTemplate();
		for (Map.Entry<String, List<String>> entry : intentPrompts.entrySet()) {
			String intentName = entry.getKey();
			List<String> prompts = entry.getValue();

			String requestId = page.runIntentConversationAndCaptureRequestId(intentName, prompts, 120, 120);
			appendRequestId(intentName, requestId);
			page.backToChatFromTrackTaskIfVisible();
		}
	}

	@Test(priority = 201, dependsOnMethods = "runIntentsAndStoreRequestIds")
	public void verifyStatusOfStoredRequestIds() throws Exception {
		IntentTemplatePage page = new IntentTemplatePage(getDriver());
		List<RequestRecord> records = readRequestIdRecords();

		Assert.assertFalse(records.isEmpty(),
				"No request IDs were found. Ensure runIntentsAndStoreRequestIds completed successfully.");

		SoftAssert softAssert = new SoftAssert();
		for (RequestRecord record : records) {
			String status = page.getRequestStatusByRequestId(record.requestId, 45);
			Log.info("[IntentTemplateTest] Intent: " + record.intent + " | Request ID: " + record.requestId
					+ " | Status: " + status);

			boolean expectedTerminalStatus = "SUCCESS".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)
					|| "FAIL".equalsIgnoreCase(status);
			softAssert.assertTrue(expectedTerminalStatus, "Unexpected status for Request ID " + record.requestId
					+ ". Expected SUCCESS/FAILED but got: " + status);
		}
		softAssert.assertAll();
	}

	private Map<String, List<String>> buildIntentPromptTemplate() {
		Map<String, List<String>> intentPrompts = new LinkedHashMap<>();

		intentPrompts.put("US Patent",
				Arrays.asList("17159723,17164950"));
		intentPrompts.put("EP Patent",
				Arrays.asList("EP3278230,EP3433749"));
		intentPrompts.put("Global Patent",
				Arrays.asList("EP3712170A1,WO2013184912A2,WO2015024060A1"));

		return intentPrompts;
	}

	private void loginIfNeeded() throws Exception {
		LoginPage login = new LoginPage(getDriver());
		login.enterUsername(config.getProperty("username"));
		login.enterPassword(config.getProperty("password"));
		login.clickSignin();
		WaitUtils.waitForPageToLoadCompletely(getDriver(), 10);
	}

	private void resetRequestIdFile() throws IOException {
		Files.createDirectories(REQUEST_ID_FILE.getParent());
		Files.writeString(REQUEST_ID_FILE, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);
	}

	private void appendRequestId(String intentName, String requestId) throws IOException {
		String line = intentName + "|" + requestId + System.lineSeparator();
		Files.writeString(REQUEST_ID_FILE, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
				StandardOpenOption.APPEND);
	}

	private List<RequestRecord> readRequestIdRecords() throws IOException {
		List<RequestRecord> records = new ArrayList<>();
		if (!Files.exists(REQUEST_ID_FILE)) {
			return records;
		}

		List<String> lines = Files.readAllLines(REQUEST_ID_FILE, StandardCharsets.UTF_8);
		for (String line : lines) {
			if (line == null || line.isBlank() || !line.contains("|")) {
				continue;
			}

			String[] parts = line.split("\\|", 2);
			if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
				continue;
			}

			records.add(new RequestRecord(parts[0].trim(), parts[1].trim()));
		}
		return records;
	}

	private static class RequestRecord {
		private final String intent;
		private final String requestId;

		private RequestRecord(String intent, String requestId) {
			this.intent = intent;
			this.requestId = requestId;
		}
	}
}
