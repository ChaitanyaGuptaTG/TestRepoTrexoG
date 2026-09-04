package pages.IDSPage;

import org.openqa.selenium.WebDriver;
import pages.help.HelpMenuPage;

import java.nio.file.Path;
import java.util.List;

public class idsPage {

    private final idsMenuPage menu;
    private final referenceCountMenuPage referenceCount;
    private final referenceDownloaderMenuPage referenceDownloader;
    private final HelpMenuPage helpMenu;

    public idsPage(WebDriver driver) {
        this.menu = new idsMenuPage(driver);
        this.referenceCount = new referenceCountMenuPage(driver);
        this.referenceDownloader = new referenceDownloaderMenuPage(driver);
        this.helpMenu = new HelpMenuPage(driver);
    }

    // ── Common ────────────────────────────────────────────────────────────────

    public void clickJuneIcon() {
        helpMenu.clickJuneIcon();
    }

    @Deprecated
    public void clickIDE() {
        clickIdsDropdown();
    }

    public void clickIdsDropdown() {
        menu.clickIdsDropdown();
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        menu.verifyIdsDropdownAvailableAfterSelection();
    }

    // ── 1449 and 892 Downloader ───────────────────────────────────────────────

    public void selectDocumentDownloader() {
        menu.clickIdsDropdown();
        menu.selectDocumentDownloader();
    }

    public void verifyDocumentDownloaderSelected() {
        menu.verifyDocumentDownloaderLabel();
        menu.verifyApplicationNumbersInstruction();
    }

    public void verifyApplicationNumbersInstructionShown() {
        menu.verifyApplicationNumbersInstruction();
    }

    public void selectDocumentDownloaderByTypingIntent(String intentText) {
        menu.enterIntentAsQuery(intentText);
        menu.clickSubmitButton();
    }

    public void enterQueryAndSubmit(String query) {
        menu.enterQuery(query);
        menu.clickSubmitButton();
    }

    @Deprecated
    public Path clickDownloadAndVerifyTxt() {
        return clickDownloadAndVerifyZip();
    }

    public Path clickDownloadAndVerifyZip() {
        return menu.clickDownloadAndAssertTxt();
    }

    public void assertZipContents(Path zipPath, String... appNumbers) {
        menu.assertZipContents(zipPath, appNumbers);
    }

    public void clickContinueYes() {
        menu.clickContinueYes();
    }

    public void clickContinueNo() {
        menu.clickContinueNo();
    }

    // ── Reference Count ───────────────────────────────────────────────────────

    public void selectReferenceCount() {
        referenceCount.baselineExistingRequestIds();
        referenceCount.clickIdsDropdown();
        referenceCount.selectReferenceCount();
    }

    public void verifyReferenceCountSelected(int instructionCountBefore) {
        referenceCount.verifyReferenceCountBubble();
        referenceCount.verifyNewApplicationNumbersInstruction(instructionCountBefore);
    }

    public int instructionCount() {
        return referenceCount.instructionCount();
    }

    public void enterReferenceCountQueryAndSubmit(String query) {
        referenceCount.enterQuery(query);
        referenceCount.clickSubmitButton();
    }

    public String awaitReferenceCountRequestId() {
        return referenceCount.awaitSubmissionAcknowledgement();
    }

    public List<String> referenceCountRequestIds() {
        return referenceCount.submittedRequestIds();
    }

    public void clickReferenceCountContinueYes() {
        referenceCount.clickContinueYes();
    }

    public void clickReferenceCountContinueNo() {
        referenceCount.clickContinueNo();
    }

    public void selectReferenceCountByTypingIntent(String intentText) {
        referenceCount.enterIntentAsQuery(intentText);
        referenceCount.clickSubmitButton();
    }

    public void verifyRequestCancelled() {
        referenceCount.verifyRequestCancelled();
    }

    public void verifyReferenceCountAvailableInDropdown() {
        referenceCount.verifyIdsDropdownAvailableAfterSelection();
    }

    // ── Reference Downloader ──────────────────────────────────────────────────

    public void selectReferenceDownloader() {
        referenceDownloader.baselineExistingRequestIds();
        referenceDownloader.clickIdsDropdown();
        referenceDownloader.selectReferenceDownloader();
    }

    public void verifyReferenceDownloaderSelected(int instructionCountBefore) {
        referenceDownloader.verifyReferenceDownloaderBubble();
        referenceDownloader.verifyNewReferenceNumbersInstruction(instructionCountBefore);
    }

    public int refDownloaderInstructionCount() {
        return referenceDownloader.instructionCount();
    }

    public void enterReferenceDownloaderQueryAndSubmit(String query) {
        referenceDownloader.enterQuery(query);
        referenceDownloader.clickSubmitButton();
    }

    public String awaitReferenceDownloaderTaskCompletion() {
        return referenceDownloader.awaitTaskCompletion();
    }

    public boolean isReferenceDownloaderRequestAcknowledgedAsync() {
        return referenceDownloader.lastSubmissionOutcome() == referenceDownloaderMenuPage.SubmissionOutcome.ACKNOWLEDGED;
    }

    public void verifyRefDownloaderDownloadLinkContainsRequestId(String requestId) {
        referenceDownloader.verifyDownloadLinkContainsRequestId(requestId);
    }

    public List<String> referenceDownloaderRequestIds() {
        return referenceDownloader.completedRequestIds();
    }

    public void clickReferenceDownloaderContinueYes() {
        referenceDownloader.clickContinueYes();
    }

    public void clickReferenceDownloaderContinueNo() {
        referenceDownloader.clickContinueNo();
    }

    public void selectReferenceDownloaderByTypingIntent(String intentText) {
        referenceDownloader.enterIntentAsQuery(intentText);
        referenceDownloader.clickSubmitButton();
    }

    public void verifyReferenceDownloaderRequestCancelled() {
        referenceDownloader.verifyRequestCancelled();
    }

    public void verifyReferenceDownloaderAvailableInDropdown() {
        referenceDownloader.verifyIdsDropdownAvailableAfterSelection();
    }
}