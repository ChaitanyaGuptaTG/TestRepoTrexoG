package pages.IDSPage.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import utils.Log;

import java.time.Duration;


public class IdsDropdownHelper extends BasePage {

    private static final By IDS_DROPDOWN = By.xpath("//div[@role='combobox' and .//span[text()='IDS']]");

    public IdsDropdownHelper(WebDriver driver) {
        super(driver);
    }

    public void clickIdsDropdown() {
        WebElement combobox = waitClickable(IDS_DROPDOWN);
        String expanded = combobox.getAttribute("aria-expanded");
        if (expanded == null || !expanded.equals("true")) {
            safeClick(combobox);
        }
        Log.info("Opened the IDS dropdown");
    }


    public void verifyDropdownAvailableAfterSelection(By optionLocator, String workflowName, Duration probeTimeout) {
        safeClick(IDS_DROPDOWN);
        boolean optionReappeared = isVisibleWithin(optionLocator, probeTimeout);

        closeDropdown();

        Assert.assertTrue(optionReappeared, "IDS dropdown should offer '" + workflowName + "' again once the workflow has been continued/exited");
        Log.pass("IDS dropdown is available again after the {} workflow", workflowName);
    }

    public void closeDropdown() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    private boolean isVisibleWithin(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
