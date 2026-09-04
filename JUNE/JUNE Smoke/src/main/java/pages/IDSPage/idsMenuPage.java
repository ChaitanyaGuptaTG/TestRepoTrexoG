package pages.IDSPage;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import utils.ConfigReader;
import utils.Log;
import utils.OutputFileWorkflowManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import api.clients.UsptoDocumentClient;


public class idsMenuPage extends BasePage {


    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);


    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);


    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(30);


    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;


    private static final long DOWNLOAD_POLL_MILLIS = 1000L;


    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    private static final String FORM_1449 = "1449";
    private static final String FORM_892 = "892";
    private static final String PDF_SUFFIX = ".pdf";
    private static final String DOWNLOADER_NAME = "1449 and 892 Downloader";
    private static final String QUERY_PLACEHOLDER = "Enter your query or select a task to get started";

    // Real 1449s/892s produced by this workflow are comfortably above these -
    // a file below threshold is a truncated/near-empty download, not a real form.
    private static final long MIN_SIZE_1449_BYTES = 10_000L;
    private static final long MIN_SIZE_892_BYTES = 5_000L;

    // Naming convention this workflow's zip entries follow: {appNum}_{formType}_{dd-MMM-yyyy}[_N].pdf
    private static final String FILENAME_PATTERN_SUFFIX = "_\\d{2}-[A-Z][a-z]{2}-\\d{4}(_\\d+)?\\.pdf";


    private static final By IDS_DROPDOWN = By.xpath("//div[@role='combobox' and .//span[text()='IDS']]");

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

    // Built lazily (see usptoApi()) rather than in the constructor: only the 1449/892
    // Downloader's zip-vs-API cross-check needs this, so environments without
    // 'uspto.api.key' configured (e.g. uat/prod today) can still run the Reference
    // Count / Reference Downloader workflows, which never touch the USPTO API.
    private UsptoDocumentClient usptoApi;

    public idsMenuPage(WebDriver driver) {
        super(driver);
    }

    private UsptoDocumentClient usptoApi() {
        if (usptoApi == null) {
            usptoApi = UsptoDocumentClient.fromConfig();
        }
        return usptoApi;
    }

    public void clickIdsDropdown() {
        safeClick(IDS_DROPDOWN);
        Log.info("Opened the IDS dropdown");
    }

    public void selectDocumentDownloader() {
        waitVisible(DOWNLOADER_OPTION).click();
        Log.info("Selected '{}' from the IDS dropdown", DOWNLOADER_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        safeClick(IDS_DROPDOWN);
        boolean optionReappeared = isVisibleWithin(DOWNLOADER_OPTION, QUICK_PROBE_TIMEOUT);

        // Close the dropdown again so this check leaves the UI exactly as it found it -
        // callers should not have to account for a dropdown left open just because
        // this verification ran (a second, un-closed open would otherwise toggle the
        // dropdown shut on the next real clickIdsDropdown() call).
        closeDropdown();

        Assert.assertTrue(optionReappeared, "IDS dropdown should offer '" + DOWNLOADER_NAME + "' again once the downloader workflow has been continued/exited");
        Log.pass("IDS dropdown is available again after the downloader workflow");
    }

    private void closeDropdown() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
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
        Assert.assertNotNull(query, "Query to enter must not be null");

        WebElement input = focusAndClearQueryBox();

        // -1 keeps trailing empty strings, so intentional blank lines survive.
        String[] lines = query.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            input.sendKeys(lines[i]);
            if (i < lines.length - 1) {
                input.sendKeys(Keys.chord(Keys.SHIFT, Keys.ENTER));
            }
        }

        Log.info("Entered query ({} line(s)): {}", lines.length, query.replace("\n", " | "));
    }

    public void enterIntentAsQuery(String intentText) {
        focusAndClearQueryBox().sendKeys(intentText);
        Log.info("Entered intent as query: {}", intentText);
    }

    public void clickSubmitButton() {
        safeClick(SUBMIT_BUTTON);
        Log.info("Submitted the query");
    }

    public void clickContinueYes() {
        waitVisible(CONTINUE_CONFIRMATION);
        safeClick(CONTINUE_YES);
        Log.info("Clicked YES to continue with '{}'", DOWNLOADER_NAME);

        // Wait for the EFFECT, not just the click: the instruction prompt reappearing
        // is what proves the workflow is genuinely ready for the next batch.
        waitVisible(APP_NUMBERS_INSTRUCTION);
        Log.pass("Instruction prompt reappeared - ready for next input");
    }

    public void clickContinueNo() {
        waitVisible(CONTINUE_CONFIRMATION);
        safeClick(CONTINUE_NO);
        Log.info("Clicked NO - exiting the '{}' workflow", DOWNLOADER_NAME);

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
            List<String> entryNames = collectEntryNames(zip);

            Log.info("─── Zip contents: {} ({} entries) ───", zipPath.getFileName(), entryNames.size());
            Log.debug("Full manifest:");
            entryNames.forEach(entry -> Log.debug("    {}", entry));

            assertOnlyPdfFiles(entryNames);

            for (String appNum : appNumbers) {
                List<String> appEntries = entriesUnderFolder(entryNames, appNum);
                Assert.assertFalse(appEntries.isEmpty(),
                        "Zip does not contain a folder for application " + appNum
                                + ". Entries found: " + entryNames);

                List<String> pdfs1449 = pdfsInSubfolder(appEntries, FORM_1449);
                Assert.assertFalse(pdfs1449.isEmpty(),
                        "No 1449 PDF found for application " + appNum
                                + ". Entries under app folder: " + appEntries);
                for (String pdfEntry : pdfs1449) {
                    verifyPdfEntry(zip, pdfEntry, appNum, FORM_1449, MIN_SIZE_1449_BYTES);
                }

                List<String> pdfs892 = pdfsInSubfolder(appEntries, FORM_892);
                Assert.assertFalse(pdfs892.isEmpty(),
                        "No 892 PDF found for application " + appNum
                                + ". Entries under app folder: " + appEntries);
                for (String pdfEntry : pdfs892) {
                    verifyPdfEntry(zip, pdfEntry, appNum, FORM_892, MIN_SIZE_892_BYTES);
                }

                assertZipCountsMatchApi(appNum, pdfs1449.size(), pdfs892.size());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read zip for content validation: " + zipPath, e);
        }
    }

    /**
     * Structural checks only - no PDF parsing/rendering library or external OCR
     * binary required, so this runs on any machine/CI runner unmodified:
     * - starts with the %PDF- magic header (not corrupted/truncated)
     * - is not suspiciously small (a near-empty file masquerading as a real form)
     * - filename follows the workflow's naming convention
     */
    private void verifyPdfEntry(ZipFile zip, String pdfEntry, String appNum, String formType, long minSizeBytes) throws IOException {
        ZipEntry entry = zip.getEntry(pdfEntry);
        Assert.assertNotNull(entry, "Zip entry disappeared for " + pdfEntry);

        assertPdfMagicHeader(zip, entry, pdfEntry);

        Assert.assertTrue(entry.getSize() >= minSizeBytes,
                formType + " PDF is suspiciously small (" + entry.getSize() + " bytes): "
                        + pdfEntry + " (expected >= " + minSizeBytes + ")");

        String fileName = Path.of(pdfEntry).getFileName().toString();
        Assert.assertTrue(fileName.matches(appNum + "_" + formType + FILENAME_PATTERN_SUFFIX),
                formType + " PDF filename does not follow naming convention: " + fileName
                        + " (expected {appNum}_" + formType + "_{dd-MMM-yyyy}[_N].pdf)");

        Log.pass("Verified {} - header, size ({} bytes), and filename convention all valid", pdfEntry, entry.getSize());
    }

    private void assertPdfMagicHeader(ZipFile zip, ZipEntry entry, String label) throws IOException {
        try (InputStream is = zip.getInputStream(entry)) {
            byte[] header = new byte[5];
            int read = is.read(header);
            Assert.assertEquals(read, 5, "Could not read 5-byte header from " + label);
            String magic = new String(header, StandardCharsets.US_ASCII);
            Assert.assertEquals(magic, "%PDF-",
                    "File does not start with %PDF- header (got '" + magic + "'): " + label);
        }
    }

    /**
     * The zip and the API are two independent views of the same USPTO data - if the
     * counts diverge, either the download is incomplete/stale or the API is, so this
     * is treated as a hard failure rather than a warning.
     * <p>
     * Fetches both form counts in a single API call (see countDocumentsByType) and
     * logs one summary line per application instead of one per form code.
     */
    private void assertZipCountsMatchApi(String appNum, int zip1449Count, int zip892Count) {
        Map<String, Integer> apiCounts = usptoApi().countDocumentsByType(appNum, FORM_1449, FORM_892);
        int api1449Count = apiCounts.get(FORM_1449);
        int api892Count = apiCounts.get(FORM_892);

        Log.info("Application {}: number of {} in zip is {} and {} in zip is {}",
                appNum, FORM_1449, zip1449Count, FORM_892, zip892Count);
        Log.info("Application {}: number of {} in API is {} and {} in API is {}",
                appNum, FORM_1449, api1449Count, FORM_892, api892Count);

        Assert.assertEquals(zip1449Count, api1449Count,
                "Application " + appNum + ": " + FORM_1449 + " PDF count mismatch - zip contained "
                        + zip1449Count + " but API reports " + api1449Count);
        Assert.assertEquals(zip892Count, api892Count,
                "Application " + appNum + ": " + FORM_892 + " PDF count mismatch - zip contained "
                        + zip892Count + " but API reports " + api892Count);

        Log.pass("Application {}: zip and API counts match - {}: {}, {}: {}",
                appNum, FORM_1449, zip1449Count, FORM_892, zip892Count);
    }

    private WebElement focusAndClearQueryBox() {
        WebElement input = new WebDriverWait(driver, UI_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(QUERY_INPUT));
        input.click();
        input.sendKeys(Keys.chord(selectAllModifier(), "a"), Keys.BACK_SPACE);
        return input;
    }

    private Keys selectAllModifier() {
        Platform platform = (driver instanceof HasCapabilities)
                ? ((HasCapabilities) driver).getCapabilities().getPlatformName()
                : Platform.getCurrent();
        return (platform != null && platform.is(Platform.MAC)) ? Keys.COMMAND : Keys.CONTROL;
    }

    /**
     * True if the element becomes visible within the timeout; false instead of throwing.
     */
    private boolean isVisibleWithin(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
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

            Path file = waitForDownloadedFile(downloadDir, FORM_1449, DOWNLOAD_TIMEOUT_SECONDS, beforeClick);

            Assert.assertNotNull(file, "Download did not complete: no file containing '" + FORM_1449 + "' modified after " + beforeClick + " appeared in " + downloadDir + " within " + DOWNLOAD_TIMEOUT_SECONDS + "s");

            assertValidNonEmptyZip(file);
            Log.pass("Downloaded {} ({} KB)", file.getFileName(), sizeInKb(file));
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

    private Path waitForDownloadedFile(Path downloadDir, String filenameContains,
                                       int timeoutSeconds, Instant after)
            throws InterruptedException, IOException {

        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);

        while (Instant.now().isBefore(deadline)) {
            Path candidate;
            // Files.list holds an OPEN OS directory handle - it must be closed,
            // or a long-running suite exhausts its file descriptors.
            try (Stream<Path> files = Files.list(downloadDir)) {
                candidate = files
                        .filter(Files::isRegularFile)
                        .filter(idsMenuPage::isNotBrowserTempFile)
                        .filter(path -> matchesName(path, filenameContains))
                        .filter(path -> isModifiedAfter(path, after))
                        .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                        .orElse(null);
            }

            if (candidate != null) {
                return candidate;
            }
            Thread.sleep(DOWNLOAD_POLL_MILLIS);
        }
        return null;
    }

    private static boolean isNotBrowserTempFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return !name.endsWith(".crdownload")   // Chrome partial
                && !name.endsWith(".part")      // Firefox partial
                && !name.endsWith(".tmp")
                && !name.startsWith(".")        // .DS_Store etc.
                && !name.startsWith("~$")       // Office lock files
                && !name.contains("chrome")
                && !name.contains("google")
                && !name.contains("edge")
                && !name.contains("chromium");
    }

    private static boolean matchesName(Path path, String filenameContains) {
        return filenameContains == null
                || filenameContains.isBlank()
                || path.getFileName().toString().contains(filenameContains);
    }

    private static boolean isModifiedAfter(Path path, Instant after) {
        return Instant.ofEpochMilli(path.toFile().lastModified()).isAfter(after);
    }

    private void assertValidNonEmptyZip(Path file) throws IOException {
        Assert.assertTrue(Files.size(file) > 0, "Downloaded file is empty: " + file);
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Assert.assertTrue(zip.entries().hasMoreElements(),
                    "Downloaded zip has no entries: " + file);
        }
    }

    private List<String> collectEntryNames(ZipFile zip) {
        List<String> names = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            names.add(entries.nextElement().getName());
        }
        return names;
    }

    private static void assertOnlyPdfFiles(List<String> entryNames) {
        for (String entry : entryNames) {
            if (entry.endsWith("/")) {
                continue; // directory entry
            }
            Assert.assertTrue(entry.toLowerCase().endsWith(PDF_SUFFIX),
                    "Unexpected non-PDF file found in zip: " + entry);
        }
    }

    private static List<String> entriesUnderFolder(List<String> entryNames, String appNum) {
        return entryNames.stream().filter(e -> e.startsWith(appNum + "/")).collect(Collectors.toList());
    }

    private static List<String> pdfsInSubfolder(List<String> appEntries, String subfolder) {
        String marker = "/" + subfolder + "/";
        return appEntries.stream().filter(e -> e.contains(marker) && e.toLowerCase().endsWith(PDF_SUFFIX)).collect(Collectors.toList());
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

    private static long sizeInKb(Path file) {
        try {
            return Files.size(file) / 1024;
        } catch (IOException e) {
            return -1;
        }
    }
}