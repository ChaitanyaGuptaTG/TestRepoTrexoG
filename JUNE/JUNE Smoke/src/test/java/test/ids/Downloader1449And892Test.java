package test.ids;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pages.IDSPage.idsPage;
import test.AbstractWorkflowTest;

import java.nio.file.Path;

public class Downloader1449And892Test extends AbstractWorkflowTest {

    private static final String DOWNLOADER_NAME = "1449 and 892 Downloader";

    private static final String APP_SINGLE_FIRST = "16999215";
    private static final String APP_BATCH_A = "17954142";
    private static final String APP_BATCH_B = "18139333";
    private static final String APP_SINGLE_SECOND = "18210779";

    private idsPage idsDownloaderPage;

    @Override
    protected String getWorkflowTag() { return "DocumentDownloader"; }

    @Override
    protected String getWorkflowConfigPrefix() { return "documentDownloader"; }

    @BeforeClass(alwaysRun = true)
    public void setUpIdsDownloaderPage() {
        performLogin();
        idsDownloaderPage = new idsPage(getDriver());
    }

    @Test(description = "Open June chat, open the IDS dropdown, select '1449 and 892 Downloader', and verify it's selected", groups = "documentDownloader")
    public void selectDocumentDownloader() {
        idsDownloaderPage.clickJuneIcon();
        idsDownloaderPage.selectDocumentDownloader();
        idsDownloaderPage.verifyDocumentDownloaderSelected();
    }

    @Test(description = "Submit a single application number and validate the downloaded zip", groups = "documentDownloader", dependsOnMethods = "selectDocumentDownloader", alwaysRun = true)
    public void submitSingleAppNumberAndValidateZip() {
        idsDownloaderPage.enterQueryAndSubmit(APP_SINGLE_FIRST);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_SINGLE_FIRST);
    }

    @Test(description = "Continue the workflow, submit two application numbers and validate", groups = "documentDownloader", dependsOnMethods = "submitSingleAppNumberAndValidateZip", alwaysRun = true)
    public void continueAndSubmitMultipleAppNumbers() {
        idsDownloaderPage.clickContinueYes();

        idsDownloaderPage.enterQueryAndSubmit(APP_BATCH_A + "\n" + APP_BATCH_B);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_BATCH_A, APP_BATCH_B);
    }

    @Test(description = "Decline to continue and exit the downloader workflow", groups = "documentDownloader", dependsOnMethods = "continueAndSubmitMultipleAppNumbers", alwaysRun = true)
    public void declineToContinue() {
        idsDownloaderPage.clickContinueNo();
    }

    @Test(description = "Re-enter the downloader by typing the intent name in the query box", groups = "documentDownloader", dependsOnMethods = "declineToContinue", alwaysRun = true)
    public void reEnterDownloaderByTypingIntent() {
        idsDownloaderPage.selectDocumentDownloaderByTypingIntent(DOWNLOADER_NAME);
        idsDownloaderPage.verifyApplicationNumbersInstructionShown();
    }

    @Test(description = "Submit a second single application number and validate the downloaded zip", groups = "documentDownloader", dependsOnMethods = "reEnterDownloaderByTypingIntent", alwaysRun = true)
    public void submitSecondSingleAppNumberAndValidateZip() {
        idsDownloaderPage.enterQueryAndSubmit(APP_SINGLE_SECOND);

        Path zip = idsDownloaderPage.clickDownloadAndVerifyZip();

        idsDownloaderPage.assertZipContents(zip, APP_SINGLE_SECOND);
    }

    @Test(description = "Decline to continue and exit the downloader workflow", groups = "documentDownloader", dependsOnMethods = "submitSecondSingleAppNumberAndValidateZip", alwaysRun = true)
    public void exitDownloaderAfterSecondSubmission() {
        idsDownloaderPage.clickContinueNo();
    }

    @Test(description = "IDS dropdown offers '1449 and 892 Downloader' again once the workflow has exited", groups = "documentDownloader", dependsOnMethods = "exitDownloaderAfterSecondSubmission", alwaysRun = true)
    public void verifyDownloaderDropdownAvailableAfterExit() {
        idsDownloaderPage.verifyIdsDropdownAvailableAfterSelection();
    }
}
