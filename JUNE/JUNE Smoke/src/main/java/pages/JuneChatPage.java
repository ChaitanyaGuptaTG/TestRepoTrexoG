package pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.Log;

/**
 * Shared base class for all JUNE Chat workflow Page Objects.
 * Contains all common UI interactions (input, messages, track task, download, logout).
 * Subclasses only need to override {@link #selectDropdownOptionByLabel(String, String)}
 * and provide a constructor.
 *
 * @author Yash Shrivastava
 */
public abstract class JuneChatPage extends BasePage {

	// ──────────────────────────────────────────────────────────
	//  Shared Locators (single source of truth)
	// ──────────────────────────────────────────────────────────
	protected static final By DASHBOARD_TILE = By.xpath("//img[@alt='JUNE']");
	protected static final By BIBLIO_DROPDOWN = By.xpath("//div//span[text()='Bibliographic Data Extraction']");
	protected static final By QUERY_INPUT = By.xpath("//*[@placeholder='Enter your query or select a task to get started']");
	protected static final By SUBMIT_BUTTON = By.xpath("//button[@type='submit'] | //*[name()='svg' and @type='submit']/ancestor::button | //*[name()='svg' and @type='submit']");
	protected static final By TRACK_TASK_LINK = By.xpath("//div//span[text()='Track Task']");
	protected static final By SEARCH_FIELD = By.xpath("//input[@name='searchText']");
	protected static final By DOWNLOAD_ICON = By.xpath("(//*[name()='svg' and contains(@data-testid,'FileDownloadOutlinedIcon')])[1]");
	protected static final By BACK_TO_CHAT = By.xpath("//p[contains(text(),'Back to IP Assistant Chat')]");
	protected static final By YES_BUTTON = By.xpath("//button[(normalize-space()='Yes' or normalize-space()='YES') and not(@disabled)]");
	protected static final By PROFILE_ICON = By.xpath("//*[name()='svg' and @data-testid='PersonIcon']");
	protected static final By LOGOUT_BUTTON = By.xpath("//div//span[text()='Logout']");

	private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("Request ID\\s*(\\d+)");

	protected JuneChatPage(WebDriver driver) {
		super(driver);
	}

	// ──────────────────────────────────────────────────────────
	//  Dashboard & Navigation
	// ──────────────────────────────────────────────────────────

	public void clickDashboardTile(String tileText) {
		safeClick(DASHBOARD_TILE);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Opens the Bibliographic Data Extraction dropdown and selects a workflow option.
	 * Subclasses override this to select their specific workflow.
	 */
	public abstract void selectDropdownOptionByLabel(String fieldLabel, String optionText);

	/**
	 * Helper: opens the Biblio dropdown and clicks the specified option.
	 * Subclasses call this from their {@link #selectDropdownOptionByLabel} override.
	 */
	protected void openDropdownAndSelect(String optionText) {
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 30);
		
		// Target option in the Material UI select popover menu
		By optionLocator = By.xpath("//li[@role='option' and contains(normalize-space(), '" + optionText + "')]");

		// Locate combobox header
		WebElement combobox = waitClickable(By.xpath("//div[@role='combobox' and .//span[text()='Bibliographic Data Extraction']]"));
		
		// Check if the dropdown option is already visible/present. If not, or if aria-expanded is false, expand it.
		String isExpanded = combobox.getAttribute("aria-expanded");
		if (isExpanded == null || !isExpanded.equals("true")) {
			try {
				combobox.click();
			} catch (Exception e) {
				try {
					WebElement span = waitClickable(By.xpath("//div//span[text()='Bibliographic Data Extraction']"));
					span.click();
				} catch (Exception ex) {
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", combobox);
				}
			}
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// Click the option in the dropdown list
		WebElement option = waitClickable(optionLocator);
		try {
			option.click();
		} catch (Exception e) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
		}
	}

	// ──────────────────────────────────────────────────────────
	//  Input Box & Submit
	// ──────────────────────────────────────────────────────────

