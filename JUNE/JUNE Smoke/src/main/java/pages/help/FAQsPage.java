package pages.help;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import pages.BasePage;
import pages.help.data.*;

public class FAQsPage extends BasePage {

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

    // Beta badges
    private final By patentDownloaderBeta = By.xpath(
            "//p[contains(normalize-space(), 'Patent File Wrapper Downloader')]/button[normalize-space()='Beta']");
    private final By claimsFormatterBeta = By.xpath(
            "//p[contains(normalize-space(), 'Claims Formatter')]/button[normalize-space()='Beta']");

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
        Assert.assertTrue(waitVisible(faqsTitle).isDisplayed(), "FAQs title is not visible");
        System.out.println("FAQs title is displayed");

        verifySidebarCategories();
        verifyBetaBadges();
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

    private void verifyBetaBadges() {
        System.out.println("\n--- Beta Badges ---");
        scrollToElement(patentDownloaderBeta);
        Assert.assertTrue(waitVisible(patentDownloaderBeta).isDisplayed(),
                "Patent File Wrapper Downloader Beta badge is not visible");
        System.out.println("  Patent File Wrapper Downloader Beta badge verified");
        scrollToElement(claimsFormatterBeta);
        Assert.assertTrue(waitVisible(claimsFormatterBeta).isDisplayed(),
                "Claims Formatter Beta badge is not visible");
        System.out.println("  Claims Formatter Beta badge verified");
    }

    private void verifyGettingStartedFAQs() {
        System.out.println("\n--- Getting Started FAQs ---");
        FAQEntry[] faqs = GettingStartedFAQData.FAQS;
        System.out.println("Total FAQ questions to verify: " + faqs.length);
        for (int i = 0; i < faqs.length; i++) {
            verifyAccordion(faqs[i], "faq_getting_started_q" + (i + 1));
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
                new By[]{downloader1449892Sub, correspondingRefCheckSub, sb08GeneratorSub,
                        removeEmbeddedFontsSub, referenceExtractorSub,
                        referenceDownloaderSub, referenceCountSub},
                new String[]{"1449 and 892 Downloader", "Corresponding RefCheck",
                        "SB-08 Generator", "Remove Embedded Fonts", "Reference Extractor",
                        "Reference Downloader", "Reference Count"}
        );

        runSubcategoryFAQs(downloader1449892Sub, "1449 and 892 Downloader",
                IDSFAQData.DOWNLOADER_1449_892);
        runComingSoon(correspondingRefCheckSub, "Corresponding RefCheck");
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
            verifyAccordion(faqs[i], "faq_patent_file_wrapper_downloader_q" + (i + 1));
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
            verifyAccordion(faqs[i], "faq_appgen_q" + (i + 1));
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
            verifyAccordion(faqs[i], "faq_oath_dec_ads_downloader_q" + (i + 1));
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
            verifyAccordion(faqs[i], "faq_oa_shell_draft_q" + (i + 1));
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
            verifyAccordion(faqs[i], "faq_claims_formatter_q" + (i + 1));
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
            verifyAccordion(faqs[i], "faq_support_help_q" + (i + 1));
        }
        System.out.println("\n  All " + faqs.length + " Support & Help FAQs verified");
    }

    private void runSubcategoryFAQs(By subcategoryLocator, String subcategoryName, FAQEntry[] faqs) {
        System.out.println("\n  --- " + subcategoryName + " FAQs ---");
        expandDropdown(subcategoryLocator);
        System.out.println("  Clicked " + subcategoryName);

        for (int i = 0; i < faqs.length; i++) {
            verifyAccordion(faqs[i],
                    "faq_" + subcategoryName.replace(" ", "_").replace("/", "_") + "_q" + (i + 1));
        }
        System.out.println("\n  All " + faqs.length + " " + subcategoryName + " FAQs verified");
    }

    private void runComingSoon(By subcategoryLocator, String subcategoryName) {
        System.out.println("\n  --- " + subcategoryName + " ---");
        expandDropdown(subcategoryLocator);

        Assert.assertTrue(waitVisible(comingSoonText).isDisplayed(),
                "Coming Soon text not visible for " + subcategoryName);
        System.out.println("  Coming Soon page verified");
    }

    private void verifyAccordion(FAQEntry entry, String screenshotName) {
        By questionLocator = entry.getLocator();
        scrollToElement(questionLocator);
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
            scrollToElement(locators[i]);
            Assert.assertTrue(waitVisible(locators[i]).isDisplayed(), names[i] + " subcategory is not visible");
            System.out.println("  Subcategory verified: " + names[i]);
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