package pages.IDSPage;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import pages.IDSPage.helpers.ContinueDialogHelper;
import pages.IDSPage.helpers.DownloadFileHelper;
import pages.IDSPage.helpers.IdsDropdownHelper;
import pages.IDSPage.helpers.QueryInputHelper;
import pages.IDSPage.helpers.UsptoCountCrossChecker;
import pages.IDSPage.helpers.ZipContentValidator;
import utils.ConfigReader;
import utils.Log;
import utils.OutputFileWorkflowManager;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Page object for the "1449 and 892 Downloader" IDS workflow: selecting it
 * from the IDS dropdown, submitting application numbers, and validating the
 * resulting zip download. The mechanics shared with the other IDS workflows
 * (dropdown, query box, continue dialog, filesystem download polling) live in
 * {@code pages.IDSPage.helpers}; this class only owns what's specific to this
 * workflow - the download-link wait, and the zip/PDF/API validation.
 */
public class documentDownloaderMenuPage extends BasePage {


    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);


    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(10);


    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(30);


    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;


    private static final long DOWNLOAD_POLL_MILLIS = 1000L;


    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    private static final String FORM_1449 = "1449";
    private static final String DOWNLOADER_NAME = "1449 and 892 Downloader";
    private static final String QUERY_PLACEHOLDER = "Enter your query or select a task to get started";


    private static final By DOWNLOADER_OPTION = By.xpath("//li[@role='option' and contains(.,'" + DOWNLOADER_NAME + "')]");

    private static final By DOWNLOADER_LABEL = By.xpath("//span[normalize-space()='" + DOWNLOADER_NAME + "']");

    private static final By APP_NUMBERS_INSTRUCTION = By.xpath("(//span[contains(text(),'Please enter the application numbers')])[last()]");

    private static final By QUERY_INPUT = By.cssSelector("textarea[placeholder='" + QUERY_PLACEHOLDER + "']");

    // SVG lives in its own XML namespace, so //svg does NOT work in XPath.
    // //*[name()='svg'] is the standard workaround.
    private static final By SUBMIT_BUTTON = By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']");

    private static final By STOP_BUTTON = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");

    private static final By COMPLETION_MESSAGE = By.xpath("(//p[contains(text(),'Your task with Request ID') and contains(text(),'has been completed')])[last()]");

    private static final By REQUEST_ID_MESSAGE = By.xpath("(//p[contains(text(),'Request ID')])[last()]");

    /**
     * ALL matching download links (no [last()]) - we need the full list to detect a NEW one.
     */
    private static final By ALL_DOWNLOAD_LINKS = By.xpath("//a[@download and contains(@href,'" + FORM_1449 + "') and contains(@href,'.zip')]");

    private static final By CONTINUE_CONFIRMATION = By.xpath("(//span[contains(text(),'Would you like to continue with " + DOWNLOADER_NAME + "')])[last()]");

    // ancestor::div scopes the Yes/No to THIS dialog; [last()] picks the newest one
    // in the transcript. Both are needed - other dialogs also have Yes/No buttons.
    private static final By CONTINUE_YES = By.xpath("(//span[contains(text(),'Would you like to continue')]/ancestor::div//button[normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(//span[contains(text(),'Would you like to continue')]/ancestor::div//button[normalize-space()='No'])[last()]");

    private static final By WORKFLOW_EXITED_MESSAGE = By.xpath("(//*[contains(text(),'" + QUERY_PLACEHOLDER + "')])[last()]");

    private String lastDownloadHref = null;

    private final IdsDropdownHelper dropdown;
    private final QueryInputHelper queryInput;
    private final ContinueDialogHelper continueDialog;
    private final UsptoCountCrossChecker usptoCrossChecker;

    public documentDownloaderMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.queryInput = new QueryInputHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
        this.usptoCrossChecker = new UsptoCountCrossChecker();
    }

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectDocumentDownloader() {
        waitVisible(DOWNLOADER_OPTION).click();
        Log.info("Selected '{}' from the IDS dropdown", DOWNLOADER_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(DOWNLOADER_OPTION, DOWNLOADER_NAME, QUICK_PROBE_TIMEOUT);
    }

    public void verifyDocumentDownloaderLabel() {
        Assert.assertTrue(waitVisible(DOWNLOADER_LABEL).isDisplayed(), "'" + DOWNLOADER_NAME + "' label is not displayed after selection");
        Log.pass("'{}' label is displayed", DOWNLOADER_NAME);
    }

    public void verifyApplicationNumbersInstruction() {
        Assert.assertTrue(waitVisible(APP_NUMBERS_INSTRUCTION).isDisplayed(), "Application numbers instruction is not displayed");
        Log.pass("Application numbers instruction is displayed");
    }

    public void enterQuery(String query) {
        queryInput.enterQuery(query);
    }

    public void enterIntentAsQuery(String intentText) {
        queryInput.enterIntentAsQuery(intentText);
    }

    public void clickSubmitButton() {
        queryInput.clickSubmitButton();
    }

    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickYes(CONTINUE_YES, DOWNLOADER_NAME);

        // Wait for the EFFECT, not just the click: the instruction prompt reappearing
        // is what proves the workflow is genuinely ready for the next batch.
        waitVisible(APP_NUMBERS_INSTRUCTION);
        Log.pass("Instruction prompt reappeared - ready for next input");
    }

    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, DOWNLOADER_NAME);

        waitVisible(WORKFLOW_EXITED_MESSAGE);
        Log.pass("Workflow exited - chat returned to its default prompt");
    }

    public Path clickDownloadAndAssertTxt() {
        Log.info("─── Validating download ───");

        assertRequestCompleted();

        String previousHref = lastDownloadHref;
        WebElement link = waitForNewDownloadLink(previousHref);
        String href = hrefOf(link);
        lastDownloadHref = href;

        Log.info("New download link ready: {}", stripQueryString(href));
        logDownloadLinkCandidates();

        // Watermark BEFORE the click: anything on disk older than this is not ours.
        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(link);
        Log.info("Clicked the download link");

        return awaitAndValidateDownloadedFile(beforeClick);
    }

    public void assertZipContents(Path zipPath, String... appNumbers) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            List<String> entryNames = ZipContentValidator.collectEntryNames(zip);

            Log.info("─── Zip contents: {} ({} entries) ───", zipPath.getFileName(), entryNames.size());
            Log.debug("Full manifest:");
            entryNames.forEach(entry -> Log.debug("    {}", entry));

            ZipContentValidator.assertOnlyPdfFiles(entryNames);

            for (String appNum : appNumbers) {
                ZipContentValidator.AppFormCounts counts = ZipContentValidator.validateApplicationFolder(zip, entryNames, appNum);
                usptoCrossChecker.assertCountsMatch(appNum, counts.count1449, counts.count892);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read zip for content validation: " + zipPath, e);
        }
    }

    private void assertRequestCompleted() {
        Assert.assertTrue(waitVisible(COMPLETION_MESSAGE).isDisplayed(),
                "Completion message ('...has been completed') never appeared");
        Assert.assertTrue(waitVisible(REQUEST_ID_MESSAGE).isDisplayed(),
                "Request ID message never appeared");

        // The send/stop icons swap in the same slot, so "stop is gone" == "generation done".
        // Waiting (rather than checking instantaneously) protects against the UI rendering
        // the completion text a beat before it swaps the icon back.
        boolean stillGenerating = !new WebDriverWait(driver, UI_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(STOP_BUTTON));
        Assert.assertFalse(stillGenerating,
                "Stop button is still present - the request has not finished generating");

        Log.pass("Request completed (completion message, request ID, generation finished)");
    }

    private WebElement waitForNewDownloadLink(String previousHref) {
        long start = System.currentTimeMillis();
        try {
            WebElement link = new WebDriverWait(driver, BACKEND_TIMEOUT).until(newDownloadLinkAppears(previousHref));
            Log.info("Backend produced a new zip in {} ms", System.currentTimeMillis() - start);
            return link;
        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW download link. Still showing the previous one: "
                            + stripQueryString(previousHref), e);
        }
    }

    private static ExpectedCondition<WebElement> newDownloadLinkAppears(String previousHref) {
        return new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(WebDriver d) {
                List<WebElement> links = d.findElements(ALL_DOWNLOAD_LINKS);
                if (links.isEmpty()) {
                    return null;
                }
                WebElement newest = links.get(links.size() - 1);
                String candidate = hrefOf(newest);
                return (candidate != null && !candidate.equals(previousHref)) ? newest : null;
            }

            @Override
            public String toString() {
                return "a 1449 .zip download link with an href different from "
                        + stripQueryString(previousHref);
            }
        };
    }

    private void logDownloadLinkCandidates() {
        List<WebElement> links = driver.findElements(ALL_DOWNLOAD_LINKS);
        Log.debug("Download links currently in DOM: {}", links.size());
        for (WebElement el : links) {
            Log.debug("    candidate: {}", stripQueryString(hrefOf(el)));
        }
    }

    private Path awaitAndValidateDownloadedFile(Instant beforeClick) {
        try {
            Path downloadDir = OutputFileWorkflowManager.resolveIncomingDownloadDirectory(ConfigReader.loadConfig());

            Path file = DownloadFileHelper.waitForNewFile(downloadDir, FORM_1449, DOWNLOAD_TIMEOUT_SECONDS, DOWNLOAD_POLL_MILLIS, beforeClick);

            Assert.assertNotNull(file, "Download did not complete: no file containing '" + FORM_1449 + "' modified after " + beforeClick + " appeared in " + downloadDir + " within " + DOWNLOAD_TIMEOUT_SECONDS + "s");

            ZipContentValidator.assertValidNonEmptyZip(file);
            Log.pass("Downloaded {} ({} KB)", file.getFileName(), DownloadFileHelper.sizeInKb(file));
            return file;

        } catch (InterruptedException e) {
            // Catching InterruptedException CLEARS the interrupt flag - restore it so
            // whoever is shutting this thread down upstream still sees the cancellation.
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the download", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for the downloaded file", e);
        }
    }

    private static String hrefOf(WebElement element) {
        return element.getAttribute("href");
    }

    private static String stripQueryString(String url) {
        if (url == null) {
            return null;
        }
        int idx = url.indexOf('?');
        return idx == -1 ? url : url.substring(0, idx);
    }
}
