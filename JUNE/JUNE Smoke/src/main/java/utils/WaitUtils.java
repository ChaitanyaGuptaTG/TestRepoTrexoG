package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.NoSuchElementException;

public class WaitUtils {
	// this utility is created for generic waits and should be used in testcases.
	// Author : Yash Shrivastava
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final SecureRandom random = new SecureRandom();

	public static void waitForPageToLoadCompletely(WebDriver driver, int timeoutInSeconds) {

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));

		// 1️⃣ Wait for document.readyState == complete
		ExpectedCondition<Boolean> pageLoadCondition = driver1 -> ((JavascriptExecutor) driver1)
				.executeScript("return document.readyState").equals("complete");

		wait.until(pageLoadCondition);

		// 2️⃣ Wait for AJAX (jQuery)
		try {
			wait.until(driver1 -> (Boolean) ((JavascriptExecutor) driver1)
					.executeScript("return window.jQuery != undefined && jQuery.active === 0"));
		} catch (TimeoutException | JavascriptException ignored) {
			// jQuery not present – ignore safely
		}

		// 3️⃣ Wait for Angular (if present)
		try {
			wait.until(driver1 -> (Boolean) ((JavascriptExecutor) driver1)
					.executeScript("return (window.angular === undefined) || "
							+ "(angular.element(document).injector() === undefined) || "
							+ "(angular.element(document).injector().get('$http').pendingRequests.length === 0)"));
		} catch (TimeoutException | JavascriptException ignored) {
			// Angular not present – ignore safely
		}
	}

	// This method is created to generate random Docket Number for createNewDocket
	// Test
	public static String generateRandomAlphaNumeric(int length) {

		{
			StringBuilder sb = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				int index = random.nextInt(CHARACTERS.length());
				sb.append(CHARACTERS.charAt(index));
			}
			return sb.toString();
		}
	}

	// This method is create to wait for notifictaion to disappear when click on
	// next in docket creation process.
	public void clickAfterOverlayGone(WebDriver driver, String xpathOfButton, String xpathOfOverlay) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

		// Wait for overlay to be either invisible or not present
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpathOfOverlay)));

		// Now wait for your button to be clickable
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathOfButton)));

		// Click the button
		button.click();
	}

	public static String waitForNonEmptyCell(WebDriver driver, By locator, int timeoutSeconds) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

		return wait.until(d -> {
			try {
				WebElement cell = d.findElement(locator);
				String text = cell.getText().trim();
				if (!text.isEmpty()) {
					return text;
				} else {
					return null; // retry
				}
			} catch (StaleElementReferenceException | NoSuchElementException e) {
				// Element went stale or not yet in DOM, retry
				return null;
			}
		});
	}

}