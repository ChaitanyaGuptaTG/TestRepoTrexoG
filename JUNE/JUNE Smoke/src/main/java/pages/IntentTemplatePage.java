package pages;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.Log;

public class IntentTemplatePage extends JuneChatPage {

	private static final Pattern REQUEST_ID_FROM_MESSAGE = Pattern.compile("Request ID\\s*([A-Za-z0-9-]+)",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern REQUEST_ID_FALLBACK = Pattern.compile("\\b\\d{5,}\\b");
	private static final Pattern STATUS_FALLBACK = Pattern.compile("\\b(SUCCESS|FAILED|FAIL|IN_PROGRESS|PENDING)\\b",
			Pattern.CASE_INSENSITIVE);

	private final By chatMessages = By.xpath("//p[not(descendant::img)]");
	private final By requestIdMessages = By.xpath("//p[contains(normalize-space(),'Request ID')]");
	private final By trackTaskRows = By.xpath("//div[contains(normalize-space(), 'Request ID:')]");
	private final By tableHeaders = By.xpath("//thead//th");

	public IntentTemplatePage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect(optionText);
	}

	@Override
	protected String getWorkflowName() {
		return "Template";
	}

	public void clickDashboardTile() {
		safeClick(DASHBOARD_TILE);
	}

	public void selectIntentFromDropdown(String intentName) {
		openDropdownAndSelect(intentName);
	}

	public void sendPrompt(String inputText) {
		enterInputValueByLabel("InputBox", inputText);
	}

	public int getMessageCount() {
		return driver.findElements(chatMessages).size();
	}

