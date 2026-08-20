package pages.IDSPage;

import org.openqa.selenium.WebDriver;
import pages.BasePage;
import pages.help.*;

import java.nio.file.Path;

public class idsPage extends BasePage {

    private final pages.IDSPage.idsMenuPage menu;
    private final HelpMenuPage helpMenu;
    private final FeedbackPage feedback;
    private final ReleaseNotesPage releaseNotes;
    private final FAQsPage faqs;
    private final TutorialPage tutorial;

    public idsPage(WebDriver driver) {
        super(driver);
        this.menu = new pages.IDSPage.idsMenuPage(driver);
        this.helpMenu = new HelpMenuPage(driver);
        this.feedback = new FeedbackPage(driver);
        this.releaseNotes = new ReleaseNotesPage(driver);
        this.faqs = new FAQsPage(driver);
        this.tutorial = new TutorialPage(driver);
    }

    // ─── Help delegation ───

    public void clickJuneIcon() {
        helpMenu.clickJuneIcon();
    }

    // ─── IDE menu delegation ───

    public void clickIDE() {
        menu.clickIdsDropdown();
    }

    public void verifyIdsDropdownDisabledAfterSelection() {
        menu.verifyIdsDropdownDisabledAfterSelection();
    }

    public void selectDocumentDownloader() {
        menu.clickIdsDropdown();
        menu.selectDocumentDownloader();
    }

    public void verifyDocumentDownloaderSelected() {
        menu.verifyDocumentDownloaderLabel();
        menu.verifyApplicationNumbersInstruction();
    }

    public void selectDocumentDownloaderByTypingIntent(String intentText) {
        menu.enterIntentAsQuery(intentText);
        menu.clickSubmitButton();
    }

    public void verifyApplicationNumbersInstructionShown() {
        menu.verifyApplicationNumbersInstruction();
    }

    public void enterQueryAndSubmit(String query) {
        menu.enterQuery(query);
        menu.clickSubmitButton();
    }

    public Path clickDownloadAndVerifyTxt() {
        return menu.clickDownloadAndAssertTxt();
    }

    // ─── New: deep zip content validation ───

    /**
     * Validates the downloaded zip's internal structure against
     * the application numbers that were submitted.
     */
    public void assertZipContents(Path zipPath, String... appNumbers) {
        menu.assertZipContents(zipPath, appNumbers);
    }

    // ─── New: continue workflow ───

    /** Click YES on "Would you like to continue with 1449 and 892 Downloader?" */
    public void clickContinueYes() {
        menu.clickContinueYes();
    }

    /** Click NO on "Would you like to continue with 1449 and 892 Downloader?" */
    public void clickContinueNo() {
        menu.clickContinueNo();
    }
}