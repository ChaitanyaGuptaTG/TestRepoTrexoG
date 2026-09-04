package test;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.IDSPage.idsPage;
import pages.LoginPage;
import utils.WaitUtils;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

public class idsPageTest extends BaseTest {

    private static final String DOWNLOADER_NAME = "1449 and 892 Downloader";
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 10;

    // ── 1449 and 892 Downloader application numbers ──
    private static final String APP_SINGLE_FIRST = "16999215";
    private static final String APP_BATCH_A = "17954142";
    private static final String APP_BATCH_B = "18139333";
    private static final String APP_SINGLE_SECOND = "18210779";

    // ── Reference Count uses its own numbers so a failure names the right workflow ──
    private static final String REF_COUNT_SINGLE = "17954142";
    private static final String REF_COUNT_BATCH_A = "18139333";
    private static final String REF_COUNT_BATCH_B = "16999215";

    // ── Reference Downloader uses its own numbers so a failure names the right workflow ──
    private static final String REF_DL_SINGLE = "US1234567A";
    private static final String REF_DL_BATCH_A = "US4683202A";
    private static final String REF_DL_BATCH_B = "DE102015013053A1";
    private static final String REF_DL_BATCH_C = "AU2010219336A1";

    private idsPage idsDownloaderPage;

    @BeforeClass
    public void setUpIdsDownloaderPage() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(config.getProperty("username"));
        loginPage.enterPassword(config.getProperty("password"));
        loginPage.clickSignin();
        WaitUtils.waitForPageToLoadCompletely(getDriver(), PAGE_LOAD_TIMEOUT_SECONDS);

