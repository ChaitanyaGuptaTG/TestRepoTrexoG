package utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class PlatformRetryUtil {

	public static final String PLATFORM_ERROR_MESSAGE = "Something went wrong. Try again later.";
	public static final String DOWNLOAD_RESPONSE_PREFIX = "You can download the file using";
	public static final String DATA_UNAVAILABLE_RESPONSE_PREFIX = "We were unable to fetch the data for";
	private static final By PLATFORM_ERROR_LOCATOR = By.xpath("//*[contains(normalize-space(),'"
			+ PLATFORM_ERROR_MESSAGE + "')]");
	private static final By ACCEPTED_RESPONSE_LOCATOR = By.xpath("//*[starts-with(normalize-space(),'"
			+ DOWNLOAD_RESPONSE_PREFIX + "') or starts-with(normalize-space(),'"
			+ DATA_UNAVAILABLE_RESPONSE_PREFIX + "')]");
	private static final By YES_BUTTON_LOCATOR = By.xpath("//button[normalize-space()='Yes' and not(@disabled)]");
	private static final By CHAT_INPUT_LOCATOR = By
			.xpath("//*[@placeholder='Enter your query or select a task to get started']");

	private PlatformRetryUtil() {
	}

	@FunctionalInterface
	public interface ScenarioAttempt {
		void run() throws Exception;
	}

	public static void executeWithRetry(WebDriver driver, Properties config, String workflowKey, String scenarioName,
			ScenarioAttempt attempt) throws Exception {
		int maxRetries = getMaxRetries(config, workflowKey);
		int retryCount = 0;

		while (true) {
			try {
				attempt.run();
				if (retryCount > 0) {
					PlatformRetryReportStore.record(workflowKey, scenarioName, retryCount, maxRetries, "RECOVERED",
							"Scenario completed successfully after retry.");
					Log.info("[PlatformRetry] Recovery successful for " + workflowKey + " | " + scenarioName
							+ " after " + retryCount + " retry attempt(s).");
				}
				return;
			} catch (PlatformTransientException e) {
				if (retryCount >= maxRetries) {
					PlatformRetryReportStore.record(workflowKey, scenarioName, retryCount, maxRetries, "FAILED",
							"Maximum retry limit exceeded. Last error: " + e.getMessage());
					Log.error("[PlatformRetry] Final failure for " + workflowKey + " | " + scenarioName
							+ " after " + retryCount + " retry attempt(s). Last error: " + e.getMessage());
					throw e;
				}

				retryCount++;
				PlatformRetryReportStore.record(workflowKey, scenarioName, retryCount, maxRetries, "RETRY_TRIGGERED",
						e.getMessage());
				Log.warn("[PlatformRetry] Transient platform error for " + workflowKey + " | " + scenarioName
						+ ". Retry attempt " + retryCount + " of " + maxRetries + ". Error: " + e.getMessage());
				recoverFromPlatformError(driver, workflowKey, scenarioName, retryCount, maxRetries);
			}
		}
	}

	public static ResponseSnapshot snapshotAcceptedResponses(WebDriver driver) {
		List<WebElement> acceptedMessages = getVisibleAcceptedResponses(driver);
		WebElement lastElement = acceptedMessages.isEmpty() ? null : acceptedMessages.get(acceptedMessages.size() - 1);
		String lastText = acceptedMessages.isEmpty() ? null
				: normalize(acceptedMessages.get(acceptedMessages.size() - 1).getText());
		return new ResponseSnapshot(acceptedMessages.size(), lastText, lastElement);
	}

	public static String waitForExpectedMessageOrPlatformError(WebDriver driver, By messageLocator,
			String expectedSnippet, int timeoutSeconds, int previousCount, String previousLastText) {
		return waitForExpectedMessageOrPlatformError(driver, messageLocator, expectedSnippet, timeoutSeconds,
				previousCount, previousLastText, snapshotAcceptedResponses(driver), null, null, null);
	}

	public static String waitForExpectedMessageOrPlatformError(WebDriver driver, By messageLocator,
			String expectedSnippet, int timeoutSeconds, int previousCount, String previousLastText,
			ResponseSnapshot previousAcceptedResponses, String workflowKey, String scenarioName) {
		return waitForExpectedMessageOrPlatformError(driver, messageLocator, expectedSnippet, timeoutSeconds,
				previousCount, previousLastText, previousAcceptedResponses, workflowKey, scenarioName, null);
	}

	public static String waitForExpectedMessageOrPlatformError(WebDriver driver, By messageLocator,
			String expectedSnippet, int timeoutSeconds, int previousCount, String previousLastText,
			ResponseSnapshot previousAcceptedResponses, String workflowKey, String scenarioName, String input) {
		
		Log.info("[PlatformRetry] Starting response validation for " + workflowKey + " | " + scenarioName + ". Wait timeout: " + timeoutSeconds + "s.");
		
		// Snapshot before state of error messages & paragraphs to prevent historical/page-wide false-positives
		int beforeErrorCount = 0;
		int beforeParagraphCount = 0;
		try {
			beforeErrorCount = driver.findElements(PLATFORM_ERROR_LOCATOR).size();
			beforeParagraphCount = driver.findElements(By.xpath("//p")).size();
		} catch (Exception ignored) {}

		// 1. Loader Disappearance
		waitForLoaderToDisappear(driver, 5);

		Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
		while (Instant.now().isBefore(deadline)) {
			try {
				// Check for new platform error (Strict Count-based check)
				List<WebElement> platformErrors = driver.findElements(PLATFORM_ERROR_LOCATOR);
				if (platformErrors.size() > beforeErrorCount) {
					WebElement latestError = platformErrors.get(platformErrors.size() - 1);
					String errorText = stabilizeAndGetText(driver, latestError, 5);
					if (errorText.contains(PLATFORM_ERROR_MESSAGE)) {
						Log.warn("[PlatformRetry] New transient platform error matched: " + errorText);
						throw new PlatformTransientException("Platform displayed transient error: " + PLATFORM_ERROR_MESSAGE);
					}
				}

				// Check for expected locator (Success or Scenario-Specific)
				List<WebElement> messages = driver.findElements(messageLocator);
				if (messages.size() > previousCount) {
					WebElement latestExpected = messages.get(messages.size() - 1);
					String expectedText = stabilizeAndGetText(driver, latestExpected, 5);
					if (!expectedText.isEmpty() && (expectedText.contains(expectedSnippet) || isAlternativeResponse(expectedText))) {
						Log.info("[PlatformRetry] Expected/Alternative scenario matched: " + expectedSnippet + ". Response: " + abbreviate(expectedText, 120));
						Log.info("[PlatformRetry] Final execution decision: Proceed with current scenario success.");
						return expectedText;
					}
				}

				// Check for any newly appeared paragraph for strict whitelisting and unknown response detection
				List<WebElement> paragraphs = driver.findElements(By.xpath("//p"));
				if (paragraphs.size() > beforeParagraphCount) {
					WebElement latestP = paragraphs.get(paragraphs.size() - 1);
					String currentText = latestP.getText().trim();
					
					if (!currentText.isEmpty()) {
						String stabilizedText = stabilizeAndGetText(driver, latestP, 5);
						boolean isError = stabilizedText.contains(PLATFORM_ERROR_MESSAGE);
						boolean isExpected = stabilizedText.contains(expectedSnippet);
						boolean isAlternative = isAlternativeResponse(stabilizedText);
						boolean isAccepted = isAcceptedBusinessResponse(stabilizedText);
						boolean isUserPrompt = stabilizedText.equals(input) || (input != null && input.contains(stabilizedText));
						
						if (isUserPrompt) {
							// Ignore user message bubble and advance our snapshot pointer
							beforeParagraphCount = paragraphs.size();
						} else if (isError) {
							Log.warn("[PlatformRetry] New transient platform error matched in paragraph: " + stabilizedText);
							throw new PlatformTransientException("Platform displayed transient error: " + PLATFORM_ERROR_MESSAGE);
						} else if (isExpected || isAlternative) {
							Log.info("[PlatformRetry] Expected/Alternative scenario matched via paragraph: " + (isExpected ? expectedSnippet : "Alternative Response") + ". Response: " + abbreviate(stabilizedText, 120));
							Log.info("[PlatformRetry] Final execution decision: Proceed with current scenario success.");
							return stabilizedText;
						} else if (isAccepted) {
							Log.info("[PlatformRetry] Accepted business response matched: " + abbreviate(stabilizedText, 120));
							Log.info("[PlatformRetry] Final execution decision: Proceed with accepted business response flow.");
							handleAcceptedBusinessResponse(driver, workflowKey, scenarioName, stabilizedText);
							return stabilizedText;
						} else {
							// Capture screenshot and log clearly as unmapped/unknown response
							Log.error("[PlatformRetry] Unknown/unmapped response detected: " + stabilizedText);
							try {
								String screenshotPath = ScreenshotUtil.captureScreenshot(driver, "UnknownResponse_" + scenarioName);
								Log.error("[PlatformRetry] Screenshot for unknown response captured at: " + screenshotPath);
							} catch (Exception ex) {
								Log.error("[PlatformRetry] Failed to capture screenshot: " + ex.getMessage());
							}
							Log.info("[PlatformRetry] Final execution decision: Fail due to unknown/unmapped response.");
							throw new org.openqa.selenium.TimeoutException("Unknown/unmapped response received from platform: " + stabilizedText);
						}
					}
				}

				// Fallback check for expectedSnippet
				By fallbackLocator = By.xpath("//*[contains(normalize-space(),'" + expectedSnippet + "')]");
				List<WebElement> fallback = driver.findElements(fallbackLocator);
				if (!fallback.isEmpty()) {
					WebElement latestFallback = fallback.get(fallback.size() - 1);
					String fallbackText = latestFallback.getText().trim();
					if ((previousLastText == null || !fallbackText.equals(previousLastText))
							&& !fallbackText.isEmpty() && fallbackText.contains(expectedSnippet)) {
						String stabilizedFallback = stabilizeAndGetText(driver, latestFallback, 5);
						Log.info("[PlatformRetry] Fallback expected response stabilized: " + abbreviate(stabilizedFallback, 120));
						Log.info("[PlatformRetry] Final execution decision: Proceed with fallback scenario success.");
						return stabilizedFallback;
					}
				}

				// Fallback check for alternative response
				By altFallbackLocator = By.xpath("//*[contains(normalize-space(),'Your request with ID') and contains(normalize-space(),'is now submitted')]");
				List<WebElement> altFallback = driver.findElements(altFallbackLocator);
				if (!altFallback.isEmpty()) {
					WebElement latestAltFallback = altFallback.get(altFallback.size() - 1);
					String altFallbackText = latestAltFallback.getText().trim();
					if ((previousLastText == null || !altFallbackText.equals(previousLastText))
							&& !altFallbackText.isEmpty() && isAlternativeResponse(altFallbackText)) {
						String stabilizedAltFallback = stabilizeAndGetText(driver, latestAltFallback, 5);
						Log.info("[PlatformRetry] Fallback alternative response stabilized: " + abbreviate(stabilizedAltFallback, 120));
						Log.info("[PlatformRetry] Final execution decision: Proceed with fallback scenario success.");
						return stabilizedAltFallback;
					}
				}
			} catch (org.openqa.selenium.WebDriverException e) {
				Log.info("[PlatformRetry] Stale element or driver exception matched during wait loop. Retrying next iteration. Error: " + e.getMessage());
			}

			sleep(300);
		}

		// Double check for platform error one last time
		List<WebElement> platformErrors = driver.findElements(PLATFORM_ERROR_LOCATOR);
		if (platformErrors.size() > beforeErrorCount) {
			WebElement latestError = platformErrors.get(platformErrors.size() - 1);
			String errorText = stabilizeAndGetText(driver, latestError, 5);
			if (errorText.contains(PLATFORM_ERROR_MESSAGE)) {
				throw new PlatformTransientException("Platform displayed transient error after wait timeout: " + PLATFORM_ERROR_MESSAGE);
			}
		}

		Log.error("[PlatformRetry] Expected scenario message was not received within " + timeoutSeconds + " seconds. Expected: " + expectedSnippet);
		try {
			String screenshotPath = ScreenshotUtil.captureScreenshot(driver, "PlatformRetry_Timeout_" + scenarioName);
			Log.error("[PlatformRetry] Screenshot captured at: " + screenshotPath);
		} catch (Exception ex) {
			Log.error("[PlatformRetry] Failed to capture screenshot: " + ex.getMessage());
		}
		Log.info("[PlatformRetry] Final execution decision: Fail due to timeout.");
		throw new org.openqa.selenium.TimeoutException("Expected scenario message was not received within "
				+ timeoutSeconds + " seconds. Expected snippet: " + expectedSnippet);
	}

	public static void waitForLoaderToDisappear(WebDriver driver, int timeoutSeconds) {
		By loaderLocator = By.xpath("//*[contains(@class, 'spinner') or contains(@class, 'loader') or contains(@class, 'loading') or contains(@class, 'CircularProgress') or @data-testid='loader']");
		try {
			WebDriverWait loaderWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
			try {
				new WebDriverWait(driver, Duration.ofMillis(500)).until(ExpectedConditions.visibilityOfElementLocated(loaderLocator));
				Log.info("[PlatformRetry] Spinner/loader appeared. Waiting for it to disappear...");
			} catch (Exception ignored) {}
			loaderWait.until(ExpectedConditions.invisibilityOfElementLocated(loaderLocator));
			Log.info("[PlatformRetry] Spinner/loader disappeared.");
		} catch (Exception ignored) {}
	}

	public static String stabilizeAndGetText(WebDriver driver, WebElement element, int maxStabilizeWaitSeconds) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		try {
			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (Exception e) {
			Log.warn("[PlatformRetry] Element not visible for stabilization: " + e.getMessage());
		}

		String lastText = "";
		Instant deadline = Instant.now().plusSeconds(maxStabilizeWaitSeconds);
		int matchCount = 0;
		while (Instant.now().isBefore(deadline)) {
			String currentText = element.getText().trim();
			if (!currentText.isEmpty() && currentText.equals(lastText)) {
				matchCount++;
				if (matchCount >= 2) {
					if (isFullyRenderedResponse(currentText)) {
						Log.info("[PlatformRetry] Response stabilized (terminal signature matched): " + abbreviate(currentText, 120));
						return currentText;
					}
					if (matchCount >= 4) {
						Log.info("[PlatformRetry] Response stabilized (no change for 1.2s): " + abbreviate(currentText, 120));
						return currentText;
					}
				}
			} else {
				matchCount = 0;
				lastText = currentText;
			}
			sleep(300);
		}
		String finalWaitingText = element.getText().trim();
		Log.warn("[PlatformRetry] Stabilization timeout reached. Final captured text: " + abbreviate(finalWaitingText, 120));
		return finalWaitingText;
	}

	private static boolean isFullyRenderedResponse(String text) {
		if (text == null) return false;
		String norm = normalize(text);
		if (norm.contains(PLATFORM_ERROR_MESSAGE)) {
			return true;
		}
		if (norm.contains("Request ID") && norm.contains("Download") && norm.contains("reference.")) {
			return true;
		}
		if (norm.contains("We were unable to fetch the data") && norm.contains("Please provide valid application numbers and try again.")) {
			return true;
		}
		if (isAlternativeResponse(norm)) {
			return true;
		}
		return false;
	}

	public static boolean isAlternativeResponse(String text) {
		if (text == null) return false;
		String norm = text.replaceAll("\\s+", " ").trim().toLowerCase();
		return norm.contains("your request with id") && norm.contains("is now submitted") && norm.contains("current status") && norm.contains("notification in your inbox");
	}

	public static String extractAlternativeRequestId(String text) {
		if (text == null) return null;
		String norm = text.replaceAll("\\s+", " ").trim();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)Your request with ID\\s*([A-Za-z0-9-]+)\\s*is now submitted");
		java.util.regex.Matcher matcher = pattern.matcher(norm);
		if (matcher.find()) {
			return matcher.group(1);
		}
		throw new IllegalStateException("Alternative Request ID not found in message: " + text);
	}

	public static boolean isPlatformErrorVisible(WebDriver driver) {
		try {
			return driver.findElements(PLATFORM_ERROR_LOCATOR).stream().anyMatch(WebElement::isDisplayed);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isAcceptedBusinessResponse(String responseText) {
		return isDownloadResponse(responseText) || isDataUnavailableResponse(responseText);
	}

	public static boolean isDownloadResponse(String responseText) {
		return normalize(responseText).startsWith(DOWNLOAD_RESPONSE_PREFIX);
	}

	public static boolean isDataUnavailableResponse(String responseText) {
		return normalize(responseText).startsWith(DATA_UNAVAILABLE_RESPONSE_PREFIX);
	}

	public static void recoverFromPlatformError(WebDriver driver, String workflowKey, String scenarioName) {
		recoverFromPlatformError(driver, workflowKey, scenarioName, 0, getMaxRetries(null, workflowKey));
	}

	private static void recoverFromPlatformError(WebDriver driver, String workflowKey, String scenarioName,
			int retryCount, int maxRetries) {
		try {
			WebDriverWait retryWait = new WebDriverWait(driver, Duration.ofSeconds(15));
			WebElement yesButton = retryWait.until(ExpectedConditions.elementToBeClickable(YES_BUTTON_LOCATOR));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", yesButton);
			yesButton.click();
			retryWait.until(ExpectedConditions.visibilityOfElementLocated(CHAT_INPUT_LOCATOR));
			PlatformRetryReportStore.record(workflowKey, scenarioName, retryCount, maxRetries,
					"RECOVERY_CLICKED", "Clicked Yes and returned to chat input.");
			Log.info("[PlatformRetry] Clicked Yes and recovered chat input for " + workflowKey + " | " + scenarioName);
		} catch (Exception e) {
			PlatformRetryReportStore.record(workflowKey, scenarioName, retryCount, maxRetries,
					"RECOVERY_FAILED", "Failed to click Yes or restore chat input: " + e.getMessage());
			throw new PlatformTransientException("Failed to recover from transient platform error: " + e.getMessage());
		}
	}

	private static void handleAcceptedBusinessResponse(WebDriver driver, String workflowKey, String scenarioName,
			String responseText) {
		String type = isDownloadResponse(responseText) ? "SUCCESS_DOWNLOAD_RESPONSE" : "DATA_UNAVAILABLE_RESPONSE";
		String safeWorkflowKey = workflowKey == null ? "unknown-workflow" : workflowKey;
		String safeScenarioName = scenarioName == null ? "unknown-scenario" : scenarioName;

		PlatformRetryReportStore.record(safeWorkflowKey, safeScenarioName, 0, getMaxRetries(null, safeWorkflowKey),
				"ACCEPTED_RESPONSE", type + ": " + abbreviate(responseText, 220));
		Log.info("[PlatformRetry] Accepted business response for " + safeWorkflowKey + " | " + safeScenarioName
				+ " | " + type + ". No retry will be triggered.");

		clickYesAndRecoverInputIfPrompted(driver, safeWorkflowKey, safeScenarioName);
	}

	private static void clickYesAndRecoverInputIfPrompted(WebDriver driver, String workflowKey, String scenarioName) {
		try {
			if (driver.findElements(YES_BUTTON_LOCATOR).isEmpty()) {
				Log.info("[PlatformRetry] No Yes confirmation was visible for " + workflowKey + " | " + scenarioName
						+ "; continuing current flow.");
				return;
			}

			WebDriverWait promptWait = new WebDriverWait(driver, Duration.ofSeconds(5));
			WebElement yesButton = promptWait.until(ExpectedConditions.elementToBeClickable(YES_BUTTON_LOCATOR));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", yesButton);
			yesButton.click();
			PlatformRetryReportStore.record(workflowKey, scenarioName, 0, getMaxRetries(null, workflowKey),
					"ACCEPTED_RECOVERY_CLICKED", "Clicked Yes after accepted platform response.");
			Log.info("[PlatformRetry] Clicked Yes after accepted response for " + workflowKey + " | " + scenarioName);

			try {
				new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.visibilityOfElementLocated(CHAT_INPUT_LOCATOR));
				Log.info("[PlatformRetry] Chat input recovered after accepted response for " + workflowKey + " | "
						+ scenarioName);
			} catch (Exception e) {
				Log.warn("[PlatformRetry] Yes was clicked after accepted response, but chat input was not visible yet for "
						+ workflowKey + " | " + scenarioName + ". Continuing current scenario flow. Detail: "
						+ e.getMessage());
			}
		} catch (Exception e) {
			Log.warn("[PlatformRetry] Accepted response was received, but optional Yes recovery was not completed for "
					+ workflowKey + " | " + scenarioName + ". Continuing without retry. Detail: " + e.getMessage());
		}
	}

	private static String getFreshAcceptedResponse(WebDriver driver, ResponseSnapshot previousAcceptedResponses) {
		List<WebElement> acceptedMessages = getVisibleAcceptedResponses(driver);
		if (acceptedMessages.isEmpty()) {
			return null;
		}

		String lastText = normalize(acceptedMessages.get(acceptedMessages.size() - 1).getText());
		if (previousAcceptedResponses == null) {
			return lastText;
		}
		if (acceptedMessages.size() > previousAcceptedResponses.getCount()) {
			return lastText;
		}
		if (!lastText.isEmpty() && !lastText.equals(previousAcceptedResponses.getLastText())) {
			return lastText;
		}
		if (previousAcceptedResponses.getLastElement() != null
				&& !acceptedMessages.get(acceptedMessages.size() - 1)
						.equals(previousAcceptedResponses.getLastElement())) {
			return lastText;
		}
		return null;
	}

	private static List<WebElement> getVisibleAcceptedResponses(WebDriver driver) {
		try {
			return driver.findElements(ACCEPTED_RESPONSE_LOCATOR).stream()
					.filter(element -> {
						try {
							return element.isDisplayed() && isAcceptedBusinessResponse(element.getText());
						} catch (Exception e) {
							return false;
						}
					}).collect(Collectors.toList());
		} catch (Exception e) {
			return List.of();
		}
	}

	private static String normalize(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}

	private static String abbreviate(String text, int maxLength) {
		String normalized = normalize(text);
		if (normalized.length() <= maxLength) {
			return normalized;
		}
		return normalized.substring(0, maxLength - 3) + "...";
	}

	private static int getMaxRetries(Properties config, String workflowKey) {
		if (config == null) {
			return 3;
		}
		String workflowValue = config.getProperty(workflowKey + ".platformErrorMaxRetries");
		String globalValue = config.getProperty("workflow.platformErrorMaxRetries", "3");
		String selectedValue = workflowValue == null || workflowValue.isBlank() ? globalValue : workflowValue;
		return Integer.parseInt(selectedValue.trim());
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static class PlatformTransientException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public PlatformTransientException(String message) {
			super(message);
		}
	}

	public static final class ResponseSnapshot {
		private final int count;
		private final String lastText;
		private final WebElement lastElement;

		private ResponseSnapshot(int count, String lastText, WebElement lastElement) {
			this.count = count;
			this.lastText = lastText;
			this.lastElement = lastElement;
		}

		public int getCount() {
			return count;
		}

		public String getLastText() {
			return lastText;
		}

		private WebElement getLastElement() {
			return lastElement;
		}
	}
}
