package pages.IDSPage.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.BasePage;
import utils.Log;

import java.time.Duration;
import java.util.List;

public class ContinueDialogHelper extends BasePage {

    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);

    public ContinueDialogHelper(WebDriver driver) {
        super(driver);
    }

    public void waitForConfirmation(By confirmationLocator) {
        waitVisible(confirmationLocator);
    }

    public void clickYes(By yesLocator, String workflowName) {
        safeClick(yesLocator);
        Log.info("Clicked YES to continue with '{}'", workflowName);
    }

    public void clickNo(By noLocator, String workflowName) {
        safeClick(noLocator);
        Log.info("Clicked NO - exiting the '{}' workflow", workflowName);
    }

    public void waitUntilNoButtonDisabled(By noLocator, String workflowName) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT).until(d -> {
                List<WebElement> no = d.findElements(noLocator);
                return !no.isEmpty() && !no.get(0).isEnabled();
            });
        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Continue dialog for " + workflowName + " is still live (NO still enabled) after clicking NO", e);
        }
        Log.pass("Continue dialog retired - workflow exited");
    }
}
