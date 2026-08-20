package pages.IDSPage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import utils.ConfigReader;
import utils.OutputFileWorkflowManager;
import org.openqa.selenium.Platform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class idsMenuPage extends BasePage {

    // ───────────────────────── Locators ─────────────────────────

    private final By idsDropdown = By.xpath("//div[@role='combobox' and .//span[text()='IDS']]");
    private final By idsDropdownSpan = By.xpath("//span[text()='IDS']");
    private final By idsDocumentDownloaderOption = By.xpath("//li[@role='option' and contains(.,'1449 and 892 Downloader')]");
    private final By idsDocumentDownloaderLabel = By.xpath("//span[normalize-space()='1449 and 892 Downloader']");
    private final By applicationNumbersInstruction = By.xpath("(//span[contains(text(),'Please enter the application numbers')])[last()]");
    private final By queryInput = By.xpath("//*[@placeholder='Enter your query or select a task to get started']");
    private final By submitButton = By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']");
    private final By stopButton = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");

    private final By cancelConfirmationText = By.xpath("//h5[text()='Are you sure you want to cancel the request?']");
    private final By cancelNoButton = By.xpath("//h5[text()='Are you sure you want to cancel the request?']/ancestor::div//button[normalize-space()='No']");
    private final By cancelYesButton = By.xpath("//h5[text()='Are you sure you want to cancel the request?']/ancestor::div//button[normalize-space()='Yes']");

    private final By completionMessage = By.xpath("(//p[contains(text(),'Your task with Request ID') and contains(text(),'has been completed')])[last()]");
    private final By downloadLink = By.xpath("(//a[@download and contains(@href, '1449') and contains(@href, '.zip')])[last()]");
    private final By requestIdMessage = By.xpath("(//p[contains(text(),'Request ID')])[last()]");

    private final By continueConfirmationText = By.xpath("(//span[contains(text(),'Would you like to continue with 1449 and 892 Downloader')])[last()]");
    private final By continueYesButton = By.xpath("(//span[contains(text(),'Would you like to continue')]/ancestor::div//button[normalize-space()='Yes'])[last()]");
    private final By continueNoButton = By.xpath("(//span[contains(text(),'Would you like to continue')]/ancestor::div//button[normalize-space()='No'])[last()]");

    private final By queryInputBox = By.cssSelector("textarea[placeholder='Enter your query or select a task to get started']");

    private final By workflowExitedMessage = By.xpath("(//*[contains(text(),'Enter your query or select a task to get started')])[last()]");
    private final By trackTaskButton = By.xpath("//span[text()='Track Task']/ancestor::button");
    private final By attachFileIcon = By.xpath("//*[name()='svg' and @data-testid='AttachFileIcon']");

    private String lastDownloadHref = null;

    // Standard USPTO form titles printed at the top of each document type.
    // These are large, clean, all-caps headings — the part of a scanned form
    // OCR reads most reliably, unlike the application number (a digit string,
    // which OCR frequently misreads, and which is already verified
    // deterministically via the zip folder/filename convention instead).
    private static final String EXPECTED_TITLE_1449 = "INFORMATION DISCLOSURE STATEMENT";
    private static final String EXPECTED_TITLE_892 = "NOTICE OF REFERENCES CITED";

    public idsMenuPage(WebDriver driver) {
        super(driver);
    }

    // ───────────────────────── IDS dropdown actions ─────────────────────────

    public void clickIdsDropdown() {
        safeClick(idsDropdown);
        System.out.println("Clicked the IDS dropdown");
    }

    public void verifyIdsDropdownDisabledAfterSelection() {
        safeClick(idsDropdown);
        boolean optionReappeared;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(idsDocumentDownloaderOption));
            optionReappeared = true;
        } catch (TimeoutException e) {
            optionReappeared = false;
        }
        Assert.assertFalse(optionReappeared, "IDS dropdown should stay disabled and not reopen once a downloader has already been selected");
        System.out.println("Confirmed the IDS dropdown does not reopen after a downloader is selected");
    }

    public void selectDocumentDownloader() {
        waitVisible(idsDocumentDownloaderOption).click();
        System.out.println("Selected 1449 and 892 Downloader option");
    }

    public void verifyDocumentDownloaderLabel() {
        waitVisible(idsDocumentDownloaderLabel).isDisplayed();
        System.out.println("1449 and 892 Downloader label is displayed");
    }

    public void verifyApplicationNumbersInstruction() {
        waitVisible(applicationNumbersInstruction).isDisplayed();
        System.out.println("Application numbers instruction is displayed");
    }

    public void enterQuery(String query) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        var element = wait.until(ExpectedConditions.elementToBeClickable(queryInput));

        element.click(); // focus

        Keys cmdCtrl = Platform.getCurrent().is(Platform.MAC) ? Keys.COMMAND : Keys.CONTROL;
        element.sendKeys(Keys.chord(cmdCtrl, "a"), Keys.BACK_SPACE);

        String[] lines = query.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            element.sendKeys(lines[i]);
            // Add Shift+Enter for every line except the very last one
            if (i < lines.length - 1) {
                element.sendKeys(Keys.chord(Keys.SHIFT, Keys.ENTER));
            }
        }

        System.out.println("Entered query: " + query);
    }

    public void enterIntentAsQuery(String intentText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        var element = wait.until(ExpectedConditions.elementToBeClickable(queryInputBox));
        element.click();
        element.clear();
        element.sendKeys(intentText);
        System.out.println("Entered intent as query: " + intentText);
    }

    public void clickSubmitButton() {
        safeClick(submitButton);
        System.out.println("Clicked the Submit button");
    }

    public void clickContinueYes() {
        waitVisible(continueConfirmationText);
        safeClick(continueYesButton);
        System.out.println("Clicked YES to continue with 1449 and 892 Downloader");

        // Wait for the UI to settle: the instruction prompt reappears once the
        // downloader is ready for the next batch of application numbers.
        waitVisible(applicationNumbersInstruction);
        System.out.println("Instruction prompt reappeared — ready for next input");
    }

    public void clickContinueNo() {
        waitVisible(continueConfirmationText);
        safeClick(continueNoButton);
        System.out.println("Clicked NO — exiting 1449 and 892 Downloader workflow");

        waitVisible(workflowExitedMessage);
        System.out.println("Confirmed workflow exited — 'Enter your query or select a task to get started' message shown");
    }

    public Path clickDownloadAndAssertTxt() {
        Assert.assertTrue(waitVisible(completionMessage).isDisplayed(), "completionMessage not visible");
        Assert.assertTrue(waitVisible(requestIdMessage).isDisplayed(), "requestIdMessage not visible");
        Assert.assertTrue(driver.findElements(stopButton).isEmpty(),
                "stopButton should not still be present once the request has completed");

        String previousHref = lastDownloadHref;
        By allDownloadLinksLocator = By.xpath("//a[@download and contains(@href, '1449') and contains(@href, '.zip')]");
        String href;
        try {
            href = new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
                List<org.openqa.selenium.WebElement> links = d.findElements(allDownloadLinksLocator);
                if (links.isEmpty()) return null;
                String candidate = links.get(links.size() - 1).getAttribute("href");
                return (candidate != null && !candidate.equals(previousHref)) ? candidate : null;
            });
        } catch (TimeoutException e) {
            throw new TimeoutException("Timed out waiting for a new downloadLink href to appear "
                    + "(still showing the previous one: " + previousHref + ")", e);
        }
        lastDownloadHref = href;

        Assert.assertTrue(href.contains("1449") && href.contains(".zip"), "downloadLink href does not match the expected 1449/.zip pattern: " + href);
        System.out.println("downloadLink object key: " + stripQueryString(href));
        List<org.openqa.selenium.WebElement> allDownloadLinks = driver.findElements(allDownloadLinksLocator);
        System.out.println("Matching download links currently in DOM: " + allDownloadLinks.size());
        for (org.openqa.selenium.WebElement el : allDownloadLinks) {
            System.out.println("  candidate object key: " + stripQueryString(el.getAttribute("href")));
        }

        Instant beforeClick = Instant.now();
        safeClick(downloadLink);
        System.out.println("Clicked the downloadLink");

        try {
            Path downloadDir = OutputFileWorkflowManager.resolveIncomingDownloadDirectory(ConfigReader.loadConfig());
            Path downloadedFile = waitForDownloadedFile(downloadDir, "1449", 30, beforeClick);
            Assert.assertNotNull(downloadedFile, "Download did not complete: no matching file modified after " + beforeClick + " appeared in " + downloadDir + " within 30s");
            assertValidNonEmptyZip(downloadedFile);
            System.out.println("Downloaded file: " + downloadedFile);
            return downloadedFile;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for download", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for downloaded file", e);
        }
    }

    public void assertZipContents(Path zipPath, String... appNumbers) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {

            List<String> allEntryNames = new ArrayList<>();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                allEntryNames.add(entries.nextElement().getName());
            }

            System.out.println("──── Zip entries (" + allEntryNames.size() + ") ────");
            allEntryNames.forEach(e -> System.out.println("  " + e));

            // 9) No unexpected file types — only directories and .pdf files
            for (String entry : allEntryNames) {
                if (entry.endsWith("/")) continue; // directory
                Assert.assertTrue(entry.toLowerCase().endsWith(".pdf"), "Unexpected non-PDF file found in zip: " + entry);
            }

            List<String[]> ocrSummaryRows = new ArrayList<>();

            for (String appNum : appNumbers) {

                // 1) Top-level folder for this application number
                List<String> appEntries = allEntryNames.stream().filter(e -> e.startsWith(appNum + "/")).collect(Collectors.toList());

                Assert.assertFalse(appEntries.isEmpty(), "Zip does not contain a folder for application " + appNum + ". Entries found: " + allEntryNames);

                // 2) 1449 subfolder with at least one PDF
                List<String> form1449 = appEntries.stream().filter(e -> e.contains("/1449/") && e.toLowerCase().endsWith(".pdf")).collect(Collectors.toList());

                Assert.assertFalse(form1449.isEmpty(), "No 1449 PDF found for application " + appNum + ". Entries under app folder: " + appEntries);

                // 3) 892 subfolder with at least one PDF
                List<String> form892 = appEntries.stream().filter(e -> e.contains("/892/") && e.toLowerCase().endsWith(".pdf")).collect(Collectors.toList());

                Assert.assertFalse(form892.isEmpty(), "No 892 PDF found for application " + appNum + ". Entries under app folder: " + appEntries);

                long minSize1449 = 10_000;  // real 1449s are 190 KB+
                long minSize892 = 5_000;    // real 892s are ~23 KB

                for (String pdfEntry : form1449) {
                    verifyPdfEntry(zip, pdfEntry, appNum, "1449", minSize1449, ocrSummaryRows);
                }

                for (String pdfEntry : form892) {
                    verifyPdfEntry(zip, pdfEntry, appNum, "892", minSize892, ocrSummaryRows);
                }

                System.out.println("✓ Application " + appNum + ": "
                        + form1449.size() + " × 1449 PDF(s), "
                        + form892.size() + " × 892 PDF(s)  — all headers valid");
            }

            printOcrSummaryTable(ocrSummaryRows);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read zip for content validation: " + zipPath, e);
        }
    }

    /**
     * Prints a table of the OCR title check across every PDF just verified, so
     * it's easy to see at a glance that the same expected title text matched
     * consistently across different application numbers.
     */
    private void printOcrSummaryTable(List<String[]> rows) {
        if (rows.isEmpty()) return;

        int appCol = Math.max(11, rows.stream().mapToInt(r -> r[0].length()).max().orElse(0));
        int formCol = Math.max(9, rows.stream().mapToInt(r -> r[1].length()).max().orElse(0));
        int titleCol = Math.max(14, rows.stream().mapToInt(r -> r[2].length()).max().orElse(0));

        String rowFormat = "  %-" + appCol + "s | %-" + formCol + "s | %-" + titleCol + "s | %s%n";

        System.out.println("──── OCR title verification summary (" + rows.size() + " PDF(s)) ────");
        System.out.printf(rowFormat, "App Number", "Form Type", "Expected Title", "Match");
        System.out.println("  " + "-".repeat(appCol + formCol + titleCol + 12));
        for (String[] row : rows) {
            System.out.printf(rowFormat, row[0], row[1], row[2], row[3]);
        }
    }

    private void verifyPdfEntry(ZipFile zip, String pdfEntry, String appNum, String formType, long minSize,
                                 List<String[]> ocrSummaryRows) throws IOException {
        System.out.println("──── Verifying " + pdfEntry + " ────");

        ZipEntry ze = zip.getEntry(pdfEntry);
        Assert.assertNotNull(ze, "ZipEntry disappeared for " + pdfEntry);

        // Non-zero size
        Assert.assertTrue(ze.getSize() > 0, formType + " PDF has zero size: " + pdfEntry);
        System.out.println("  ✓ Non-zero size: " + ze.getSize() + " bytes");

        // Naming convention: {appNum}_{formType}_{dd-MMM-yyyy}[_N].pdf
        String fileName = Path.of(pdfEntry).getFileName().toString();
        Assert.assertTrue(fileName.startsWith(appNum + "_" + formType + "_"),
                formType + " PDF filename does not follow naming convention: " + fileName);
        Assert.assertTrue(fileName.matches(
                        appNum + "_" + formType + "_\\d{2}-[A-Z][a-z]{2}-\\d{4}(_\\d+)?\\.pdf"),
                formType + " PDF filename date pattern invalid: " + fileName
                        + " (expected {appNum}_" + formType + "_{dd-MMM-yyyy}[_N].pdf)");
        System.out.println("  ✓ Filename matches naming convention: " + fileName);

        assertPdfMagicHeader(zip, ze, pdfEntry);

        Assert.assertTrue(ze.getSize() >= minSize,
                formType + " PDF is suspiciously small (" + ze.getSize() + " bytes): "
                        + pdfEntry + " (expected >= " + minSize + ")");
        System.out.println("  ✓ Size >= minimum threshold for " + formType
                + " (" + ze.getSize() + " >= " + minSize + " bytes)");

        assertPdfStructurallyValidAndNotBlank(zip, ze, pdfEntry);

        // Content-level check: confirm the form title actually printed on the
        // page matches the folder (1449 vs 892) it was filed under, independent
        // of the application number (see EXPECTED_TITLE_* comment for why).
        assertPdfFormTitleViaOcr(zip, ze, pdfEntry, formType, appNum, ocrSummaryRows);
    }

    private void assertPdfMagicHeader(ZipFile zip, ZipEntry ze, String label) throws IOException {
        try (java.io.InputStream is = zip.getInputStream(ze)) {
            byte[] header = new byte[5];
            int read = is.read(header);
            Assert.assertEquals(read, 5, "Could not read 5-byte header from " + label);
            String magic = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
            Assert.assertEquals(magic, "%PDF-",
                    "File does not start with %PDF- header (got '" + magic + "'): " + label);
        }
        System.out.println("  ✓ PDF magic header valid (%PDF-)");
    }

    private void assertPdfStructurallyValidAndNotBlank(ZipFile zip, ZipEntry ze, String label) throws IOException {
        try (java.io.InputStream is = zip.getInputStream(ze);
             PDDocument document = PDDocument.load(is)) {

            Assert.assertFalse(document.isEncrypted(), "PDF is unexpectedly encrypted: " + label);
            System.out.println("  ✓ Not encrypted");

            int pageCount = document.getNumberOfPages();
            Assert.assertTrue(pageCount > 0, "PDF has no pages: " + label);
            System.out.println("  ✓ PDFBox parsed successfully: " + pageCount + " page(s)");

            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, 72, ImageType.GRAY);
            int width = image.getWidth();
            int height = image.getHeight();

            long nonWhitePixels = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (image.getRaster().getSample(x, y, 0) < 250) {
                        nonWhitePixels++;
                    }
                }
            }
            double nonWhiteRatio = (double) nonWhitePixels / ((long) width * height);

            Assert.assertTrue(nonWhiteRatio > 0.001,
                    String.format("Page 1 of PDF appears blank (%.4f%% non-white pixels): %s",
                            nonWhiteRatio * 100, label));
            System.out.println(String.format("  ✓ Page 1 not blank (%.2f%% non-white pixels)", nonWhiteRatio * 100));
        }
    }

    /**
     * OCRs page 1 and asserts the standard USPTO form title for {@code formType}
     * ("1449" or "892") is present. Works for any application number, since it
     * only checks for the fixed title text, not the (OCR-unreliable) app number.
     * <p>
     * Uses a fuzzy/tolerant match (normalized text, small edit-distance
     * allowance) rather than an exact substring check, since OCR output on a
     * scanned document is never perfectly clean.
     */
    private void assertPdfFormTitleViaOcr(ZipFile zip, ZipEntry ze, String label, String formType,
                                           String appNum, List<String[]> ocrSummaryRows) throws IOException {
        String expectedTitle = formType.equals("1449") ? EXPECTED_TITLE_1449 : EXPECTED_TITLE_892;

        try (java.io.InputStream is = zip.getInputStream(ze);
             PDDocument document = PDDocument.load(is)) {

            BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, 300, ImageType.GRAY);
            String rawOcrText = runTesseractCli(image, label);

            String normalizedText = normalizeForOcrMatch(rawOcrText);
            String normalizedExpected = normalizeForOcrMatch(expectedTitle);

            boolean titleFound = containsFuzzy(normalizedText, normalizedExpected, 0.15);

            ocrSummaryRows.add(new String[] { appNum, formType, expectedTitle, titleFound ? "✓ matched" : "✗ NOT FOUND" });

            Assert.assertTrue(titleFound,
                    "OCR did not find expected " + formType + " form title (\"" + expectedTitle
                            + "\") on page 1 of " + label
                            + "\n  OCR text (normalized): " + normalizedText);
            System.out.println("  ✓ OCR confirms " + formType + " form title present: \"" + expectedTitle + "\"");
        }
    }

    /**
     * Runs OCR by shelling out to the {@code tesseract} CLI binary rather than
     * using tess4j's JNA bindings: on this environment those bindings failed to
     * load (a version mismatch between lept4j's expected native symbol set and
     * the system's installed liblept), while the CLI binary itself works fine.
     * Invoking it directly avoids that whole class of native-binding fragility.
     */
    private String runTesseractCli(BufferedImage image, String label) throws IOException {
        java.io.File tempImage = java.io.File.createTempFile("ocr-page-", ".png");
        try {
            javax.imageio.ImageIO.write(image, "png", tempImage);

            ProcessBuilder pb = new ProcessBuilder("tesseract", tempImage.getAbsolutePath(), "stdout", "--psm", "6");
            pb.redirectErrorStream(false);
            Process process = pb.start();

            String stdout = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            int exitCode;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while running tesseract for " + label, e);
            }

            if (exitCode != 0) {
                throw new RuntimeException("tesseract CLI exited with code " + exitCode + " for " + label + ": " + stderr);
            }
            return stdout;
        } finally {
            tempImage.delete();
        }
    }

    private static String normalizeForOcrMatch(String s) {
        return s.toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * True if {@code needle} appears in {@code haystack} either exactly, or
     * within {@code maxErrorRatio} character edit-distance in some window of
     * {@code haystack} the same length as {@code needle} — tolerates the kind
     * of noise OCR introduces (misread characters, minor spacing drift).
     */
    private static boolean containsFuzzy(String haystack, String needle, double maxErrorRatio) {
        if (needle.isEmpty()) return true;
        if (haystack.contains(needle)) return true;
        if (haystack.length() < needle.length()) return false;

        int windowLen = needle.length();
        int maxAllowedDistance = (int) Math.floor(windowLen * maxErrorRatio);

        for (int start = 0; start <= haystack.length() - windowLen; start++) {
            String window = haystack.substring(start, start + windowLen);
            if (levenshteinDistance(window, needle) <= maxAllowedDistance) {
                return true;
            }
        }
        return false;
    }

    private static int levenshteinDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }

    private static String stripQueryString(String url) {
        if (url == null) return null;
        int idx = url.indexOf('?');
        return idx == -1 ? url : url.substring(0, idx);
    }

    private void assertValidNonEmptyZip(Path file) throws IOException {
        Assert.assertTrue(Files.size(file) > 0, "Downloaded file is empty: " + file);
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Assert.assertTrue(zip.entries().hasMoreElements(), "Downloaded zip has no entries: " + file);
        }
    }

    private static List<String> concat(List<String> a, List<String> b) {
        List<String> combined = new ArrayList<>(a);
        combined.addAll(b);
        return combined;
    }
}