package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TableUtil {

	private WebDriver driver;

	public TableUtil(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Check if a value is present in a dynamic table
	 * 
	 * @param value            The text to search
	 * @param timeoutInSeconds Max wait time for table to load
	 * @return true if value exists, false otherwise
	 */
	public boolean isValuePresentInTable(String value, int timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));

		// Retry mechanism to handle re-renders
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				// Wait for rows to appear
				wait.until(ExpectedConditions
						.presenceOfAllElementsLocatedBy(By.xpath("//tbody[contains(@class,'MuiTableBody-root')]//tr")));

				List<WebElement> rows = driver
						.findElements(By.xpath("//tbody[contains(@class,'MuiTableBody-root')]//tr"));

				for (int i = 0; i < rows.size(); i++) {
					// Re-fetch row each iteration to avoid stale element
					WebElement row = driver.findElements(By.xpath("//tbody[contains(@class,'MuiTableBody-root')]//tr"))
							.get(i);

					List<WebElement> cells = row.findElements(By.tagName("td"));
					for (WebElement cell : cells) {
						if (cell.getText().trim().equals(value)) {
							return true;
						}
					}
				}
				return false;

			} catch (StaleElementReferenceException e) {
				// Retry if DOM changed
			}
		}
		return false;
	}
}
