package pages.help;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import pages.BasePage;

public class FeedbackPage extends BasePage {

    private final By feedbackFormTitle = By.xpath("//h5[contains(text(),'Feedback Form')]");
    private final By rateUsText = By.xpath("//h6[contains(text(),'Rate us')]");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");

    public FeedbackPage(WebDriver driver) {
        super(driver);
    }

    public void verifyAndClose() {
        Assert.assertTrue(waitVisible(feedbackFormTitle).isDisplayed(), "Feedback Form title is not visible");
        Assert.assertTrue(waitVisible(rateUsText).isDisplayed(), "Rate Us text is not visible");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cancelButton)).click();
        System.out.println("Clicked the Cancel button");
    }
}
