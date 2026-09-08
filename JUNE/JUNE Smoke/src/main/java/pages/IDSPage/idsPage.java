package pages.IDSPage;

import org.openqa.selenium.WebDriver;
import pages.help.HelpMenuPage;
import utils.Log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class idsPage {

    // Selenium clicks the dropdown far faster than a human would after declining a
    // workflow's continue dialog - the app needs a beat to finish resetting its
    // sidebar/session state, or it can replay the just-exited workflow instead of
    // switching (see referenceDownloaderMenuPage.selectReferenceDownloader()). This
    // buffer is a pragmatic stopgap, not a substitute for a real ready-signal.
    private static final Duration REFERENCE_DOWNLOADER_SELECT_BUFFER = Duration.ofSeconds(2);

    // Bundled test resource (checked into source control), not a path under the
    // machine's personal Downloads folder - resolved the same way ConfigReader
    // resolves its properties files, relative to the Maven working directory.
    private static final Path REFERENCE_EXTRACTOR_SAMPLE_PDF =
            Paths.get("src/test/resources/testfiles/ReferenceExtractorSample.pdf");

    private final documentDownloaderMenuPage menu;
    private final referenceCountMenuPage referenceCount;
    private final referenceDownloaderMenuPage referenceDownloader;
    private final referenceExtractorMenuPage referenceExtractor;
    private final HelpMenuPage helpMenu;

    public idsPage(WebDriver driver) {
        this.menu = new documentDownloaderMenuPage(driver);
        this.referenceCount = new referenceCountMenuPage(driver);
        this.referenceDownloader = new referenceDownloaderMenuPage(driver);
        this.referenceExtractor = new referenceExtractorMenuPage(driver);
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
        referenceCount.verifyReferenceCountBubble();
        referenceCount.selectReferenceCount();
    }

    public void verifyReferenceCountSelected(int instructionCountBefore) {
//        referenceCount.verifyReferenceCountBubble();
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
            // Catching InterruptedException CLEARS the interrupt flag - restore it so
            // whoever is shutting this thread down upstream still sees the cancellation.
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

    // The "N file attached" bubble is the SENT-message shape, confirmed live to only
    // replace the pre-submit staging chip (filename + a remove/X icon, sitting in the
    // input box) once submit is clicked - checking for it any earlier can never pass.
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
}