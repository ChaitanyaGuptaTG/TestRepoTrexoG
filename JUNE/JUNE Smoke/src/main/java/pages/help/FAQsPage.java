package pages.help;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import pages.BasePage;
import pages.help.data.*;

public class FAQsPage extends BasePage {

    // Fresh instance per verifyAll() run so a broken subcategory (e.g. content
    // reverted to "Coming Soon") is recorded as a failure but doesn't stop the
    // rest of the page from being checked. Collected failures are all reported
    // together at the end via assertAll().
    private SoftAssert softAssert = new SoftAssert();

    private final By faqsTitle = By.xpath("//h1[normalize-space()='FAQs']");
    private final By faqBackButton = By.xpath("//h1[normalize-space()='FAQs']//button");
    private final By comingSoonText = By.xpath(
            "//*[contains(text(),'This will be available shortly')]");

    // Sidebar categories
    private final By gettingStarted = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Getting Started')]");
    private final By bibliographicDataExtraction = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Bibliographic Data Extraction')]");
    private final By documentGeneration = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Document Generation')]");
    private final By ids = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'IDS')]");
    private final By patentFileWrapperDownloader = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Patent File Wrapper Downloader')]");
    private final By appGen = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'AppGen')]");
    private final By oathDecAdsDownloader = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Oath/Dec & ADS Downloader')]");
    private final By oaShellDraft = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'OA Shell Draft')]");
    private final By claimsFormatter = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Claims Formatter')]");
    private final By supportHelp = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Support')]");

    // Patent File Wrapper Downloader and Claims Formatter both graduated out of Beta -
    // no badges expected for either anymore.

    // Bibliographic Data Extraction subcategories
    private final By usTrademarkSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'US Trademark')]");
    private final By usPatentSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'US Patent')]");
    private final By auTrademarkSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'AU Trademark')]");
    private final By epPatentSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'EP Patent')]");
    private final By auPatentSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'AU Patent')]");
    private final By usTmImageDownloadSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'US TM Image Download')]");
    private final By globalPatentSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Global Patent')]");

    // Document Generation subcategories
    private final By changeCorrespondenceAppSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Change of correspondence address (Application)')]");
    private final By rceTransmittalSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'RCE Transmittal')]"
                    + "[not(contains(text(),'EFS'))]");
    private final By poaRevocationSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'PoA/Revocation')]");
    private final By attorneyWithdrawalSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Attorney Withdrawal')]");
    private final By poaGpoaSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'POA / GPOA')]");
    private final By changeCorrespondencePatentSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Change of correspondence address (Patent)')]");
    private final By correctApplicantNameSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Correct or Update Applicant Name')]");
    private final By rceTransmittalEfsSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'RCE Transmittal (EFS)')]");
    private final By cfrSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'37 CFR or 3.73')]");

    // IDS subcategories
    private final By downloader1449892Sub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'1449 and 892 Downloader')]");
    private final By correspondingRefCheckSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Corresponding RefCheck')]");
    private final By sb08GeneratorSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'SB-08 Generator')]");
    private final By removeEmbeddedFontsSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Remove Embedded Fonts')]");
    private final By referenceExtractorSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Reference Extractor')]");
    private final By referenceDownloaderSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Reference Downloader')]");
    private final By referenceCountSub = By.xpath(
            "//div[contains(@class,'MuiBox-root')][contains(text(),'Reference Count')]");


    public FAQsPage(WebDriver driver) {
        super(driver);
    }

    public void verifyAll() {
        softAssert = new SoftAssert();

        Assert.assertTrue(waitVisible(faqsTitle).isDisplayed(), "FAQs title is not visible");
        System.out.println("FAQs title is displayed");

        verifySidebarCategories();
        verifyGettingStartedFAQs();
        verifyBibliographicFAQs();
        verifyDocumentGenerationFAQs();
        verifyIDSFAQs();
        verifyPatentFileWrapperDownloaderFAQs();
        verifyAppGenFAQs();
        verifyOathDecAdsFAQs();
        verifyOAShellDraftFAQs();
        verifyClaimsFormatterFAQs();
        verifySupportHelpFAQs();

        scrollToElement(faqBackButton);
        waitVisible(faqBackButton).click();
        System.out.println("Clicked the back button");

        softAssert.assertAll();
    }

    private void verifySidebarCategories() {
        System.out.println("\n--- Sidebar Categories ---");
        By[] categories = {
                gettingStarted, bibliographicDataExtraction, documentGeneration,
                ids, patentFileWrapperDownloader, appGen,
                oathDecAdsDownloader, oaShellDraft, claimsFormatter, supportHelp
        };
        String[] categoryNames = {
                "Getting Started", "Bibliographic Data Extraction", "Document Generation",
                "IDS", "Patent File Wrapper Downloader", "AppGen",
                "Oath/Dec & ADS Downloader", "OA Shell Draft", "Claims Formatter", "Support & Help"
        };
        verifySubcategories(categories, categoryNames);
        System.out.println("  All " + categories.length + " sidebar categories verified");
    }

    private void verifyGettingStartedFAQs() {
        System.out.println("\n--- Getting Started FAQs ---");
        FAQEntry[] faqs = GettingStartedFAQData.FAQS;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "Getting Started");
        }
        System.out.println("\n  All " + faqs.length + " Getting Started FAQs verified");
    }

    private void verifyBibliographicFAQs() {
        System.out.println("\n=== Bibliographic Data Extraction FAQs ===");
        expandDropdown(bibliographicDataExtraction);
        System.out.println("Clicked Bibliographic Data Extraction dropdown");

        By[] subcategories = {
                usTrademarkSub, usPatentSub, auTrademarkSub,
                epPatentSub, auPatentSub, usTmImageDownloadSub, globalPatentSub
        };
        String[] subcategoryNames = {
                "US Trademark", "US Patent", "AU Trademark",
                "EP Patent", "AU Patent", "US TM Image Download", "Global Patent"
        };
        verifySubcategories(subcategories, subcategoryNames);
        System.out.println("  All " + subcategories.length + " subcategories verified");

        runSubcategoryFAQs(usTrademarkSub, "US Trademark", BibliographicFAQData.US_TRADEMARK);
        runSubcategoryFAQs(usPatentSub, "US Patent", BibliographicFAQData.US_PATENT);
        runSubcategoryFAQs(auTrademarkSub, "AU Trademark", BibliographicFAQData.AU_TRADEMARK);
        runSubcategoryFAQs(epPatentSub, "EP Patent", BibliographicFAQData.EP_PATENT);
        runSubcategoryFAQs(auPatentSub, "AU Patent", BibliographicFAQData.AU_PATENT);
        runSubcategoryFAQs(usTmImageDownloadSub, "US TM Image Download", BibliographicFAQData.US_TM_IMAGE_DOWNLOAD);
        runSubcategoryFAQs(globalPatentSub, "Global Patent", BibliographicFAQData.GLOBAL_PATENT);
    }

    private void verifyDocumentGenerationFAQs() {
        System.out.println("\n=== Document Generation FAQs ===");
        expandDropdown(documentGeneration);
        System.out.println("Clicked Document Generation dropdown");

        By[] subcategories = {
                changeCorrespondenceAppSub, rceTransmittalSub, poaRevocationSub,
                attorneyWithdrawalSub, poaGpoaSub, changeCorrespondencePatentSub,
                correctApplicantNameSub, rceTransmittalEfsSub, cfrSub
        };
        String[] subcategoryNames = {
                "Change of correspondence address (Application)", "RCE Transmittal",
                "PoA/Revocation of PoA/Correspondence Add Change",
                "Attorney Withdrawal & Address Change Request", "POA / GPOA",
                "Change of correspondence address (Patent)",
                "Correct or Update Applicant Name(1.46)", "RCE Transmittal (EFS)",
                "37 CFR or 3.73(c)"
        };
        verifySubcategories(subcategories, subcategoryNames);
        System.out.println("  All " + subcategories.length + " subcategories verified");

        // Subcategories with FAQs
        runSubcategoryFAQs(changeCorrespondenceAppSub,
                "Change of correspondence address (Application)", DocumentGenerationFAQData.CHANGE_CORR_APP);
        runSubcategoryFAQs(rceTransmittalSub, "RCE Transmittal", DocumentGenerationFAQData.RCE_TRANSMITTAL);

        // Coming Soon
        runComingSoon(poaRevocationSub, "PoA/Revocation of PoA/Correspondence Add Change");
        runComingSoon(attorneyWithdrawalSub, "Attorney Withdrawal & Address Change Request");

        // Subcategories with FAQs
        runSubcategoryFAQs(poaGpoaSub, "POA / GPOA", DocumentGenerationFAQData.POA_GPOA);
        runSubcategoryFAQs(changeCorrespondencePatentSub,
                "Change of correspondence address (Patent)", DocumentGenerationFAQData.CHANGE_CORR_PATENT);

        // Coming Soon
        runComingSoon(correctApplicantNameSub, "Correct or Update Applicant Name(1.46)");

        // Subcategories with FAQs
        runSubcategoryFAQs(rceTransmittalEfsSub, "RCE Transmittal (EFS)", DocumentGenerationFAQData.RCE_EFS);

        // 37 CFR FAQ  available
        runSubcategoryFAQs(cfrSub, "37 CFR or 3.73(c)", DocumentGenerationFAQData.CFR_37);
    }

    private void verifyIDSFAQs() {
        System.out.println("\n=== IDS FAQs ===");
        expandDropdown(ids);

        verifySubcategories(
                new By[]{downloader1449892Sub, /* correspondingRefCheckSub, */ sb08GeneratorSub,
                        removeEmbeddedFontsSub, referenceExtractorSub,
                        referenceDownloaderSub, referenceCountSub},
                new String[]{"1449 and 892 Downloader", /* "Corresponding RefCheck", */
                        "SB-08 Generator", "Remove Embedded Fonts", "Reference Extractor",
                        "Reference Downloader", "Reference Count"}
        );

        runSubcategoryFAQs(downloader1449892Sub, "1449 and 892 Downloader",
                IDSFAQData.DOWNLOADER_1449_892);
//        runComingSoon(correspondingRefCheckSub, "Corresponding RefCheck"); // in prod this is not deployed!
        runSubcategoryFAQs(sb08GeneratorSub, "SB-08 Generator",
                IDSFAQData.SB08_GENERATOR);
        runSubcategoryFAQs(removeEmbeddedFontsSub, "Remove Embedded Fonts",
                IDSFAQData.REMOVE_EMBEDDED_FONTS);
        runSubcategoryFAQs(referenceExtractorSub, "Reference Extractor",
                IDSFAQData.REFERENCE_EXTRACTOR);
        runSubcategoryFAQs(referenceDownloaderSub, "Reference Downloader",
                IDSFAQData.REFERENCE_DOWNLOADER);
        runSubcategoryFAQs(referenceCountSub, "Reference Count", IDSFAQData.REFERENCE_COUNT);

    }

    private void verifyPatentFileWrapperDownloaderFAQs() {
        System.out.println("\n=== Patent File Wrapper Downloader FAQs ===");
        expandDropdown(patentFileWrapperDownloader);
        System.out.println("Clicked Patent File Wrapper Downloader");

        FAQEntry[] faqs = PatentFileWrapperDownloaderData.PATENT_FILE_WRAPPER_DOWNLOADER;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "Patent File Wrapper Downloader");
        }
        System.out.println("\n  All " + faqs.length + " Patent File Wrapper Downloader FAQs verified");
    }

    private void verifyAppGenFAQs() {
        System.out.println("\n=== AppGen FAQs ===");
        expandDropdown(appGen);
        System.out.println("Clicked AppGen");

        FAQEntry[] faqs = AppGenFAQData.APP_GEN;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "AppGen");
        }
        System.out.println("\n  All " + faqs.length + " AppGen FAQs verified");
    }

    private void verifyOathDecAdsFAQs() {
        System.out.println("\n=== Oath/Dec & ADS Downloader FAQs ===");
        expandDropdown(oathDecAdsDownloader);
        System.out.println("Clicked Oath/Dec & ADS Downloader");

        FAQEntry[] faqs = OathDecAdsFAQData.OATH_DEC_ADS_DOWNLOADER;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "Oath/Dec & ADS Downloader");
        }
        System.out.println("\n  All " + faqs.length + " Oath/Dec & ADS Downloader FAQs verified");
    }

    private void verifyOAShellDraftFAQs() {
        System.out.println("\n=== OA Shell Draft FAQs ===");
        expandDropdown(oaShellDraft);
        System.out.println("Clicked OA Shell Draft");

        FAQEntry[] faqs = OAShellDraftFAQData.OA_SHELL_DRAFT;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "OA Shell Draft");
        }
        System.out.println("\n  All " + faqs.length + " OA Shell Draft FAQs verified");
    }

    private void verifyClaimsFormatterFAQs() {
        System.out.println("\n=== Claims Formatter FAQs ===");
        expandDropdown(claimsFormatter);
        System.out.println("Clicked Claims Formatter");

        FAQEntry[] faqs = ClaimsFormatterFAQData.CLAIMS_FORMATTER;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "Claims Formatter");
        }
        System.out.println("\n  All " + faqs.length + " Claims Formatter FAQs verified");
    }

    private void verifySupportHelpFAQs() {
        System.out.println("\n=== Support & Help FAQs ===");
        expandDropdown(supportHelp);
        System.out.println("Clicked Support & Help");

        FAQEntry[] faqs = SupportHelpFAQData.SUPPORT_HELP;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordionSoft(faqs[i], "Support & Help");
        }
        System.out.println("\n  All " + faqs.length + " Support & Help FAQs verified");
    }

    private void runSubcategoryFAQs(By subcategoryLocator, String subcategoryName, FAQEntry[] faqs) {
        System.out.println("\n  --- " + subcategoryName + " FAQs ---");
        try {
            expandDropdown(subcategoryLocator);
            System.out.println("  Clicked " + subcategoryName);

            for (int i = 0; i < faqs.length; i++) {
                verifyAccordion(faqs[i],
                        "faq_" + subcategoryName.replace(" ", "_").replace("/", "_") + "_q" + (i + 1));
            }
            System.out.println("\n  All " + faqs.length + " " + subcategoryName + " FAQs verified");
        } catch (Throwable t) {
            System.out.println("  ERROR in " + subcategoryName + " FAQs: " + t.getMessage());
            softAssert.fail(subcategoryName + " FAQs failed: " + t.getMessage());
        }
    }

    private void runComingSoon(By subcategoryLocator, String subcategoryName) {
        System.out.println("\n  --- " + subcategoryName + " ---");
        try {
            expandDropdown(subcategoryLocator);
            Assert.assertTrue(waitVisible(comingSoonText).isDisplayed(),
                    "Coming Soon text not visible for " + subcategoryName);
            System.out.println("  Coming Soon page verified");
        } catch (Throwable t) {
            System.out.println("  ERROR verifying " + subcategoryName + ": " + t.getMessage());
            softAssert.fail(subcategoryName + " verification failed: " + t.getMessage());
        }
    }

    private void verifyAccordionSoft(FAQEntry entry, String categoryName) {
        try {
            verifyAccordion(entry, null);
        } catch (Throwable t) {
            System.out.println("  ERROR verifying '" + entry.getQuestion() + "': " + t.getMessage());
            softAssert.fail(categoryName + " FAQ failed for '" + entry.getQuestion() + "': " + t.getMessage());
        }
    }

    private void verifyAccordion(FAQEntry entry, String screenshotName) {
        By questionLocator = entry.getLocator();
        // Short timeout for this lookup only: a rendered accordion appears within ~1s
        // of the dropdown expand, so if it's still missing after 5s (e.g. the subcategory
        // reverted to "Coming Soon"), waiting the full 30s here just wastes minutes across
        // a broken subcategory's whole question list without changing the outcome.
        WebElement questionPresent = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.presenceOfElementLocated(questionLocator));
        scrollIntoView(questionPresent);
        WebElement questionElement = waitVisible(questionLocator);
        System.out.println("\n    " + entry.getQuestion());

        String expanded = questionElement.getAttribute("aria-expanded");
        if (!"true".equals(expanded)) {
            questionElement.click();
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
        }

        questionElement = driver.findElement(questionLocator);
        Assert.assertEquals(questionElement.getAttribute("aria-expanded"), "true",
                "Accordion did not expand for: " + entry.getQuestion());
        System.out.println("    Accordion expanded");

        WebElement accordionRoot = questionElement.findElement(
                By.xpath("ancestor::div[contains(@class,'MuiAccordion-root')]"));
        WebElement answerArea = accordionRoot.findElement(
                By.cssSelector("div.MuiAccordionDetails-root"));
        String answerText = answerArea.getText().replace('\u00A0', ' ').trim();

        Assert.assertTrue(answerText.length() > 10,
                "Answer too short for: " + entry.getQuestion() + " (length: " + answerText.length() + ")");
        System.out.println("    A: " + answerText.substring(0,
                Math.min(80, answerText.length())) + "...");

        String normalizedAnswerText = normalizeWhitespace(normalizeQuotes(answerText));
        String normalizedExpectedAnswer = normalizeWhitespace(normalizeQuotes(entry.getExpectedAnswer()));
        Assert.assertTrue(normalizedAnswerText.contains(normalizedExpectedAnswer),
                "Expected answer missing for: " + entry.getQuestion()
                        + "\nExpected: " + entry.getExpectedAnswer()
                        + "\nActual: " + answerText);
        System.out.println("    Answer content verified");
    }

    private String normalizeQuotes(String text) {
        return text
                .replace('‘', '\'')
                .replace('’', '\'')
                .replace('“', '"')
                .replace('”', '"');
    }

    private String normalizeWhitespace(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private void verifySubcategories(By[] locators, String[] names) {
        for (int i = 0; i < locators.length; i++) {
            try {
                scrollToElement(locators[i]);
                Assert.assertTrue(waitVisible(locators[i]).isDisplayed(), names[i] + " subcategory is not visible");
                System.out.println("  Subcategory verified: " + names[i]);
            } catch (Throwable t) {
                System.out.println("  ERROR verifying subcategory '" + names[i] + "': " + t.getMessage());
                softAssert.fail(names[i] + " subcategory check failed: " + t.getMessage());
            }
        }
    }

    private void expandDropdown(By categoryLocator) {
        scrollToElement(categoryLocator);
        waitVisible(categoryLocator).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    private void scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
    }
}