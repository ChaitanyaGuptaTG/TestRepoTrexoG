package pages.IDSPage;

import org.openqa.selenium.WebDriver;
import pages.help.HelpMenuPage;
import utils.Log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class idsPage {

    private static final Duration REFERENCE_DOWNLOADER_SELECT_BUFFER = Duration.ofSeconds(2);

    private static final Duration REFERENCE_COUNT_SELECT_BUFFER = Duration.ofSeconds(1);
    private static final Path REFERENCE_EXTRACTOR_SAMPLE_PDF = Paths.get("src/test/resources/testfiles/ReferenceExtractorSample.pdf");

    private final documentDownloaderMenuPage menu;
    private final referenceCountMenuPage referenceCount;
    private final referenceDownloaderMenuPage referenceDownloader;
    private final referenceExtractorMenuPage referenceExtractor;
    private final removeEmbeddedFontsMenuPage removeEmbeddedFonts;
    private final correspondingRefCheckMenuPage correspondingRefCheck;
    private final HelpMenuPage helpMenu;

    public idsPage(WebDriver driver) {
        this.menu = new documentDownloaderMenuPage(driver);
        this.referenceCount = new referenceCountMenuPage(driver);
        this.referenceDownloader = new referenceDownloaderMenuPage(driver);
        this.referenceExtractor = new referenceExtractorMenuPage(driver);
        this.removeEmbeddedFonts = new removeEmbeddedFontsMenuPage(driver);
        this.correspondingRefCheck = new correspondingRefCheckMenuPage(driver);
        this.helpMenu = new HelpMenuPage(driver);
    }

    // Common

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

    // 1449 and 892 Downloader
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

    // Reference Count

    public void selectReferenceCount() {
        referenceCount.baselineExistingRequestIds();
        referenceCount.clickIdsDropdown();
        sleepUninterruptibly(REFERENCE_COUNT_SELECT_BUFFER);
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
//        referenceDownloader.baselineExistingRequestIds();
        sleepUninterruptibly(REFERENCE_DOWNLOADER_SELECT_BUFFER);
        referenceDownloader.clickIdsDropdown();
        referenceDownloader.selectReferenceDownloader();
    }

    private static void sleepUninterruptibly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            Log.warn("Interrupted while waiting before selecting Reference Downloader");
        }
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

    // ── Reference Extractor ───────────────────────────────────────────────────

    public void selectReferenceExtractor() {
        referenceExtractor.baselineExistingState();
        referenceExtractor.clickIdsDropdown();
        referenceExtractor.selectReferenceExtractor();
    }

    public void verifyReferenceExtractorSelected() {
        referenceExtractor.verifyUploadInstructionShown();
    }

    public void uploadReferenceExtractorSampleFile() {
        referenceExtractor.uploadFile(REFERENCE_EXTRACTOR_SAMPLE_PDF);
    }

    public void clickReferenceExtractorSubmitButton() {
        referenceExtractor.clickSubmitButton();
        referenceExtractor.verifyFileAttachedBubble(REFERENCE_EXTRACTOR_SAMPLE_PDF.getFileName().toString());
    }

    public String awaitReferenceExtractorRequestId() {
        return referenceExtractor.awaitSubmissionAcknowledgement();
    }

    public List<String> referenceExtractorRequestIds() {
        return referenceExtractor.submittedRequestIds();
    }

    public void clickReferenceExtractorContinueYes() {
        referenceExtractor.clickContinueYes();
    }

    public void clickReferenceExtractorContinueNo() {
        referenceExtractor.clickContinueNo();
    }

    public void verifyReferenceExtractorAvailableInDropdown() {
        referenceExtractor.verifyIdsDropdownAvailableAfterSelection();
    }

    // ── Remove Embedded Fonts ─────────────────────────────────────────────────

    public void selectRemoveEmbeddedFonts() {
        removeEmbeddedFonts.baselineExistingState();
        removeEmbeddedFonts.clickIdsDropdown();
        removeEmbeddedFonts.selectRemoveEmbeddedFonts();
    }

    public void verifyRemoveEmbeddedFontsSelected() {
        removeEmbeddedFonts.verifyUploadInstructionShown();
    }

    public void uploadRemoveEmbeddedFontsSampleFile() {
        removeEmbeddedFonts.uploadFile(REFERENCE_EXTRACTOR_SAMPLE_PDF);
    }

    public void clickRemoveEmbeddedFontsSubmitButton() {
        removeEmbeddedFonts.clickSubmitButton();
        removeEmbeddedFonts.verifyFileAttachedBubble(REFERENCE_EXTRACTOR_SAMPLE_PDF.getFileName().toString());
    }

    public String awaitRemoveEmbeddedFontsRequestId() {
        return removeEmbeddedFonts.awaitSubmissionAcknowledgement();
    }

    public List<String> removeEmbeddedFontsRequestIds() {
        return removeEmbeddedFonts.submittedRequestIds();
    }

    public void clickRemoveEmbeddedFontsContinueYes() {
        removeEmbeddedFonts.clickContinueYes();
    }

    public void clickRemoveEmbeddedFontsContinueNo() {
        removeEmbeddedFonts.clickContinueNo();
    }

    public void verifyRemoveEmbeddedFontsAvailableInDropdown() {
        removeEmbeddedFonts.verifyIdsDropdownAvailableAfterSelection();
    }

    // ── Corresponding RefCheck ────────────────────────────────────────────────

    public void selectCorrespondingRefCheck() {
        correspondingRefCheck.baselineExistingRequestIds();
        correspondingRefCheck.clickIdsDropdown();
        correspondingRefCheck.selectCorrespondingRefCheck();
    }

    public void verifyCorrespondingRefCheckSelected(int instructionCountBefore) {
        correspondingRefCheck.verifyCorrespondingRefCheckBubble();
        correspondingRefCheck.verifyNewApplicationNumberInstruction(instructionCountBefore);
    }

    public int correspondingRefCheckApplicationNumberInstructionCount() {
        return correspondingRefCheck.applicationNumberInstructionCount();
    }

    public void enterCorrespondingRefCheckApplicationNumberAndSubmit(String applicationNumber) {
        int before = correspondingRefCheck.referenceListInstructionCount();

        correspondingRefCheck.enterQuery(applicationNumber);
        correspondingRefCheck.clickSubmitButton();

        correspondingRefCheck.verifyNewReferenceListInstruction(before);
    }

    public void enterCorrespondingRefCheckSingleReferenceAndSubmit(String reference) {
        correspondingRefCheck.enterSingleReference(reference);
        correspondingRefCheck.clickSubmitButton();
    }

    public void enterCorrespondingRefCheckReferencesCommaSeparatedAndSubmit(List<String> references) {
        correspondingRefCheck.enterReferencesCommaSeparated(references);
        correspondingRefCheck.clickSubmitButton();
    }

    public String awaitCorrespondingRefCheckRequestId() {
        return correspondingRefCheck.awaitSubmissionAcknowledgement();
    }

    public List<String> correspondingRefCheckRequestIds() {
        return correspondingRefCheck.submittedRequestIds();
    }

    public void clickCorrespondingRefCheckContinueYes() {
        correspondingRefCheck.clickContinueYes();
    }

    public void clickCorrespondingRefCheckContinueNo() {
        correspondingRefCheck.clickContinueNo();
    }

    public void verifyCorrespondingRefCheckAvailableInDropdown() {
        correspondingRefCheck.verifyIdsDropdownAvailableAfterSelection();
    }
}