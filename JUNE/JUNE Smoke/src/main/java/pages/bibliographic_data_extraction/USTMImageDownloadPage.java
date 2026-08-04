package pages.bibliographic_data_extraction;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.Log;

/**
 * Page Object for the US TM Image Download workflow.
 * Extends {@link USTrademarkAllScenariosPage} (which extends {@link JuneChatPage}).
 * Adds image format selection (PNG/JPG) on top of the standard trademark workflow.
 *
 * @author Yash Shrivastava
 */
public class USTMImageDownloadPage extends USTrademarkAllScenariosPage {

	public USTMImageDownloadPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("US TM Image Download");
	}

	@Override
	protected String getWorkflowName() {
		return "US TM Image Download";
	}

	// ──────────────────────────────────────────────────────────
	//  Image Format Selection
	// ──────────────────────────────────────────────────────────

	public void selectImageFormat(String format) {
		String normalizedFormat = format.toUpperCase();
		String preferredXpath = "//button[@type='button' and normalize-space()='." + normalizedFormat + "']";
		String fallbackXpath = "//button[normalize-space()='." + normalizedFormat + "']";

		List<WebElement> elements = driver.findElements(By.xpath(preferredXpath));
		if (elements.isEmpty()) {
			elements = driver.findElements(By.xpath(fallbackXpath));
		}

		if (elements.isEmpty()) {
			throw new org.openqa.selenium.NoSuchElementException(
					"Could not find button for format: ." + normalizedFormat);
		}

		// Find the latest visible one (closest to chat input box, at the bottom)
		WebElement latestButton = null;
		for (int i = elements.size() - 1; i >= 0; i--) {
			WebElement btn = elements.get(i);
			if (btn.isDisplayed()) {
				latestButton = btn;
				break;
			}
		}

		if (latestButton == null) {
			latestButton = elements.get(elements.size() - 1);
		}

		Log.info("[USTMImageDownload] Clicking format button: ." + normalizedFormat);
		safeClick(latestButton); // Uses BasePage.safeClick(WebElement) — no more duplicate
	}

	private int initialFormatButtonsCount = 0;
	private int initialFailureCount = 0;

	private int getFormatButtonsCount() {
		By pngPreferred = By.xpath("//button[@type='button' and normalize-space()='.PNG']");
		By pngFallback = By.xpath("//button[normalize-space()='.PNG']");
		By jpgPreferred = By.xpath("//button[@type='button' and normalize-space()='.JPG']");
		By jpgFallback = By.xpath("//button[normalize-space()='.JPG']");

		return driver.findElements(pngPreferred).size()
				+ driver.findElements(pngFallback).size()
				+ driver.findElements(jpgPreferred).size()
				+ driver.findElements(jpgFallback).size();
	}

	@Override
	public void enterInputValueByLabel(String fieldLabel, String value) {
		initialFormatButtonsCount = getFormatButtonsCount();
		By failureLocator = By.xpath("//*[contains(normalize-space(),'All application numbers failed to fetch data.')]");
		initialFailureCount = driver.findElements(failureLocator).size();
		super.enterInputValueByLabel(fieldLabel, value);
	}

	/**
	 * Waits for the format prompt (PNG/JPG buttons) to appear after submitting
	 * input, then selects the specified format. Handles the case where a failure
	 * message appears instead of format buttons.
	 */
	public void waitForFormatPromptAndSelect(String format) {
		By pngPreferred = By.xpath("//button[@type='button' and normalize-space()='.PNG']");
		By pngFallback = By.xpath("//button[normalize-space()='.PNG']");
		By jpgPreferred = By.xpath("//button[@type='button' and normalize-space()='.JPG']");
		By jpgFallback = By.xpath("//button[normalize-space()='.JPG']");
		By failureLocator = By.xpath("//*[contains(normalize-space(),'All application numbers failed to fetch data.')]");

		Log.info("[USTMImageDownload] Waiting for format prompt buttons to appear...");
		try {
			new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
				int currentFormatCount = d.findElements(pngPreferred).size()
						+ d.findElements(pngFallback).size()
						+ d.findElements(jpgPreferred).size()
						+ d.findElements(jpgFallback).size();
				int currentFailureCount = d.findElements(failureLocator).size();

				boolean newButtonsPresent = currentFormatCount > initialFormatButtonsCount;
				boolean newFailurePresent = currentFailureCount > initialFailureCount;
				return newButtonsPresent || newFailurePresent;
			});

			int currentFormatCount = driver.findElements(pngPreferred).size()
					+ driver.findElements(pngFallback).size()
					+ driver.findElements(jpgPreferred).size()
					+ driver.findElements(jpgFallback).size();
			boolean newButtonsPresent = currentFormatCount > initialFormatButtonsCount;

			if (newButtonsPresent) {
				selectImageFormat(format);
			} else {
				Log.warn("[USTMImageDownload] Format buttons did not appear, but a final message/failure was detected. Skipping format selection.");
			}
		} catch (Exception e) {
			Log.error("[USTMImageDownload] Error or timeout waiting for format prompt buttons: " + e.getMessage());
			// Fallback: try to select format anyway
			selectImageFormat(format);
		}
	}
}
