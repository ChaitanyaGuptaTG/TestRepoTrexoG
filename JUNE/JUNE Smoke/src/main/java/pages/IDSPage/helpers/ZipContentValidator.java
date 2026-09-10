package pages.IDSPage.helpers;

import org.testng.Assert;
import utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ZipContentValidator {

    private static final String FORM_1449 = "1449";
    private static final String FORM_892 = "892";
    private static final String PDF_SUFFIX = ".pdf";

    // Real 1449s/892s produced by this workflow are comfortably above these -
    // a file below threshold is a truncated/near-empty download, not a real form.
    private static final long MIN_SIZE_1449_BYTES = 10_000L;
    private static final long MIN_SIZE_892_BYTES = 5_000L;

    // Naming convention this workflow's zip entries follow: {appNum}_{formType}_{dd-MMM-yyyy}[_N].pdf
    private static final String FILENAME_PATTERN_SUFFIX = "_\\d{2}-[A-Z][a-z]{2}-\\d{4}(_\\d+)?\\.pdf";

    private ZipContentValidator() {
        // static utility - no instances
    }


    public static final class AppFormCounts {
        public final int count1449;
        public final int count892;

        private AppFormCounts(int count1449, int count892) {
            this.count1449 = count1449;
            this.count892 = count892;
        }
    }

    public static void assertValidNonEmptyZip(Path file) throws IOException {
        Assert.assertTrue(Files.size(file) > 0, "Downloaded file is empty: " + file);
        try (ZipFile zip = new ZipFile(file.toFile())) {
            Assert.assertTrue(zip.entries().hasMoreElements(), "Downloaded zip has no entries: " + file);
        }
    }

    public static List<String> collectEntryNames(ZipFile zip) {
        List<String> names = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            names.add(entries.nextElement().getName());
        }
        return names;
    }

    public static void assertOnlyPdfFiles(List<String> entryNames) {
        for (String entry : entryNames) {
            if (entry.endsWith("/")) {
                continue; // directory entry
            }
            Assert.assertTrue(entry.toLowerCase().endsWith(PDF_SUFFIX),
                    "Unexpected non-PDF file found in zip: " + entry);
        }
    }

    public static AppFormCounts validateApplicationFolder(ZipFile zip, List<String> entryNames, String appNum) throws IOException {
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

        return new AppFormCounts(pdfs1449.size(), pdfs892.size());
    }

    private static void verifyPdfEntry(ZipFile zip, String pdfEntry, String appNum, String formType, long minSizeBytes) throws IOException {
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

    private static void assertPdfMagicHeader(ZipFile zip, ZipEntry entry, String label) throws IOException {
        try (InputStream is = zip.getInputStream(entry)) {
            byte[] header = new byte[5];
            int read = is.read(header);
            Assert.assertEquals(read, 5, "Could not read 5-byte header from " + label);
            String magic = new String(header, StandardCharsets.US_ASCII);
            Assert.assertEquals(magic, "%PDF-",
                    "File does not start with %PDF- header (got '" + magic + "'): " + label);
        }
    }

    private static List<String> entriesUnderFolder(List<String> entryNames, String appNum) {
        return entryNames.stream().filter(e -> e.startsWith(appNum + "/")).collect(Collectors.toList());
    }

    private static List<String> pdfsInSubfolder(List<String> appEntries, String subfolder) {
        String marker = "/" + subfolder + "/";
        return appEntries.stream().filter(e -> e.contains(marker) && e.toLowerCase().endsWith(PDF_SUFFIX)).collect(Collectors.toList());
    }
}
