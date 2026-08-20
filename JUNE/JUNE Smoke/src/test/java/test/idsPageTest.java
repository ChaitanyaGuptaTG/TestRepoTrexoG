package test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.BaseTest;
import pages.LoginPage;
import pages.IDSPage.idsPage;
import utils.WaitUtils;

import java.nio.file.Path;

import static utils.DriverManager.getDriver;

public class idsPageTest extends BaseTest {

    private idsPage idePage;

    // ── Real application numbers from the dataset ──
    private static final String APP_1 = "16999215";
    private static final String APP_2 = "17954142";
    private static final String APP_3 = "18139333";

    // ── Additional application numbers (from Application_NUmbers.ods, US Patent
    //    Application Number column — 17/18 series, non-provisional) ──
    private static final String APP_4 = "18210779";
    private static final String APP_5 = "17691286";
    private static final String APP_6 = "17862541";

    @BeforeClass
    public void setUpIdePage() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(config.getProperty("username"));
        loginPage.enterPassword(config.getProperty("password"));
        loginPage.clickSignin();
        WaitUtils.waitForPageToLoadCompletely(getDriver(), 10);
        idePage = new pages.IDSPage.idsPage(getDriver());
    }

    @Test(description = "Open the June chat interface", priority = 1)
    public void clickTheJuneIcon() {
        idePage.clickJuneIcon();
    }

    @Test(description = "Open the IDS dropdown", priority = 2)
    public void clickTheIDSDropdown() {
        idePage.clickIDE();
    }

    @Test(description = "Select '1449 and 892 Downloader' from IDS", priority = 3)
    public void selectDocumentDownloader() {
        idePage.selectDocumentDownloader();
    }

    @Test(description = "Verify label and instruction appear after selection", priority = 4)
    public void verifyDocumentDownloaderSelected() {
        idePage.verifyDocumentDownloaderSelected();
    }


    @Test(description = "Submit a single application number and validate the downloaded zip",
            priority = 5)
    public void submitSingleAppNumberAndValidateZip() {
        idePage.enterQueryAndSubmit(APP_1);

        Path zip = idePage.clickDownloadAndVerifyTxt();

        // Deep validation: folder structure, 1449/892 subfolders, PDF naming
        idePage.assertZipContents(zip, APP_1);
    }

    @Test(description = "Click YES to continue, then submit two app numbers and validate",
            priority = 6)
    public void continueAndSubmitMultipleAppNumbers() {
        // June asks "Would you like to continue with 1449 and 892 Downloader?"
        idePage.clickContinueYes();

        // Submit two numbers separated by newline
        idePage.enterQueryAndSubmit(APP_2 + "\n" + APP_3);

        Path zip = idePage.clickDownloadAndVerifyTxt();

        // The zip should contain folders for both application numbers
        idePage.assertZipContents(zip, APP_2, APP_3);
    }

    @Test(description = "Click NO to exit the downloader workflow", priority = 7)
    public void declineToContinue() {
        idePage.clickContinueNo();
    }


    @Test(description = "Re-enter the downloader by typing the intent name in the query box",
            priority = 8)
    public void selectDocumentDownloaderByTypingIntent() {
        idePage.selectDocumentDownloaderByTypingIntent("1449 and 892 Downloader");
        idePage.verifyApplicationNumbersInstructionShown();
    }

    @Test(description = "Submit a second single application number and validate the downloaded zip",
            priority = 9)
    public void submitSecondSingleAppNumberAndValidateZip() {
        idePage.enterQueryAndSubmit(APP_4);

        Path zip = idePage.clickDownloadAndVerifyTxt();

        idePage.assertZipContents(zip, APP_4);
    }

    @Test(description = "Click YES to continue, then submit two more app numbers and validate",
            priority = 10)
    public void continueAndSubmitMoreAppNumbers() {
        idePage.clickContinueYes();

        idePage.enterQueryAndSubmit(APP_5 + "\n" + APP_6);

        Path zip = idePage.clickDownloadAndVerifyTxt();

        idePage.assertZipContents(zip, APP_5, APP_6);
    }

    @Test(description = "Click NO to exit the downloader workflow (second cycle)", priority = 11)
    public void declineToContinueSecondCycle() {
        idePage.clickContinueNo();
    }

    @Test(description = "IDS dropdown stays disabled after a downloader is selected",
            priority = 12)
    public void ideDropdownDisabledAfterSelection() {
        idePage.verifyIdsDropdownDisabledAfterSelection();
    }
}