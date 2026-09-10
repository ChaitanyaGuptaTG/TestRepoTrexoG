package test.ids;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.util.HashSet;
import java.util.List;

public class ReferenceDownloaderTest extends AbstractWorkflowTest {

    private static final String REF_DL_SINGLE = "US1234567A";
    private static final String REF_DL_BATCH_A = "US4683202A";
    private static final String REF_DL_BATCH_B = "DE102015013053A1";
    private static final String REF_DL_BATCH_C = "AU2010219336A1";

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "ReferenceDownloader"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "referenceDownloader"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

    @Test(description = "Open June chat, open the IDS dropdown, select 'Reference Downloader', and verify it's selected", groups = "referenceDownloader")
    public void selectReferenceDownloader() {
        idsDownloaderPage.clickJuneIcon();
        idsDownloaderPage.selectReferenceDownloader();
        int instructionsBefore = idsDownloaderPage.refDownloaderInstructionCount();

        idsDownloaderPage.verifyReferenceDownloaderSelected(instructionsBefore);
    }

    @Test(description = "Submit a single reference number to Reference Downloader", groups = "referenceDownloader", dependsOnMethods = "selectReferenceDownloader", alwaysRun = true)
    public void submitSingleRefNumberToReferenceDownloader() {
        idsDownloaderPage.enterReferenceDownloaderQueryAndSubmit(REF_DL_SINGLE);

        String requestId = idsDownloaderPage.awaitReferenceDownloaderTaskCompletion();

        Assert.assertTrue(requestId.matches("\\d+"), "Reference Downloader Request ID should be numeric but was: " + requestId);

        idsDownloaderPage.verifyRefDownloaderDownloadLinkContainsRequestId(requestId);
    }

    @Test(description = "Continue Reference Downloader and submit a batch of three reference numbers", groups = "referenceDownloader", dependsOnMethods = "submitSingleRefNumberToReferenceDownloader", alwaysRun = true)
    public void continueAndSubmitBatchToReferenceDownloader() {
        idsDownloaderPage.clickReferenceDownloaderContinueYes();

        idsDownloaderPage.enterReferenceDownloaderQueryAndSubmit(
                REF_DL_BATCH_A + "\n" + REF_DL_BATCH_B + "\n" + REF_DL_BATCH_C);

        String requestId = idsDownloaderPage.awaitReferenceDownloaderTaskCompletion();

        if (idsDownloaderPage.isReferenceDownloaderRequestAcknowledgedAsync()) {

        } else {
            idsDownloaderPage.verifyRefDownloaderDownloadLinkContainsRequestId(requestId);
        }
    }

    @Test(description = "Every Reference Downloader submission produced a distinct Request ID", groups = "referenceDownloader", dependsOnMethods = "continueAndSubmitBatchToReferenceDownloader", alwaysRun = true)
    public void referenceDownloaderRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.referenceDownloaderRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Reference Downloader submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Reference Downloader submissions: " + ids);
    }

    @Test(description = "Decline to continue and exit the Reference Downloader workflow", groups = "referenceDownloader", dependsOnMethods = "referenceDownloaderRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueReferenceDownloader() {
        idsDownloaderPage.clickReferenceDownloaderContinueNo();
    }

    @Test(description = "IDS dropdown offers 'Reference Downloader' again once the workflow has exited", groups = "referenceDownloader", dependsOnMethods = "declineToContinueReferenceDownloader", alwaysRun = true)
    public void verifyReferenceDownloaderDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyReferenceDownloaderAvailableInDropdown();
    }
}