	public String waitForNextMessage(int previousMessageCount, String promptText, int timeoutSeconds) {
		final String cleanPrompt = promptText.trim();
		Log.info("[waitForNextMessage] Waiting for messages count > " + previousMessageCount + " (perPromptWaitSeconds = " + timeoutSeconds + "). Clean prompt: '" + cleanPrompt + "'");
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			List<WebElement> messages = d.findElements(chatMessages);
			if (messages.size() <= previousMessageCount) {
				return null;
			}

			// Look at the new messages that appeared after previousMessageCount
			for (int i = previousMessageCount; i < messages.size(); i++) {
				String text = messages.get(i).getText().trim();
				if (text.isEmpty()) {
					text = messages.get(i).getAttribute("textContent").trim();
				}
				if (!text.isEmpty() && !text.equalsIgnoreCase(cleanPrompt)) {
					return text;
				}
			}
			return null;
		});
	}

	public String runIntentConversationAndCaptureRequestId(String intentName, List<String> prompts,
			int perPromptWaitSeconds, int finalRequestWaitSeconds) {
		if (prompts == null || prompts.isEmpty()) {
			throw new IllegalArgumentException("Prompts list cannot be empty.");
		}

		selectIntentFromDropdown(intentName);

		// Wait for loader to disappear and input box to be ready
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 30);
		wait.until(d -> {
			WebElement el = d.findElement(QUERY_INPUT);
			return el.isDisplayed() && el.isEnabled();
		});

		String lastResponse = null;

		for (String prompt : prompts) {
			int beforeCount = getMessageCount();
			sendPrompt(prompt);
			lastResponse = waitForNextMessage(beforeCount, prompt, perPromptWaitSeconds);
		}

		String requestIdFromMessage = extractRequestIdFromText(lastResponse);
		if (requestIdFromMessage == null) {
			int beforeRequestMessageCount = driver.findElements(requestIdMessages).size();
			String requestIdMessage = waitForRequestIdMessage(beforeRequestMessageCount, finalRequestWaitSeconds);
			requestIdFromMessage = extractRequestIdFromText(requestIdMessage);
		}

		String resolvedRequestId = requestIdFromMessage;
		if (resolvedRequestId == null) {
			Log.info("[IntentTemplatePage] Request ID not found in chat messages. Falling back to Track Task...");
			openTrackTask();
			resolvedRequestId = getLatestRequestIdForIntent(intentName, finalRequestWaitSeconds);
		}

		if (resolvedRequestId == null) {
			throw new IllegalStateException("Unable to capture Request ID for intent: " + intentName);
		}

		Log.info("[IntentTemplatePage] Intent '" + intentName + "' mapped to Request ID: " + resolvedRequestId);
		return resolvedRequestId;
	}

	public void openTrackTask() {
		safeClick(TRACK_TASK_LINK);
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 30);
	}

	public String getLatestRequestIdForIntent(String intentName, int timeoutSeconds) {
		searchTrackTask(intentName);

		List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(trackTaskRows));

		String intentLower = intentName.toLowerCase(Locale.ENGLISH);
		for (WebElement row : rows) {
			String rowText = normalizeSpaces(row.getText()).toLowerCase(Locale.ENGLISH);
			if (!rowText.contains(intentLower)) {
				continue;
			}

			String requestId = extractRequestIdFromRow(row);
			if (requestId != null) {
				return requestId;
			}
		}

		return null;
	}

	public String getRequestStatusByRequestId(String requestId, int timeoutSeconds) {
		if (driver.findElements(SEARCH_FIELD).isEmpty()) {
			openTrackTask();
		}
		searchTrackTask(requestId);

		By matchingRow = By.xpath("//div[contains(normalize-space(), 'Request ID: " + requestId + "')]");
		WebElement row = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			try {
				WebElement el = d.findElement(matchingRow);
				if (el.isDisplayed()) {
					return el;
				}
			} catch (Exception ignored) {}
			return null;
		});

		String statusFromHeader = getCellByHeader(row, Arrays.asList("status"));
		if (statusFromHeader != null && !statusFromHeader.isBlank()) {
			return statusFromHeader.trim();
		}

		Matcher statusMatcher = STATUS_FALLBACK.matcher(normalizeSpaces(row.getText()));
		if (statusMatcher.find()) {
			return statusMatcher.group(1).toUpperCase(Locale.ENGLISH);
		}

		throw new IllegalStateException("Status was not found for Request ID: " + requestId);
	}

	public void backToChatFromTrackTaskIfVisible() {
		if (driver.findElements(BACK_TO_CHAT).isEmpty()) {
			return;
		}

		safeClick(BACK_TO_CHAT);

		if (!driver.findElements(YES_BUTTON).isEmpty()) {
			safeClick(YES_BUTTON);
		}

		waitVisible(QUERY_INPUT);
	}

	private String waitForRequestIdMessage(int previousRequestMessageCount, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			List<WebElement> messages = d.findElements(requestIdMessages);
			if (messages.size() <= previousRequestMessageCount) {
				return null;
			}

			String text = messages.get(messages.size() - 1).getText().trim();
			return text.isEmpty() ? null : text;
		});
	}

	private void searchTrackTask(String value) {
		WebElement searchInput = waitVisible(SEARCH_FIELD);
		searchInput.click();
		searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		searchInput.sendKeys(Keys.BACK_SPACE);
		searchInput.sendKeys(value);
		searchInput.sendKeys(Keys.ENTER);
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 30);
	}

	private String extractRequestIdFromRow(WebElement row) {
		String requestIdCellText = getCellByHeader(row, Arrays.asList("request id", "request"));
		String extractedFromCell = extractRequestIdFromText(requestIdCellText);
		if (extractedFromCell != null) {
			return extractedFromCell;
		}

		return extractRequestIdFromText(row.getText());
	}

	private String extractRequestIdFromText(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}

		String normalized = normalizeSpaces(text);
		Matcher messageMatcher = REQUEST_ID_FROM_MESSAGE.matcher(normalized);
		if (messageMatcher.find()) {
			return messageMatcher.group(1);
		}

		Matcher fallbackMatcher = REQUEST_ID_FALLBACK.matcher(normalized);
		if (fallbackMatcher.find()) {
			return fallbackMatcher.group();
		}

		String compact = normalized.replaceAll("[^A-Za-z0-9-]", "");
		return compact.isBlank() ? null : compact;
	}

	private String getCellByHeader(WebElement row, List<String> headerKeywords) {
		List<WebElement> headers = driver.findElements(tableHeaders);
		if (headers.isEmpty()) {
			return null;
		}

		int headerIndex = -1;
		for (int i = 0; i < headers.size(); i++) {
			String header = headers.get(i).getText().trim().toLowerCase(Locale.ENGLISH);
			for (String keyword : headerKeywords) {
				if (header.contains(keyword.toLowerCase(Locale.ENGLISH))) {
					headerIndex = i;
					break;
				}
			}
			if (headerIndex >= 0) {
				break;
			}
		}

		if (headerIndex < 0) {
			return null;
		}

		for (int retry = 0; retry < 2; retry++) {
			try {
				List<WebElement> cells = row.findElements(By.tagName("td"));
				if (headerIndex < cells.size()) {
					return cells.get(headerIndex).getText().trim();
				}
				return null;
			} catch (StaleElementReferenceException e) {
				// Retry once when table re-renders.
			}
		}

		return null;
	}

	private String normalizeSpaces(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}
}
