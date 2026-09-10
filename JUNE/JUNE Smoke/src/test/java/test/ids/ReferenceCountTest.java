package test.ids;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.util.HashSet;
import java.util.List;

public class ReferenceCountTest extends AbstractWorkflowTest {

    private static final String REF_COUNT_SINGLE = "17954142";
    private static final String REF_COUNT_BATCH_A = "18139333";
    private static final String REF_COUNT_BATCH_B = "16999215";

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "ReferenceCount"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "referenceCount"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

    @Test(description = "Open June chat, open the IDS dropdown, select 'Reference Count', and verify it's selected", groups = "referenceCount")
    public void selectReferenceCount() {
        idsDownloaderPage.clickJuneIcon();
        int instructionsBefore = idsDownloaderPage.instructionCount();

        idsDownloaderPage.selectReferenceCount();

        idsDownloaderPage.verifyReferenceCountSelected(instructionsBefore);
    }

    @Test(description = "Submit a single application number to Reference Count", groups = "referenceCount", dependsOnMethods = "selectReferenceCount", alwaysRun = true)
    public void submitSingleAppNumberToReferenceCount() {
        idsDownloaderPage.enterReferenceCountQueryAndSubmit(REF_COUNT_SINGLE);

        String requestId = idsDownloaderPage.awaitReferenceCountRequestId();

        Assert.assertTrue(requestId.matches("\\d+"), "Reference Count Request ID should be numeric but was: " + requestId);
    }

    @Test(description = "Continue Reference Count and submit two application numbers", groups = "referenceCount", dependsOnMethods = "submitSingleAppNumberToReferenceCount", alwaysRun = true)
    public void continueAndSubmitMultipleToReferenceCount() {
        idsDownloaderPage.clickReferenceCountContinueYes();

        idsDownloaderPage.enterReferenceCountQueryAndSubmit(
                REF_COUNT_BATCH_A + "\n" + REF_COUNT_BATCH_B);

        idsDownloaderPage.awaitReferenceCountRequestId();
    }

    @Test(description = "Every Reference Count submission produced a distinct Request ID", groups = "referenceCount", dependsOnMethods = "continueAndSubmitMultipleToReferenceCount", alwaysRun = true)
    public void referenceCountRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.referenceCountRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Reference Count submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Reference Count submissions: " + ids);
    }

    @Test(description = "Decline to continue and exit the Reference Count workflow", groups = "referenceCount", dependsOnMethods = "referenceCountRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueReferenceCount() {
        idsDownloaderPage.clickReferenceCountContinueNo();
    }

    @Test(description = "IDS dropdown offers 'Reference Count' again once the workflow has exited",
            groups = "referenceCount", dependsOnMethods = "declineToContinueReferenceCount", alwaysRun = true)
    public void verifyReferenceCountDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyReferenceCountAvailableInDropdown();
    }
}
