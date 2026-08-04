package pages.bibliographic_data_extraction;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.openqa.selenium.JavascriptExecutor;
import pages.JuneChatPage;
import utils.Log;

/**
 * Page Object for the Global Patent workflow.
 * Extends {@link JuneChatPage} for shared behavior and adds Global Patent-specific
 * functionality: multi-line input handling, initial response verification,
 * alert handling, and submit-button retry logic.
 *
 * @author Yash Shrivastava
 */
public class GlobalPatentAllScenariosPage extends JuneChatPage {

	private boolean alertExpected = false;

	public void setAlertExpected(boolean alertExpected) {
		this.alertExpected = alertExpected;
	}

	public GlobalPatentAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("Global Patent");
		// Global Patent workspace takes time to transition and load the new chat flow
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		utils.PlatformRetryUtil.waitForLoaderToDisappear(driver, 10);
	}

	@Override
	protected String getWorkflowName() {
		return "Global Patent";
	}

	// ──────────────────────────────────────────────────────────
	//  Global Patent-specific: Initial Response Verification
	// ──────────────────────────────────────────────────────────

	public boolean verifyInitialResponse(String expectedMessage, int timeoutSeconds) {
		// Use a partial match or check for the key parts of the message to be robust
		String searchMsg = expectedMessage;
		if (expectedMessage.contains("prefixed with the country code")) {
			searchMsg = "prefixed with the country code";
		}
		// Target any element containing the normalized search message
		By locator = By.xpath("//*[contains(normalize-space(.), '" + searchMsg + "')]");
		try {
			WebElement element = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return element.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	// ──────────────────────────────────────────────────────────
	//  Global Patent-specific: Multi-line Input with Submit Retry
	// ──────────────────────────────────────────────────────────

	/**
	 * Overrides the standard input method to support multi-line values
	 * (newline-separated country+number inputs) and includes a submit-button
	 * retry mechanism for cases where the React textarea doesn't clear after submit.
	 */
	@Override
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
			Thread.sleep(500);
		} catch (Exception e) {
			Log.warn("[GlobalPatentAllScenariosPage] Timeout waiting for submit button to be enabled: " + e.getMessage());
		}

		safeClick(SUBMIT_BUTTON);

		if (this.alertExpected) {
			Log.info("[GlobalPatentAllScenariosPage] Alert is expected. Skipping fallback check and returning immediately.");
			// Reset the flag for subsequent runs
			this.alertExpected = false;
			return;
		}

		// Global Patent: Check if a native browser alert is present immediately after submission click.
		// If present, we return immediately to allow the test scenario to handle the alert.
		// This avoids executing other driver commands (like checking textarea value) which would 
		// trigger automated alert dismissal due to unhandledPromptBehavior capability.
		try {
			new WebDriverWait(driver, Duration.ofSeconds(2))
					.until(ExpectedConditions.alertIsPresent());
			Log.info("[GlobalPatentAllScenariosPage] Native browser alert detected immediately after submit. Skipping fallback check.");
			return;
		} catch (Exception ignored) {
			// No native alert within 2s, continue with fallback check
		}

		// Fallback: If the textarea is still not empty after 2 seconds, retry submit via JS
		try {
			Thread.sleep(2000);
			WebElement textEl = driver.findElement(QUERY_INPUT);
			String textVal = textEl.getAttribute("value");
			if (textVal != null && !textVal.trim().isEmpty()) {
				Log.info("[GlobalPatentAllScenariosPage] Textarea is not empty, retrying submit click via JS...");
				WebElement btn = driver.findElement(SUBMIT_BUTTON);
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));", btn);
			}
		} catch (Exception ignored) {}
	}

	// ──────────────────────────────────────────────────────────
	//  Global Patent-specific: Alert Handling
	// ──────────────────────────────────────────────────────────

	/**
	 * Handle browser alert dialog or UI alert and get its text.
	 * First tries a native browser alert, then falls back to UI pop-ups.
	 */
	public String handleAlertAndGetText(int timeoutSeconds) {
		// 1. Try native browser alert (using dynamic timeout up to 10s for slow environments)
		try {
			org.openqa.selenium.Alert alert = new WebDriverWait(driver, Duration.ofSeconds(Math.min(timeoutSeconds, 10)))
					.until(ExpectedConditions.alertIsPresent());
			String text = alert.getText();
			alert.accept();
			Log.info("[GlobalPatent] Handled native browser alert: " + text);
			return text;
		} catch (Exception e) {
			Log.info("[GlobalPatent] No native browser alert found, checking for UI alert/pop-up...");
		}

		// 2. Try UI alert/pop-up/dialog (using robust normalize-space on the element itself)
		By uiAlertLocator = By.xpath("//*[contains(normalize-space(.), 'Please enter a valid country code')]");
		try {
			WebElement alertEl = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
					.until(ExpectedConditions.visibilityOfElementLocated(uiAlertLocator));
			String text = alertEl.getText();
			Log.info("[GlobalPatent] Handled UI alert/pop-up: " + text);

			// Dismiss the UI popup/alert if possible
			By closeButtonLocator = By.xpath(
					"//button[contains(text(),'OK') or contains(text(),'Ok') or contains(text(),'Close') or contains(text(),'Dismiss') or contains(text(),'Agree')] " +
					"| //*[name()='svg' and (contains(@data-testid,'Close') or contains(@class,'close') or contains(@class,'Close'))]");
			if (!driver.findElements(closeButtonLocator).isEmpty()) {
				try {
					driver.findElement(closeButtonLocator).click();
					Log.info("[GlobalPatent] Clicked UI alert close/OK button.");
					Thread.sleep(1500); // Allow modal closing animation to settle
				} catch (Exception ignored) {}
			}
			return text;
		} catch (Exception e) {
			Log.error("[GlobalPatent] Failed to find native browser alert or UI alert/pop-up. Error: " + e.getMessage());
			throw e;
		}
	}

	// ──────────────────────────────────────────────────────────
	//  Override: Back to Chat with extra stabilization wait
	// ──────────────────────────────────────────────────────────

	@Override
	public void backToIPAssistantChatAndConfirm() {
		Log.info("[GlobalPatent] Navigating back to Home dashboard via link to guarantee a clean workspace...");
		try {
			By homeBreadcrumb = By.xpath("//*[text()='Home' or normalize-space(.)='Home' or contains(@href, 'dashboard') or @data-testid='HomeIcon' or @data-testid='HomeOutlinedIcon']");
			WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(homeBreadcrumb));
			try {
				element.click();
			} catch (Exception e) {
				// Fallback to Javascript click if regular click is intercepted or blocked by overlay
				((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
			}
			Thread.sleep(3000);
			clickDashboardTile("JUNE");
			selectDropdownOptionByLabel("Bibliographic Data Extraction", "Global Patent");
			Thread.sleep(3000);
		} catch (Exception e) {
			Log.warn("[GlobalPatent] Failed to navigate to Home via link: " + e.getMessage() + ". Falling back to super implementation.");
			super.backToIPAssistantChatAndConfirm();
		}
	}
}
