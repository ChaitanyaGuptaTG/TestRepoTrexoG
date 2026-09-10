package test.ids;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.util.HashSet;
import java.util.List;

// Disabled: kept as-is from the original idsPageTest.java, relocated for structure only.
public class CorrespondingRefCheckTest extends AbstractWorkflowTest {

    private static final String CORR_REF_APP_SINGLE = "11111111";
    private static final String CORR_REF_SINGLE_REFERENCE = "US20200245210A1";
    private static final String CORR_REF_APP_BATCH = "11111112";
    private static final List<String> CORR_REF_BATCH_REFERENCES = List.of("US20200245210A1", "US20200145150A1", "US20030179831A1", "US10637694B1");

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "CorrespondingRefCheck"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "correspondingRefCheck"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

//    @Test(description = "Open June chat, open the IDS dropdown, select 'Corresponding RefCheck', and verify it's selected", groups = "correspondingRefCheck")
    public void selectCorrespondingRefCheck() {
        idsDownloaderPage.clickJuneIcon();
        int instructionsBefore = idsDownloaderPage.correspondingRefCheckApplicationNumberInstructionCount();

        idsDownloaderPage.selectCorrespondingRefCheck();

        idsDownloaderPage.verifyCorrespondingRefCheckSelected(instructionsBefore);
    }

//    @Test(description = "Submit a single application number and one reference to Corresponding RefCheck", groups = "correspondingRefCheck", dependsOnMethods = "selectCorrespondingRefCheck", alwaysRun = true)
    public void submitSingleAppNumberAndReferenceToCorrespondingRefCheck() {
        idsDownloaderPage.enterCorrespondingRefCheckApplicationNumberAndSubmit(CORR_REF_APP_SINGLE);

        idsDownloaderPage.enterCorrespondingRefCheckSingleReferenceAndSubmit(CORR_REF_SINGLE_REFERENCE);

        String requestId = idsDownloaderPage.awaitCorrespondingRefCheckRequestId();

        Assert.assertTrue(requestId.matches("\\d+"), "Corresponding RefCheck Request ID should be numeric but was: " + requestId);
    }

//    @Test(description = "Continue Corresponding RefCheck and submit a comma-separated batch of references", groups = "correspondingRefCheck", dependsOnMethods = "submitSingleAppNumberAndReferenceToCorrespondingRefCheck", alwaysRun = true)
    public void continueAndSubmitBatchToCorrespondingRefCheck() {
        idsDownloaderPage.clickCorrespondingRefCheckContinueYes();

        idsDownloaderPage.enterCorrespondingRefCheckApplicationNumberAndSubmit(CORR_REF_APP_BATCH);

        idsDownloaderPage.enterCorrespondingRefCheckReferencesCommaSeparatedAndSubmit(CORR_REF_BATCH_REFERENCES);

        idsDownloaderPage.awaitCorrespondingRefCheckRequestId();
    }

//    @Test(description = "Every Corresponding RefCheck submission produced a distinct Request ID", groups = "correspondingRefCheck", dependsOnMethods = "continueAndSubmitBatchToCorrespondingRefCheck", alwaysRun = true)
    public void correspondingRefCheckRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.correspondingRefCheckRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Corresponding RefCheck submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Corresponding RefCheck submissions: " + ids);
    }

//    @Test(description = "Decline to continue and exit the Corresponding RefCheck workflow", groups = "correspondingRefCheck", dependsOnMethods = "correspondingRefCheckRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueCorrespondingRefCheck() {
        idsDownloaderPage.clickCorrespondingRefCheckContinueNo();
    }

//    @Test(description = "IDS dropdown offers 'Corresponding RefCheck' again once the workflow has exited", groups = "correspondingRefCheck", dependsOnMethods = "declineToContinueCorrespondingRefCheck", alwaysRun = true)
    public void verifyCorrespondingRefCheckDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyCorrespondingRefCheckAvailableInDropdown();
    }
}
