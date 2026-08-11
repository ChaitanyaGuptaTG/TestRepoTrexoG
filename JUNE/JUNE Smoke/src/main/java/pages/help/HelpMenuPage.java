package pages.help;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import pages.BasePage;
import utils.ScreenshotUtil;

public class HelpMenuPage extends BasePage {

    private final By juneIcon = By.cssSelector("img[alt='JUNE']");
    private final By helpIcon = By.xpath("//button[normalize-space()='Help']");
    private final By feedbackOption = By.xpath("//li[contains(text(),'Feedback')]");
    private final By releaseNotesOption = By.xpath("//li[contains(text(),'Release Notes')]");
    private final By faqsOption = By.xpath("//li[contains(text(),'FAQ')]");
    private final By tutorialOption = By.xpath("//li[contains(text(),'Tutorial')]");

    public HelpMenuPage(WebDriver driver) { super(driver); }

    public void clickJuneIcon() {
        safeClick(juneIcon);
        System.out.println("Clicked the June icon");
    }

    public void clickHelpIcon() {
        safeClick(helpIcon);
        System.out.println("Clicked the Help icon");
    }

    public void clickFeedbackOption() {
        waitVisible(feedbackOption).click();
    }

    public void clickReleaseNotesOption() {
        waitVisible(releaseNotesOption).click();
    }

    public void clickFaqsOption() {
        waitVisible(faqsOption).click();
    }

    public void checkElementsOfHelpIcon() {
        Assert.assertTrue(waitVisible(feedbackOption).isDisplayed(),
                "Feedback option is not displayed");
        System.out.println("Feedback option is displayed");

        Assert.assertTrue(waitVisible(releaseNotesOption).isDisplayed(),
                "Release Notes option is not displayed");
        System.out.println("Release Notes option is displayed");

        Assert.assertTrue(waitVisible(faqsOption).isDisplayed(),
                "FAQ's option is not displayed");
        System.out.println("FAQ's option is displayed");

        Assert.assertTrue(waitVisible(tutorialOption).isDisplayed(),
                "Tutorial option is not displayed");
        System.out.println("Tutorial option is displayed");

        ScreenshotUtil.captureScreenshot(driver, "helpMenuElements");
    }
}