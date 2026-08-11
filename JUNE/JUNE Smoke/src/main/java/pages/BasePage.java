package pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

public abstract class BasePage {
	// this class is created to resuse Waits
	// Author : Yash Shrivastava

	protected WebDriver driver;
	protected WebDriverWait wait;

	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
	}

	protected WebElement waitVisible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	protected WebElement waitClickable(By locator) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	protected void safeClick(By locator) {
		WebElement element = waitClickable(locator);
		scrollIntoView(element);
		try {
			element.click();
		} catch (WebDriverException e) {
			// Fallback: click via Javascript if click is intercepted by an overlay/backdrop
			try {
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));", element);
			} catch (Exception jsEx) {
				throw e; // Throw original exception if fallback also fails
			}
		}
	}

	protected void safeClick(WebElement element) {
		scrollIntoView(element);
		try {
			element.click();
		} catch (WebDriverException e) {
			try {
				((JavascriptExecutor) driver).executeScript(
						"arguments[0].dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));", element);
			} catch (Exception jsEx) {
				throw e;
			}
		}
	}

	protected void safeType(By locator, String text) {
		WebElement element = waitVisible(locator);
		scrollIntoView(element);
		element.clear();
		element.sendKeys(text);
	}

	protected void scrollIntoView(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
	}

	// ── Helpers ──

	/**
	 * Waits for element to exist in DOM (no visibility needed),
	 * then scrolls it into view within its scrollable container.
	 */
	public void scrollToElement(By locator) {
		WebElement element = wait.until(
				ExpectedConditions.presenceOfElementLocated(locator));
		scrollIntoView(element);
	}

	/**
	 * Finds the release element matching the given label from an already-collected
	 * list, normalizing non-breaking spaces so exact text comparison is reliable.
	 */
	public WebElement findReleaseByLabel(List<WebElement> releases, String label) {
		for (WebElement release : releases) {
			String text = release.getText().replace('\u00A0', ' ').trim();
			if (text.equals(label)) {
				return release;
			}
		}
		throw new NoSuchElementException("Release item not found: " + label);
	}

	public void scrollAndVerify(WebElement element, String label) {
		scrollIntoView(element);
		Assert.assertTrue(wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed(),
				label + " is not visible");
		System.out.println(label + " verified");
	}

}