        idsDownloaderPage = new idsPage(getDriver());
    }

    // 1449 and 892 Downloader

    @Test(description = "Open June chat, open the IDS dropdown, select '1449 and 892 Downloader', and verify it's selected")
    public void selectDocumentDownloader() {
        idsDownloaderPage.clickJuneIcon();
        idsDownloaderPage.clickIdsDropdown();
        idsDownloaderPage.selectDocumentDownloader();
        idsDownloaderPage.verifyDocumentDownloaderSelected();
    }

    @Test(description = "Submit a single application number and validate the downloaded zip", dependsOnMethods = "selectDocumentDownloader", alwaysRun = true)
    public void submitSingleAppNumberAndValidateZip() {
        idsDownloaderPage.enterQueryAndSubmit(APP_SINGLE_FIRST);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_SINGLE_FIRST);
    }

    @Test(description = "Continue the workflow, submit two application numbers and validate", dependsOnMethods = "submitSingleAppNumberAndValidateZip", alwaysRun = true)
    public void continueAndSubmitMultipleAppNumbers() {
        idsDownloaderPage.clickContinueYes();

        idsDownloaderPage.enterQueryAndSubmit(APP_BATCH_A + "\n" + APP_BATCH_B);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_BATCH_A, APP_BATCH_B);
    }

    @Test(description = "Decline to continue and exit the downloader workflow", dependsOnMethods = "continueAndSubmitMultipleAppNumbers", alwaysRun = true)
    public void declineToContinue() {
        idsDownloaderPage.clickContinueNo();
    }

    @Test(description = "Re-enter the downloader by typing the intent name in the query box", dependsOnMethods = "declineToContinue", alwaysRun = true)
    public void reEnterDownloaderByTypingIntent() {
        idsDownloaderPage.selectDocumentDownloaderByTypingIntent(DOWNLOADER_NAME);
        idsDownloaderPage.verifyApplicationNumbersInstructionShown();
    }

    @Test(description = "Submit a second single application number and validate the downloaded zip", dependsOnMethods = "reEnterDownloaderByTypingIntent", alwaysRun = true)
    public void submitSecondSingleAppNumberAndValidateZip() {
        idsDownloaderPage.enterQueryAndSubmit(APP_SINGLE_SECOND);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_SINGLE_SECOND);
    }

    @Test(description = "Exit the downloader so the session can move on to Reference Count", dependsOnMethods = "submitSecondSingleAppNumberAndValidateZip", alwaysRun = true)
    public void exitDownloaderBeforeReferenceCount() {
        idsDownloaderPage.clickContinueNo();
    }

    @Test(description = "IDS dropdown offers '1449 and 892 Downloader' again once the workflow has exited", dependsOnMethods = "exitDownloaderBeforeReferenceCount", alwaysRun = true)
    public void verifyDownloaderDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyIdsDropdownAvailableAfterSelection();
    }

    // Reference Count

    @Test(description = "Select 'Reference Count' from the IDS dropdown after the downloader has exited", dependsOnMethods = "verifyDownloaderDropdownAvailableAfterExit", alwaysRun = true)
    public void selectReferenceCount() {
        int instructionsBefore = idsDownloaderPage.instructionCount();

        idsDownloaderPage.selectReferenceCount();

        idsDownloaderPage.verifyReferenceCountSelected(instructionsBefore);
    }

    @Test(description = "Submit a single application number to Reference Count", dependsOnMethods = "selectReferenceCount", alwaysRun = true)
    public void submitSingleAppNumberToReferenceCount() {
        idsDownloaderPage.enterReferenceCountQueryAndSubmit(REF_COUNT_SINGLE);

        String requestId = idsDownloaderPage.awaitReferenceCountRequestId();

        Assert.assertTrue(requestId.matches("\\d+"), "Reference Count Request ID should be numeric but was: " + requestId);
    }

    @Test(description = "Continue Reference Count and submit two application numbers", dependsOnMethods = "submitSingleAppNumberToReferenceCount", alwaysRun = true)
    public void continueAndSubmitMultipleToReferenceCount() {
        idsDownloaderPage.clickReferenceCountContinueYes();

        idsDownloaderPage.enterReferenceCountQueryAndSubmit(
                REF_COUNT_BATCH_A + "\n" + REF_COUNT_BATCH_B);

        idsDownloaderPage.awaitReferenceCountRequestId();
    }

    @Test(description = "Every Reference Count submission produced a distinct Request ID", dependsOnMethods = "continueAndSubmitMultipleToReferenceCount", alwaysRun = true)
    public void referenceCountRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.referenceCountRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Reference Count submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Reference Count submissions: " + ids);
    }

    @Test(description = "Decline to continue and exit the Reference Count workflow", dependsOnMethods = "referenceCountRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueReferenceCount() {
        idsDownloaderPage.clickReferenceCountContinueNo();
    }

    @Test(description = "IDS dropdown offers 'Reference Count' again once the workflow has exited", dependsOnMethods = "declineToContinueReferenceCount", alwaysRun = true)
    public void verifyReferenceCountDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyReferenceCountAvailableInDropdown();
    }

    // Reference Downloader

    @Test(description = "Select 'Reference Downloader' from the IDS dropdown after Reference Count has exited", dependsOnMethods = "verifyReferenceCountDropdownAvailableAfterExit", alwaysRun = true)
    public void selectReferenceDownloader() {
        int instructionsBefore = idsDownloaderPage.refDownloaderInstructionCount();

        idsDownloaderPage.selectReferenceDownloader();

        idsDownloaderPage.verifyReferenceDownloaderSelected(instructionsBefore);
    }

    @Test(description = "Submit a single reference number to Reference Downloader", dependsOnMethods = "selectReferenceDownloader", alwaysRun = true)
    public void submitSingleRefNumberToReferenceDownloader() {
        idsDownloaderPage.enterReferenceDownloaderQueryAndSubmit(REF_DL_SINGLE);

        String requestId = idsDownloaderPage.awaitReferenceDownloaderTaskCompletion();

        Assert.assertTrue(requestId.matches("\\d+"), "Reference Downloader Request ID should be numeric but was: " + requestId);

        idsDownloaderPage.verifyRefDownloaderDownloadLinkContainsRequestId(requestId);
    }

    @Test(description = "Continue Reference Downloader and submit a batch of three reference numbers", dependsOnMethods = "submitSingleRefNumberToReferenceDownloader", alwaysRun = true)
    public void continueAndSubmitBatchToReferenceDownloader() {
        idsDownloaderPage.clickReferenceDownloaderContinueYes();

        idsDownloaderPage.enterReferenceDownloaderQueryAndSubmit(
                REF_DL_BATCH_A + "\n" + REF_DL_BATCH_B + "\n" + REF_DL_BATCH_C);

        String requestId = idsDownloaderPage.awaitReferenceDownloaderTaskCompletion();

        if (idsDownloaderPage.isReferenceDownloaderRequestAcknowledgedAsync()) {
            // Async path (e.g. DE102015013053A1): request is queued, no download link yet.
            // Actual completion is verified separately via Collab - not covered here.
        } else {
            idsDownloaderPage.verifyRefDownloaderDownloadLinkContainsRequestId(requestId);
        }
    }

    @Test(description = "Every Reference Downloader submission produced a distinct Request ID", dependsOnMethods = "continueAndSubmitBatchToReferenceDownloader", alwaysRun = true)
    public void referenceDownloaderRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.referenceDownloaderRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Reference Downloader submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Reference Downloader submissions: " + ids);
    }

    @Test(description = "Decline to continue and exit the Reference Downloader workflow", dependsOnMethods = "referenceDownloaderRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueReferenceDownloader() {
        idsDownloaderPage.clickReferenceDownloaderContinueNo();
    }

    @Test(description = "IDS dropdown offers 'Reference Downloader' again once the workflow has exited", dependsOnMethods = "declineToContinueReferenceDownloader", alwaysRun = true)
    public void verifyReferenceDownloaderDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyReferenceDownloaderAvailableInDropdown();
    }
}
