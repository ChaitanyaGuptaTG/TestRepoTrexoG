package test.ids;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.util.HashSet;
import java.util.List;

// Disabled: kept as-is from the original idsPageTest.java, relocated for structure only.
public class ReferenceExtractorTest extends AbstractWorkflowTest {

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "ReferenceExtractor"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "referenceExtractor"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

//    @Test(description = "Open June chat, open the IDS dropdown, select 'Reference Extractor', and verify it's selected", groups = "referenceExtractor")
    public void selectReferenceExtractor() {
        idsDownloaderPage.clickJuneIcon();
        idsDownloaderPage.selectReferenceExtractor();
        idsDownloaderPage.verifyReferenceExtractorSelected();
    }

//    @Test(description = "Upload a PDF to Reference Extractor and submit", groups = "referenceExtractor", dependsOnMethods = "selectReferenceExtractor", alwaysRun = true)
    public void uploadAndSubmitToReferenceExtractor() {
        idsDownloaderPage.uploadReferenceExtractorSampleFile();
        idsDownloaderPage.clickReferenceExtractorSubmitButton();

        String requestId = idsDownloaderPage.awaitReferenceExtractorRequestId();

        Assert.assertTrue(requestId.matches("\\d+"), "Reference Extractor Request ID should be numeric but was: " + requestId);
    }

//    @Test(description = "Continue Reference Extractor and submit a second file", groups = "referenceExtractor", dependsOnMethods = "uploadAndSubmitToReferenceExtractor", alwaysRun = true)
    public void continueAndSubmitSecondFileToReferenceExtractor() {
        idsDownloaderPage.clickReferenceExtractorContinueYes();

        idsDownloaderPage.uploadReferenceExtractorSampleFile();
        idsDownloaderPage.clickReferenceExtractorSubmitButton();

        idsDownloaderPage.awaitReferenceExtractorRequestId();
    }

//    @Test(description = "Every Reference Extractor submission produced a distinct Request ID", groups = "referenceExtractor", dependsOnMethods = "continueAndSubmitSecondFileToReferenceExtractor", alwaysRun = true)
    public void referenceExtractorRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.referenceExtractorRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Reference Extractor submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Reference Extractor submissions: " + ids);
    }

//    @Test(description = "Decline to continue and exit the Reference Extractor workflow", groups = "referenceExtractor", dependsOnMethods = "referenceExtractorRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueReferenceExtractor() {
        idsDownloaderPage.clickReferenceExtractorContinueNo();
    }

//    @Test(description = "IDS dropdown offers 'Reference Extractor' again once the workflow has exited", groups = "referenceExtractor", dependsOnMethods = "declineToContinueReferenceExtractor", alwaysRun = true)
    public void verifyReferenceExtractorDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyReferenceExtractorAvailableInDropdown();
    }
}
