package test.ids;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.util.HashSet;
import java.util.List;

public class RemoveEmbeddedFontsTest extends AbstractWorkflowTest {

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "RemoveEmbeddedFonts"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "removeEmbeddedFonts"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

    @Test(description = "Select 'Remove Embedded Fonts' from the IDS dropdown", groups = "removeEmbeddedFonts")
    public void selectRemoveEmbeddedFonts() {
        idsDownloaderPage.clickJuneIcon();
        idsDownloaderPage.selectRemoveEmbeddedFonts();
        idsDownloaderPage.verifyRemoveEmbeddedFontsSelected();
    }

    @Test(description = "Upload a PDF to Remove Embedded Fonts, submit, and verify the processed file downloads", groups = "removeEmbeddedFonts", dependsOnMethods = "selectRemoveEmbeddedFonts", alwaysRun = true)
    public void uploadAndSubmitToRemoveEmbeddedFonts() {
        idsDownloaderPage.uploadRemoveEmbeddedFontsSampleFile();
        idsDownloaderPage.clickRemoveEmbeddedFontsSubmitButton();

        String requestId = idsDownloaderPage.awaitRemoveEmbeddedFontsRequestId();

        Assert.assertTrue(requestId.matches("\\d+"), "Remove Embedded Fonts Request ID should be numeric but was: " + requestId);
    }

    @Test(description = "Continue Remove Embedded Fonts and submit a second file", groups = "removeEmbeddedFonts", dependsOnMethods = "uploadAndSubmitToRemoveEmbeddedFonts", alwaysRun = true)
    public void continueAndSubmitSecondFileToRemoveEmbeddedFonts() {
        idsDownloaderPage.clickRemoveEmbeddedFontsContinueYes();

        idsDownloaderPage.uploadRemoveEmbeddedFontsSampleFile();
        idsDownloaderPage.clickRemoveEmbeddedFontsSubmitButton();

        idsDownloaderPage.awaitRemoveEmbeddedFontsRequestId();
    }

    @Test(description = "Every Remove Embedded Fonts submission produced a distinct Request ID", groups = "removeEmbeddedFonts", dependsOnMethods = "continueAndSubmitSecondFileToRemoveEmbeddedFonts", alwaysRun = true)
    public void removeEmbeddedFontsRequestIdsAreDistinct() {
        List<String> ids = idsDownloaderPage.removeEmbeddedFontsRequestIds();

        Assert.assertEquals(ids.size(), 2, "Expected two Remove Embedded Fonts submissions, got: " + ids);
        Assert.assertEquals(new HashSet<>(ids).size(), ids.size(), "Duplicate Request IDs across Remove Embedded Fonts submissions: " + ids);
    }

    @Test(description = "Decline to continue and exit the Remove Embedded Fonts workflow", groups = "removeEmbeddedFonts", dependsOnMethods = "removeEmbeddedFontsRequestIdsAreDistinct", alwaysRun = true)
    public void declineToContinueRemoveEmbeddedFonts() {
        idsDownloaderPage.clickRemoveEmbeddedFontsContinueNo();
    }

    @Test(description = "IDS dropdown offers 'Remove Embedded Fonts' again once the workflow has exited", groups = "removeEmbeddedFonts", dependsOnMethods = "declineToContinueRemoveEmbeddedFonts", alwaysRun = true)
    public void verifyRemoveEmbeddedFontsDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyRemoveEmbeddedFontsAvailableInDropdown();
    }
}
