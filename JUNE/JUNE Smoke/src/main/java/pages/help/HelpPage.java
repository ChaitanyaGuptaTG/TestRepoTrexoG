package pages.help;

import org.openqa.selenium.WebDriver;
import pages.BasePage;

public class HelpPage extends BasePage {

    private final HelpMenuPage menu;
    private final FeedbackPage feedback;
    private final ReleaseNotesPage releaseNotes;
    private final FAQsPage faqs;
    private final TutorialPage tutorial;

    public HelpPage(WebDriver driver) {
        super(driver);
        this.menu = new HelpMenuPage(driver);
        this.feedback = new FeedbackPage(driver);
        this.releaseNotes = new ReleaseNotesPage(driver);
        this.faqs = new FAQsPage(driver);
        this.tutorial = new TutorialPage(driver);
    }

    public void clickJuneIcon() {
        menu.clickJuneIcon();
    }

    public void clickHelpIcon() {
        menu.clickHelpIcon();
    }

    public void checkHelpMenuOptions() {
        menu.checkElementsOfHelpIcon();
    }

    public void clickAndVerifyFeedback() {
        menu.clickFeedbackOption();
        feedback.verifyAndClose();
    }

    public void clickAndVerifyReleaseNotes() {
        menu.clickReleaseNotesOption();
        releaseNotes.verifyAllReleases();
    }

    public void clickAndSearchReleaseNotes() {
        menu.clickReleaseNotesOption();
        releaseNotes.verifySearch();
    }

    public void clickAndVerifyFAQs() {
        menu.clickFaqsOption();
        faqs.verifyAll();
    }

    public void clickAndVerifyTutorial() {
        menu.clickTutorialOption();
        tutorial.verifyAll();
    }

}
