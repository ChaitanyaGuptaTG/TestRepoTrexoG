package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utils.ScreenshotUtil;

public class helpPage extends BasePage {

    // In this class we are identifying WebElements of HelpPage
    // Author : Yash Shrivastava
/*
* XPaths of the elements
*/
//    private final By helpIcon = By.xpath("//button[@aria-label='Help']");
    private final By juneIcon = By.cssSelector("img[alt='JUNE']");
    private final By helpIcon = By.xpath("//button[normalize-space()='Help']");
    private final By feedbackOption = By.xpath("/html/body/div[2]/div[3]/ul/li[1]");
    private final By releaseNotesOption = By.xpath("/html/body/div[2]/div[3]/ul/li[2]");
    private final By faqsOption = By.xpath("/html/body/div[2]/div[3]/ul/li[3]");
    private final By tutorialOption = By.xpath("/html/body/div[2]/div[3]/ul/li[4]");


    public helpPage(WebDriver driver) {
        super(driver);
    }

    public void clickHelpIcon() {
        safeClick(helpIcon);
        System.out.println("CLicked the helpIcon");
    }
    public void clickJuneIcon(){
        safeClick(juneIcon);
        System.out.println("Clicked the juneIcon");
    }
    public void checkElementsOfHelpIcon() {
//        wait.until(ExpectedConditions.visibilityOfElementLocated(helpIcon)).click();
        Assert.assertTrue(waitVisible(feedbackOption).isDisplayed(), "Feedback option is not displayed");
        System.out.println("feedback is displayed");
        Assert.assertTrue(waitVisible(releaseNotesOption).isDisplayed(), "Release Notes option is not displayed");
        System.out.println("releaseNotesOption is displayed");
        Assert.assertTrue(waitVisible(faqsOption).isDisplayed(), "FAQ's option is not displayed");
        System.out.println("faqsOption is displayed");
        Assert.assertTrue(waitVisible(tutorialOption).isDisplayed(), "Tutorial option is not displayed");
        System.out.println("tutorialOption is displayed");
        ScreenshotUtil.captureScreenshot(driver,"checkAsserts");
    }
}