	public void enterInputValueByLabel(String fieldLabel, String value) {
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 60);

		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(QUERY_INPUT));
		wait.until(d -> {
			WebElement el = d.findElement(QUERY_INPUT);
			return el.isDisplayed() && el.isEnabled();
		});

		// Normalize value: replace all commas with newlines
		String normalizedValue = value.replace(",", "\n");

		try {
			input.click();
			input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
			input.sendKeys(Keys.BACK_SPACE);

			String[] lines = normalizedValue.split("\n", -1);
			for (int i = 0; i < lines.length; i++) {
				input.sendKeys(lines[i]);
				if (i < lines.length - 1) {
					input.sendKeys(Keys.chord(Keys.SHIFT, Keys.ENTER));
				}
			}
		} catch (Exception e) {
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].value = arguments[1]; " +
					"arguments[0].dispatchEvent(new Event('input', {bubbles: true})); " +
					"arguments[0].dispatchEvent(new Event('change', {bubbles: true})); " +
					"arguments[0].dispatchEvent(new Event('blur', {bubbles: true}));",
					input, normalizedValue);
		}

		// Wait until the submit button is enabled
		try {
			new WebDriverWait(driver, Duration.ofSeconds(20))
				.until(d -> {
					WebElement btn = d.findElement(SUBMIT_BUTTON);
					String disabledAttr = btn.getAttribute("disabled");
					return disabledAttr == null;
				});
		} catch (Exception e) {
			Log.warn("[JuneChatPage] Timeout waiting for submit button to be enabled: " + e.getMessage());
		}

		safeClick(SUBMIT_BUTTON);

		// Fallback: If the textarea is still not empty after 2 seconds, retry submit via JS
		try {
			Thread.sleep(2000);
			WebElement textEl = driver.findElement(QUERY_INPUT);
			String textVal = textEl.getAttribute("value");
			if (textVal != null && !textVal.trim().isEmpty()) {
				Log.info("[JuneChatPage] Textarea is not empty, retrying submit click via JS...");
				WebElement btn = driver.findElement(SUBMIT_BUTTON);
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));", btn);
			}
		} catch (Exception ignored) {}
	}

	// ──────────────────────────────────────────────────────────
	//  Message Waiting & Parsing
	// ──────────────────────────────────────────────────────────

	public String waitForMessageText(By messageLocator, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			WebElement message = d.findElement(messageLocator);
			String text = message.getText().trim();
			return text.isEmpty() ? null : text;
		});
	}

	public String waitForMessageContaining(By messageLocator, String expectedSnippet, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			WebElement message = d.findElement(messageLocator);
			String text = message.getText().trim();
			if (!text.isEmpty() && text.contains(expectedSnippet)) {
				return text;
			}
			return null;
		});
	}

	public String waitForLatestMessageContaining(By messageLocator, String expectedSnippet, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			List<WebElement> messages = d.findElements(messageLocator);
			if (messages.isEmpty()) {
				return null;
			}

			String lastMatchingText = null;
			for (WebElement message : messages) {
				String text = message.getText().trim();
				if (!text.isEmpty() && text.contains(expectedSnippet)) {
					lastMatchingText = text;
				}
			}
			return lastMatchingText;
		});
	}

	public int getMessageCount(By messageLocator) {
		return driver.findElements(messageLocator).size();
	}

	public String getLastMessageText(By messageLocator) {
		List<WebElement> messages = driver.findElements(messageLocator);
		if (messages.isEmpty()) {
			return null;
		}
		return messages.get(messages.size() - 1).getText().trim();
	}

	public String waitForLatestMessageContaining(By messageLocator, String expectedSnippet, int timeoutSeconds,
			int previousCount) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			List<WebElement> messages = d.findElements(messageLocator);
			if (messages.size() <= previousCount) {
				return null;
			}

			WebElement last = messages.get(messages.size() - 1);
			String text = last.getText().trim();
			if (!text.isEmpty() && text.contains(expectedSnippet)) {
				return text;
			}
			return null;
		});
	}

	public String waitForLatestMessageContaining(By messageLocator, String expectedSnippet, int timeoutSeconds,
			int previousCount, String previousLastText) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
			List<WebElement> messages = d.findElements(messageLocator);
			if (messages.isEmpty()) {
				By fallbackLocator = By.xpath("//*[contains(normalize-space(),'" + expectedSnippet + "')]");
				List<WebElement> fallback = d.findElements(fallbackLocator);
				if (fallback.isEmpty()) {
					return null;
				}
				WebElement lastFallback = fallback.get(fallback.size() - 1);
				String fallbackText = lastFallback.getText().trim();
				if (previousLastText != null && fallbackText.equals(previousLastText)) {
					return null;
				}
				return (!fallbackText.isEmpty() && fallbackText.contains(expectedSnippet)) ? fallbackText : null;
			}

			if (messages.size() > previousCount) {
				WebElement last = messages.get(messages.size() - 1);
				String text = last.getText().trim();
				if (!text.isEmpty() && text.contains(expectedSnippet)) {
					return text;
				}
				return null;
			}

			WebElement last = messages.get(messages.size() - 1);
			String text = last.getText().trim();
			if (previousLastText != null && text.equals(previousLastText)) {
				return null;
			}

			return (!text.isEmpty() && text.contains(expectedSnippet)) ? text : null;
		});
	}

	// ──────────────────────────────────────────────────────────
	//  Request ID Extraction
	// ──────────────────────────────────────────────────────────

	public String extractRequestId(String fullMessageText) {
		Matcher matcher = REQUEST_ID_PATTERN.matcher(fullMessageText);
		if (matcher.find()) {
			return matcher.group(1);
		}
		throw new IllegalStateException("Request ID not found in message: " + fullMessageText);
	}

	// ──────────────────────────────────────────────────────────
	//  Track Task & Download
	// ──────────────────────────────────────────────────────────

	public void openTrackTask(String trackTaskText) {
		safeClick(TRACK_TASK_LINK);
	}

	public void searchTrackTaskByRequestId(String requestId) {
		int attempts = 0;
		while (attempts < 4) {
			try {
				WebElement searchInput = waitVisible(SEARCH_FIELD);
				searchInput.click();
				searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				searchInput.sendKeys(Keys.BACK_SPACE);
				searchInput.sendKeys(requestId);
				searchInput.sendKeys(Keys.ENTER);

				By requestRow = By.xpath("//*[contains(normalize-space(),'" + requestId + "')]");
				new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.visibilityOfElementLocated(requestRow));
				return;
			} catch (Exception e) {
				attempts++;
				if (attempts >= 4) {
					throw e;
				}
				Log.info("[JuneChatPage] Request ID " + requestId + " not found on Track Task yet. Retrying in 10 seconds... (Attempt " + attempts + "/4)");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				// Re-click Track Task link to reload table
				try {
					safeClick(TRACK_TASK_LINK);
					utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 15);
				} catch (Exception ignored) {}
			}
		}
	}

	public void clickDownloadButton(String buttonText) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(DOWNLOAD_ICON));
		safeClick(DOWNLOAD_ICON);
	}

	public Path waitForDownloadedFile(Path downloadDir, String filenameContains, int timeoutSeconds)
			throws InterruptedException, IOException {
		Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
		while (Instant.now().isBefore(deadline)) {
			Path downloadedFile;
			try (Stream<Path> fileStream = Files.list(downloadDir)) {
				downloadedFile = fileStream.filter(Files::isRegularFile)
						.filter(path -> {
							String fn = path.getFileName().toString().toLowerCase();
							return !fn.endsWith(".crdownload") 
									&& !fn.endsWith(".tmp") 
									&& !fn.endsWith(".part")
									&& !fn.startsWith(".") 
									&& !fn.startsWith("~$")
									&& !fn.contains("chrome") 
									&& !fn.contains("google")
									&& !fn.contains("edge")
									&& !fn.contains("chromium");
						})
						.filter(path -> filenameContains == null || filenameContains.isBlank()
								|| path.getFileName().toString().contains(filenameContains))
						.max(Comparator.comparingLong(path -> path.toFile().lastModified()))
						.orElse(null);
			}

			if (downloadedFile != null) {
				return downloadedFile;
			}
			Thread.sleep(1000);
		}
		return null;
	}

	// ──────────────────────────────────────────────────────────
	//  Back to Chat & Confirm
	// ──────────────────────────────────────────────────────────

	public void backToIPAssistantChatAndConfirm() {
		if (!driver.findElements(BACK_TO_CHAT).isEmpty()) {
			try {
				wait.until(ExpectedConditions.elementToBeClickable(BACK_TO_CHAT));
				safeClick(BACK_TO_CHAT);
			} catch (Exception ignored) {
				// If click fails, continue to yes/fallback handling below.
			}
		}

		try {
			wait.until(ExpectedConditions.elementToBeClickable(YES_BUTTON));
		} catch (Exception ignored) {
		}

		boolean clickedYes = false;
		if (!driver.findElements(YES_BUTTON).isEmpty()) {
			try {
				safeClick(YES_BUTTON);
				clickedYes = true;
			} catch (Exception ignored) {
				clickedYes = false;
			}
		}

		if (clickedYes) {
			wait.until(ExpectedConditions.visibilityOfElementLocated(QUERY_INPUT));
		} else {
			ensureInputReadyWithFallback();
		}
	}

	/**
	 * Returns the workflow name used for fallback recovery (e.g., "US Trademark", "EP Patent").
	 * Subclasses override to provide their workflow-specific name.
	 */
	protected abstract String getWorkflowName();

	private void ensureInputReadyWithFallback() {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(QUERY_INPUT));
			return;
		} catch (Exception ignored) {
			// fall through to recovery
		}

		// Recover by submitting the workflow name to force a response
		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(QUERY_INPUT));
		input.click();
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		input.sendKeys(Keys.BACK_SPACE);
		input.sendKeys(getWorkflowName());

		safeClick(SUBMIT_BUTTON);

		By anyMessage = By.xpath("//p[contains(text(),'Request ID') or contains(text(),'We were unable to fetch')]");
		waitForMessageText(anyMessage, 30);
	}

	// ──────────────────────────────────────────────────────────
	//  Logout
	// ──────────────────────────────────────────────────────────

	public void logoutFromProfileMenu(String logoutText) {
		safeClick(PROFILE_ICON);
		wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON));
		safeClick(LOGOUT_BUTTON);
	}

	// ──────────────────────────────────────────────────────────
	//  Utility: Clear query input
	// ──────────────────────────────────────────────────────────

	public void clearQueryInput() {
		WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(QUERY_INPUT));
		input.click();
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		input.sendKeys(Keys.BACK_SPACE);
	}
}
