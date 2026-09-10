package pages.IDSPage.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import utils.Log;

import java.time.Duration;

public class QueryInputHelper extends BasePage {

    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final String QUERY_PLACEHOLDER = "Enter your query or select a task to get started";

    private static final By QUERY_INPUT = By.cssSelector("textarea[placeholder='" + QUERY_PLACEHOLDER + "']");
    private static final By SUBMIT_BUTTON = By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']");

    public QueryInputHelper(WebDriver driver) {
        super(driver);
    }

    public void enterQuery(String query) {
        Assert.assertNotNull(query, "Query to enter must not be null");

        WebElement input = focusAndClearQueryBox();

        // -1 keeps trailing empty strings, so intentional blank lines survive.
        String[] lines = query.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            input.sendKeys(lines[i]);
            if (i < lines.length - 1) {
                input.sendKeys(Keys.chord(Keys.SHIFT, Keys.ENTER));
            }
        }

        Log.info("Entered query ({} line(s)): {}", lines.length, query.replace("\n", " | "));
    }

    public void enterIntentAsQuery(String intentText) {
        focusAndClearQueryBox().sendKeys(intentText);
        Log.info("Entered intent as query: {}", intentText);
    }

    public void clickSubmitButton() {
        safeClick(SUBMIT_BUTTON);
        Log.info("Submitted the query");
    }

    private WebElement focusAndClearQueryBox() {
        WebElement input = new WebDriverWait(driver, UI_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(QUERY_INPUT));
        input.click();
        input.sendKeys(Keys.chord(selectAllModifier(), "a"), Keys.BACK_SPACE);
        return input;
    }

    private Keys selectAllModifier() {
        Platform platform = (driver instanceof HasCapabilities)
                ? ((HasCapabilities) driver).getCapabilities().getPlatformName()
                : Platform.getCurrent();
        return (platform != null && platform.is(Platform.MAC)) ? Keys.COMMAND : Keys.CONTROL;
    }
}
