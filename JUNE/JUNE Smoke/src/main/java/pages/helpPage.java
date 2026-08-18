package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utils.ScreenshotUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class helpPage extends BasePage {

    // ══════════════════════════════════════════════════════════
    // LOCATORS
    // ══════════════════════════════════════════════════════════

    private final By juneIcon = By.cssSelector("img[alt='JUNE']");
    private final By helpIcon = By.xpath("//button[normalize-space()='Help']");

    // Help menu items
    private final By feedbackOption = By.xpath("//li[contains(text(),'Feedback')]");
    private final By releaseNotesOption = By.xpath("//li[contains(text(),'Release Notes')]");
    private final By faqsOption = By.xpath("//li[contains(text(),'FAQ')]");
    private final By tutorialOption = By.xpath("//li[contains(text(),'Tutorial')]");

    // Feedback form elements
    private final By feedbackFormTitle = By.xpath("//h5[contains(text(),'Feedback Form')]");
    private final By rateUsText = By.xpath("//h6[contains(text(),'Rate us')]");
    private final By cancelButton = By.xpath("//button[normalize-space()='Cancel']");

    // Release Notes sidebar
    private final By releaseNotesTitle = By.xpath(
            "//h1[contains(normalize-space(), 'Release Notes')]");
    private final By releaseItems = By.xpath(
            "//p[starts-with(normalize-space(), 'Release')]");
    private final By backButton = By.xpath(
            "//h1[contains(normalize-space(), 'Release Notes')]//button");
    private final By searchField = By.xpath("//input[@placeholder='Search']");

    // Release detail page (right panel)
    private final By releaseDetailTitle = By.cssSelector("h2.MuiTypography-h2");
    private final By keyUpdatesHeading = By.xpath(
            "//*[contains(normalize-space(), 'Key Updates and Enhancements')]");
    private final By detailContentArea = By.xpath(
            "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div");

    // ── FAQ Page ──

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

    // Getting Started FAQ accordion buttons
    private final By whatIsJune = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: What is JUNE ?']]");
    private final By howDoIGetStarted = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: How do I get started ?']]");
    private final By singleSignOn = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: Does JUNE support Single Sign-On (SSO) ?']]");
    private final By tasksUsingJune = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: What tasks can I perform using JUNE ?']]");
    private final By startTask = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: How do I start a task in JUNE ?']]");
    private final By simultaneousRequests = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: Can JUNE handle multiple simultaneous requests ?']]");
    private final By taskHistory = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: Can I view or download my task history ?']]");
    private final By updateFeatures = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: How often does JUNE update its features ?']]");
    private final By disabledFeatures = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: Why are some features disabled in my account ?']]");
    private final By customizeWorkflow = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: Can I customize JUNE to fit my workflow ?']]");
    private final By dataAvailable = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: What data is available in JUNE for US applications ?']]");
    private final By requestTimeout = By.xpath(
            "//div[@role='button'][.//p[normalize-space()='Q: What happens if my request takes more than 30 seconds to process ?']]");

    // ══════════════════════════════════════════════════════════
    // FAQ DATA
    // ══════════════════════════════════════════════════════════

    // ── Getting Started ──

    private final Object[][] FAQ_DATA = {
            {whatIsJune, "Q: What is JUNE ?",
                    "Trexo product designed for paralegals to streamline IP-related workflows"},
            {howDoIGetStarted, "Q: How do I get started ?",
                    "Once your subscription is active, we will need your user details"},
            {singleSignOn, "Q: Does JUNE support Single Sign-On (SSO) ?",
                    "Yes, JUNE supports SSO for secure access within your organization"},
            {tasksUsingJune, "Q: What tasks can I perform using JUNE ?",
                    "JUNE simplifies IP workflows with the following capabilities"},
            {startTask, "Q: How do I start a task in JUNE ?",
                    "Either select an intent from the left menu or type its name"},
            {simultaneousRequests, "Q: Can JUNE handle multiple simultaneous requests ?",
                    "JUNE is built to process multiple requests concurrently"},
            {taskHistory, "Q: Can I view or download my task history ?",
                    "Track Task Screen allows you to view your task history for up to 7 days"},
            {updateFeatures, "Q: How often does JUNE update its features ?",
                    "JUNE provides regular updates, and all subscribers receive release notes by email"},
            {disabledFeatures, "Q: Why are some features disabled in my account ?",
                    "role-based restrictions set by your administrator or limitations of your subscription plan"},
            {customizeWorkflow, "Q: Can I customize JUNE to fit my workflow ?",
                    "JUNE includes prebuilt features for fast deployment"},
            {dataAvailable, "Q: What data is available in JUNE for US applications ?",
                    "bibliographic data for US applications filed from 2000 onward"},
            {requestTimeout, "Q: What happens if my request takes more than 30 seconds to process ?",
                    "JUNE will display a notification so you can track its progress"}
    };

    // ── Bibliographic Data Extraction FAQs ──

    private final Object[][] US_TRADEMARK_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),\"What is 'US Trademark' feature\")]]"),
                    "Q: What is 'US Trademark' feature in JUNE ?",
                    "US Trademark feature lets users retrieve bibliographic details for US Trademark"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many application numbers')]]"),
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'extract data for multiple jurisdictions')]]"),
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'enter an invalid number')]]"),
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download"}
    };

    private final Object[][] US_PATENT_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),\"What is 'US Patents' feature\")]]"),
                    "Q: What is 'US Patents' feature ?",
                    "US Patent feature lets users retrieve bibliographic details for US Patents"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many application numbers')]]"),
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'extract data for multiple jurisdictions')]]"),
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'enter an invalid number')]]"),
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download"}
    };

    private final Object[][] AU_TRADEMARK_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),\"What is 'AU Trademark' feature\")]]"),
                    "Q: What is 'AU Trademark' feature ?",
                    "AU Trademark feature lets users retrieve bibliographic details for Australian trademarks"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many application numbers')]]"),
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'extract data for multiple jurisdictions')]]"),
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'enter an invalid number')]]"),
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download"}
    };

    private final Object[][] EP_PATENT_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What is the EP Patents feature')]]"),
                    "Q1: What is the EP Patents feature?",
                    "EP Patent feature lets users retrieve bibliographic details for European Patent"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What input format is supported')]]"),
                    "Q2: What input format is supported?",
                    "EP publication numbers only, consisting of 7 digits and a kind code"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Can I extract data for multiple jurisdictions')]]"),
                    "Q3: Can I extract data for multiple jurisdictions at once?",
                    "No. You must select EP Patent and enter publication numbers"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many publication numbers')]]"),
                    "Q4: How many publication numbers can I enter at once?",
                    "You can enter up to 500 publication numbers"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What happens if I enter an invalid number')]]"),
                    "Q5: What happens if I enter an invalid number?",
                    "JUNE displays an error message in the UI and sends an email notification"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q6: How quickly will I get the results?",
                    "results are generated instantly and are available for download"}
    };

    private final Object[][] AU_PATENT_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),\"What is 'AU Patent' feature\")]]"),
                    "Q: What is 'AU Patent' feature in JUNE ?",
                    "AU Patent feature lets users retrieve bibliographic details for Australian Patents"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many application numbers')]]"),
                    "Q: How many application numbers can I enter at once ?",
                    "You can enter up to 500 application numbers per request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'extract data for multiple jurisdictions')]]"),
                    "Q: Can I extract data for multiple jurisdictions at once ?",
                    "No. You must select one option at a time"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'enter an invalid number')]]"),
                    "Q: What happens if I enter an invalid number ?",
                    "JUNE shows an error in the UI and sends an email"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q: How quickly will I get the results ?",
                    "results are generated instantly and are available for download"}
    };

    private final Object[][] US_TM_IMAGE_DOWNLOAD_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What is the US Trademark Image Download')]]"),
                    "Q: What is the US Trademark Image Download module?",
                    "allows users to download trademark images associated with US trademark application numbers in bulk"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What image formats are supported')]]"),
                    "Q: What image formats are supported?",
                    "downloaded in either PNG or JPG format"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How do I download trademark images')]]"),
                    "Q: How do I download trademark images?",
                    "Enter one or more US trademark application numbers, select the desired image format"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many images can I download')]]"),
                    "Q: How many images can I download at once?",
                    "You can download up to 500 trademark images in a single request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'download images for multiple application numbers')]]"),
                    "Q: Can I download images for multiple application numbers simultaneously?",
                    "module supports bulk processing, allowing you to enter multiple US trademark application numbers"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'do not have associated trademark images')]]"),
                    "Q: What happens if some application numbers do not have associated trademark images?",
                    "download available images and notify you of any application numbers"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'choose different formats for different images')]]"),
                    "Q: Can I choose different formats for different images in the same request?",
                    "single download request can be processed in only one selected format"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'downloaded images provided in their original quality')]]"),
                    "Q: Are the downloaded images provided in their original quality?",
                    "retrieves and downloads the trademark images as available from the source records"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'limit on the number of application numbers')]]"),
                    "Q: Is there a limit on the number of application numbers I can submit?",
                    "supports downloading up to 500 images per request"}
    };

    private final Object[][] GLOBAL_PATENT_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What is Global Patent Feature')]]"),
                    "Q: What is Global Patent Feature?",
                    "retrieve bibliographic data for patent publications across multiple jurisdictions"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What input format is supported')]]"),
                    "Q: What input format is supported?",
                    "Enter one publication or patent number per line or separate multiple numbers using commas"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Which jurisdictions are supported')]]"),
                    "Q: Which jurisdictions are supported?",
                    "jurisdictions for which bibliographic data is available through the European Patent Office"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many publication or patent numbers')]]"),
                    "Q: How many publication or patent numbers can I enter at once?",
                    "You can enter up to 500 publication or patent numbers in a single request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'publication numbers from different jurisdictions')]]"),
                    "Q: Can I enter publication numbers from different jurisdictions in the same request?",
                    "include publication or patent numbers from multiple jurisdictions in a single request"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'invalid or unsupported patent number')]]"),
                    "Q: What happens if I enter an invalid or unsupported patent number?",
                    "JUNE displays an error message in the UI and sends an email notification"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How quickly will I get the results')]]"),
                    "Q: How quickly will I get the results?",
                    "results are generated instantly and are available for download"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'mix publication numbers and patent numbers')]]"),
                    "Q: Can I mix publication numbers and patent numbers in the same request?",
                    "accepts both publication and patent numbers, provided they are prefixed"}
    };

    // ── Document Generation FAQs ──

    private final Object[][] CHANGE_CORR_APP_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Change of Correspondence Address (Application)')]]"),
                    "Q: What is the \"Change of Correspondence Address (Application)\" feature used for?",
                    "generate correspondence address change forms for US patent applications"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What inputs are required for generating these forms')]]"),
                    "Q: What inputs are required for generating these forms?",
                    "application number(s), customer number, attorney name, and registration number"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'In which format are the forms downloaded')]]"),
                    "Q: In which format are the forms downloaded?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Where can I download the generated forms')]]"),
                    "Q: Where can I download the generated forms?",
                    "downloaded from the JUNE interface, or via an email link"}
    };

    private final Object[][] RCE_TRANSMITTAL_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'\"RCE Transmittal\" feature used for')]]"),
                    "Q: What is the \"RCE Transmittal\" feature used for ?",
                    "generate Request for Continued Examination (RCE) transmittal forms"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'inputs are required for generating RCE Transmittal')]]"),
                    "Q: What inputs are required for generating RCE Transmittal forms ?",
                    "application number(s), attorney name, and registration number"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'In which format are the forms downloaded')]]"),
                    "Q: In which format are the forms downloaded ?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Where can I download the generated forms')]]"),
                    "Q: Where can I download the generated forms ?",
                    "downloaded from the JUNE interface, or via an email link"}
    };

    private final Object[][] POA_GPOA_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'\"POA\" feature used for')]]"),
                    "Q: What is the \"POA\" feature used for ?",
                    "generate Power of Attorney forms quickly and efficiently"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'inputs are required for generating POA forms')]]"),
                    "Q: What inputs are required for generating POA forms ?",
                    "application number(s), attorney name, and registration number"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'In which format are the forms downloaded')]]"),
                    "Q: In which format are the forms downloaded ?",
                    "All POA forms are downloaded in PDF format"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Where can I download the generated POA forms')]]"),
                    "Q: Where can I download the generated POA forms ?",
                    "downloaded directly from the JUNE interface"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'GPOA option work while generating POA')]]"),
                    "Q: How does the GPOA option work while generating POA forms ?",
                    "upload a GPOA while generating a POA, and it will be merged"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'POA and 37 CFR be merged')]]"),
                    "Q: Can POA and 37 CFR be merged for an application number ?",
                    "both documents are generated together in a single PD"}
    };

    private final Object[][] CHANGE_CORR_PATENT_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Change of Correspondence Address (Patent)')]]"),
                    "Q: What is the \"Change of Correspondence Address (Patent)\" feature used for ?",
                    "generate correspondence address change forms specific to issued patents"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'What inputs are required for generating these forms')]]"),
                    "Q: What inputs are required for generating these forms ?",
                    "patent number(s), customer number, attorney name, and registration number"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'In which format are the forms downloaded')]]"),
                    "Q: In which format are the forms downloaded ?",
                    "Forms are generated in PDF format and can be edited using any standard PDF editor"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Where can I download the generated forms')]]"),
                    "Q: Where can I download the generated forms ?",
                    "downloaded from the JUNE interface, or via an email link"}
    };

    private final Object[][] RCE_EFS_FAQS = {
            {By.xpath("//div[@role='button'][.//p[contains(text(),'RCE Transmittal EFS (SB-30)')]]"),
                    "Q: What is the \"RCE Transmittal EFS (SB-30)\" feature used for?",
                    "generate Request for Continued Examination (RCE) transmittal forms in bulk"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'inputs are required for generating SB-30')]]"),
                    "Q: What inputs are required for generating SB-30 forms?",
                    "provide one or more U.S. application numbers"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'How many application numbers can be entered')]]"),
                    "Q: How many application numbers can be entered at once?",
                    "You can enter up to 500 application numbers at a time"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'generate forms for multiple applications')]]"),
                    "Q: Can I generate forms for multiple applications at once?",
                    "enter multiple application numbers separated by commas or paste a list"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Are all fields mandatory')]]"),
                    "Q: Are all fields mandatory while configuring the form?",
                    "most fields are optional. However, attorney or applicant name is mandatory"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'include an attorney signature')]]"),
                    "Q: Can I include an attorney signature in the generated forms?",
                    "include the attorney signature by selecting the signature checkbox"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'In which format are the forms downloaded')]]"),
                    "Q: In which format are the forms downloaded?",
                    "generated in PDF format and provided as a ZIP file"},
            {By.xpath("//div[@role='button'][.//p[contains(text(),'Where can I download the generated forms')]]"),
                    "Q: Where can I download the generated forms?",
                    "downloaded directly from the JUNE interface via a downloadable link"}
    };

    // ══════════════════════════════════════════════════════════
    // RELEASE CONTENT MAP
    // ══════════════════════════════════════════════════════════

    private static final Map<String, String[]> RELEASE_CONTENT = new LinkedHashMap<>();

    static {
        RELEASE_CONTENT.put("Release 11.0", new String[]{
                "Introducing the latest JUNE update with enhancements designed to improve flexibility and usability",
                "Patent Image File Wrapper Downloader",
                "greater flexibility when downloading prosecution documents",
                "Dual Download Options", "Collated File",
                "complete prosecution history with bookmarks",
                "Individual Files", "Each prosecution document is downloaded as a separate file",
                "Descriptive File Naming", "Application Number", "Mailing Date", "Prosecution Event"
        });
        RELEASE_CONTENT.put("Release 10.0", new String[]{
                "Introducing the latest JUNE update with new features and enhancements",
                "Global Patent Bibliographic Search",
                "Search patent or publication numbers across multiple jurisdictions",
                "bibliographic data directly from the EPO", "AppGen Module",
                "Customize document titles for CIP, 371, and Bypass Continuation",
                "SB-08 Generator", "supported reference limit to 4,000 references",
                "1,000 references for both DOCX and PDF outputs", "Embedded Font Removal",
                "USPTO-compatible PDFs with reduced file size", "OA Shell",
                "automatically retrieves and extracts claims",
                "eliminating the need to upload a separate claims document"
        });
        RELEASE_CONTENT.put("Release 9.0", new String[]{
                "Introducing trademark image retrieval capability within Bibliographic Data Extraction",
                "US Trademark Image Download", "now available under Bibliographic Data Extraction",
                "Download US trademark images in bulk",
                "Submit one or more US trademark application numbers",
                "Download images in PNG or JPG format",
                "Process up to 500 trademark images per request",
                "faster and more efficient way to retrieve trademark image assets",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 8.0", new String[]{
                "Introducing new form support and enhancements to reference-related workflows",
                "RCE Transmittal EFS (SB-30) Form",
                "Patent Center-compatible version of the SB-30 form",
                "select, generate, and download the SB-30 form", "SB-08 Generator",
                "Excel file containing a structured list of all reference numbers",
                "easier tracking, review, and downstream processing",
                "1449 and 892 Downloader", "access to recently filed SB-08 documents",
                "not yet have been reviewed or processed by the examiner",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 7.0", new String[]{
                "Introducing new automation capabilities for reference retrieval and document generation",
                "Reference Downloader", "Retrieve multiple patent references through a single request",
                "USPTO-ready output package", "Bulk reference retrieval",
                "Built-in validation checks", "Delivery status tracking",
                "Support for English and non-English references", "37 CFR 1.46",
                "Correct or Update Applicant Name",
                "generate the 1.46 form directly from the Document Generation module",
                "POA/GPOA and 37 CFR 3.73(c) workflows",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 6.0", new String[]{
                "enhancements to the Reference Extractor",
                "improving extraction accuracy and reporting capabilities", "Reference Extractor",
                "Improved extraction accuracy for both patent and non-patent literature",
                "structured Excel output with separate Patent and NPL reference columns",
                "identified page numbers", "probable NPL publication titles",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 5.0", new String[]{
                "Introducing new features and module enhancements across JUNE",
                "Patent File Wrapper Downloader",
                "Download the complete prosecution history for U.S. patents",
                "single, collated Image File Wrapper", "EP Bibliographic Data Extraction",
                "bibliographic data extraction for European Patent (EP) publications",
                "Claims Formatter", "clean and standardize claims by updating identifiers",
                "removing unwanted formatting", "generating observation reports",
                "SB-08 Generator", "now supports DOCX output", "AppGen Module",
                "Preliminary Amendment documents for 371 filing packages",
                "Initial IDS documents can now be auto-generated",
                "option to enable or disable IDS generation",
                "Specifications, Drawings, Power of Attorney",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 4.0", new String[]{
                "Introducing new forms and module enhancements across JUNE", "AIA-83",
                "Request for Withdrawal as Attorney or Agent", "AIA-81A",
                "Power of Attorney (PoA) or Revocation of PoA",
                "generate the above forms by simply following the system prompts",
                "Bibliographic Data Extraction (US Patent)", "Entity Size",
                "Office Action Mailing Date"
        });
        RELEASE_CONTENT.put("Release 3.0", new String[]{
                "streamline workflows and improve usability across the Trexo Platform",
                "Help Section", "access support and learning resources directly within JUNE",
                "Feedback", "Share suggestions or report issues", "Release Notes",
                "Stay informed about the latest changes", "FAQs",
                "Searchable, feature-specific answers", "Tutorials",
                "Watch short video walkthroughs", "OA Shell Draft",
                "Office Action response drafts by simply entering the application number",
                "auto-retrieves the necessary documents", "AppGen",
                "Bypass Continuation Filing", "Non-English Filing Package",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 2.0", new String[]{
                "Introducing new forms and workflow enhancements", "Document Generation module",
                "Change of Correspondence Address", "AIA-122", "AIA-123",
                "RCE Transmittal (SB-30)", "POA + 37 CFR",
                "merged copy of POA and 37 CFR in a single PDF",
                "Refer to the FAQ and Tutorial sections"
        });
        RELEASE_CONTENT.put("Release 1.0", new String[]{
                "Introducing the new 1449 and 892 Downloader in JUNE",
                "simplify and accelerate IDS preparation", "1449 and 892 Downloader",
                "1449 (applicant-cited) and 892 (examiner-cited) references",
                "one or multiple U.S. application numbers",
                "fetches all documents in one go", "organizes them by mailing date",
                "Refer to the FAQ and Tutorial sections"
        });
    }

    // ══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ══════════════════════════════════════════════════════════

    public helpPage(WebDriver driver) {
        super(driver);
    }

    // ══════════════════════════════════════════════════════════
    // PUBLIC ACTIONS
    // ══════════════════════════════════════════════════════════

    public void clickJuneIcon() {
        safeClick(juneIcon);
        System.out.println("Clicked the June icon");
    }

    public void clickHelpIcon() {
        safeClick(helpIcon);
        System.out.println("Clicked the Help icon");
    }

    public void checkElementsOfHelpIcon() {
        Assert.assertTrue(waitVisible(feedbackOption).isDisplayed(), "Feedback option is not displayed");
        System.out.println("Feedback option is displayed");
        Assert.assertTrue(waitVisible(releaseNotesOption).isDisplayed(), "Release Notes option is not displayed");
        System.out.println("Release Notes option is displayed");
        Assert.assertTrue(waitVisible(faqsOption).isDisplayed(), "FAQ's option is not displayed");
        System.out.println("FAQ's option is displayed");
        Assert.assertTrue(waitVisible(tutorialOption).isDisplayed(), "Tutorial option is not displayed");
        System.out.println("Tutorial option is displayed");
        ScreenshotUtil.captureScreenshot(driver, "helpMenuElements");
    }

    public void clickFeedback() {
        waitVisible(feedbackOption).click();
        System.out.println("Clicked Feedback to open the feedback form");
        Assert.assertTrue(waitVisible(feedbackFormTitle).isDisplayed(), "Feedback Form title is not visible");
        Assert.assertTrue(waitVisible(rateUsText).isDisplayed(), "Rate Us text is not visible");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cancelButton)).click();
        System.out.println("Clicked the Cancel button");
    }

    // ══════════════════════════════════════════════════════════
    // RELEASE NOTES
    // ══════════════════════════════════════════════════════════

    public void clickReleaseNotes() {
        waitVisible(releaseNotesOption).click();
        System.out.println("Clicked Release Notes");
        Assert.assertTrue(waitVisible(releaseNotesTitle).isDisplayed(), "Release Notes title is not visible");
        System.out.println("Release Notes title is displayed");

        List<WebElement> releases = driver.findElements(releaseItems);
        int totalReleases = releases.size();
        Assert.assertTrue(totalReleases > 0, "No releases found in sidebar");
        System.out.println("Total releases found: " + totalReleases);

        String[] releaseLabels = new String[totalReleases];
        for (int i = 0; i < totalReleases; i++) {
            releaseLabels[i] = releases.get(i).getText().replace('\u00A0', ' ').trim();
        }

        for (int i = 0; i < releaseLabels.length; i++) {
            String label = releaseLabels[i];
            releases = driver.findElements(releaseItems);
            WebElement releaseElement = releases.get(i);
            scrollIntoView(releaseElement);
            wait.until(ExpectedConditions.visibilityOf(releaseElement)).click();
            System.out.println("\n--- Clicked " + label + " in sidebar ---");
            verifyReleaseDetailPage(label);
        }

        ScreenshotUtil.captureScreenshot(driver, "allReleasesVerified");
        scrollToElement(backButton);
        waitVisible(backButton).click();
        System.out.println("Clicked the back button");
    }

    public void searchReleaseNotes() {
        waitVisible(releaseNotesOption).click();
        System.out.println("Clicked Release Notes");
        Assert.assertTrue(waitVisible(releaseNotesTitle).isDisplayed(), "Release Notes title is not visible");

        List<WebElement> allReleases = driver.findElements(releaseItems);
        int totalBefore = allReleases.size();
        System.out.println("Total releases before search: " + totalBefore);

        String[] allLabels = new String[totalBefore];
        for (int i = 0; i < totalBefore; i++) {
            allLabels[i] = allReleases.get(i).getText().replace('\u00A0', ' ').trim();
        }

        for (String term : allLabels) {
            WebElement search = waitVisible(searchField);
            search.click();
            search.sendKeys(Keys.CONTROL + "a");
            search.sendKeys(Keys.DELETE);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }

            search.sendKeys(term);
            System.out.println("\n--- Searched: " + term + " ---");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }

            List<WebElement> filteredReleases = driver.findElements(releaseItems);
            System.out.println("  Releases shown after filter: " + filteredReleases.size());
            Assert.assertTrue(filteredReleases.size() > 0, "No releases found after searching: " + term);

            boolean found = false;
            for (WebElement release : filteredReleases) {
                String text = release.getText().replace('\u00A0', ' ').trim();
                if (text.equals(term)) {
                    found = true;
                    System.out.println("  Found in results: " + text);
                    scrollIntoView(release);
                    wait.until(ExpectedConditions.visibilityOf(release)).click();
                    System.out.println("  Clicked " + term);

                    WebElement titleElement = waitVisible(releaseDetailTitle);
                    String actualTitle = titleElement.getText().replace('\u00A0', ' ').trim();
                    Assert.assertEquals(actualTitle, term, "Detail title mismatch after search");
                    System.out.println("  Detail page title verified: " + term);

                    Assert.assertTrue(waitVisible(keyUpdatesHeading).isDisplayed(),
                            "'Key Updates and Enhancements' not visible for " + term);
                    System.out.println("  'Key Updates and Enhancements' heading verified");
                    break;
                }
            }
            Assert.assertTrue(found, term + " not found in filtered results");
            ScreenshotUtil.captureScreenshot(driver, "search_" + term.replace(" ", "_"));
        }

        WebElement search = waitVisible(searchField);
        search.click();
        search.sendKeys(Keys.CONTROL + "a");
        search.sendKeys(Keys.DELETE);
        System.out.println("\n--- Cleared search ---");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        List<WebElement> allReleasesAfterClear = driver.findElements(releaseItems);
        System.out.println("Releases after clearing search: " + allReleasesAfterClear.size());
        Assert.assertEquals(allReleasesAfterClear.size(), totalBefore,
                "Release count mismatch after clearing search");
        System.out.println("All releases restored after clearing search");

        scrollToElement(backButton);
        waitVisible(backButton).click();
        System.out.println("Clicked the back button");
    }

    // ══════════════════════════════════════════════════════════
    // FAQs
    // ══════════════════════════════════════════════════════════

    public void clickFAQs() {
        waitVisible(faqsOption).click();
        System.out.println("Clicked FAQs");
        Assert.assertTrue(waitVisible(faqsTitle).isDisplayed(), "FAQs title is not visible");
        System.out.println("FAQs title is displayed");

        verifySidebarCategories();
        verifyBetaBadges();
        verifyFAQContent();
        verifyBibliographicFAQs();
        verifyDocumentGenerationFAQs();

        ScreenshotUtil.captureScreenshot(driver, "faqPageVerified");
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
        for (int i = 0; i < categories.length; i++) {
            scrollToElement(categories[i]);
            Assert.assertTrue(waitVisible(categories[i]).isDisplayed(),
                    categoryNames[i] + " category is not visible");
            System.out.println("  Category verified: " + categoryNames[i]);
        }
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

    // ── Getting Started FAQs ──

    private void verifyFAQContent() {
        System.out.println("\n--- Getting Started FAQs ---");
        System.out.println("Total FAQ questions to verify: " + FAQ_DATA.length);
        for (int i = 0; i < FAQ_DATA.length; i++) {
            verifyAccordionFAQ((By) FAQ_DATA[i][0], (String) FAQ_DATA[i][1],
                    (String) FAQ_DATA[i][2], "faq_getting_started_q" + (i + 1));
        }
        System.out.println("\n  All " + FAQ_DATA.length + " Getting Started FAQs verified");
    }

    // ── Bibliographic Data Extraction FAQs ──

    private void verifyBibliographicFAQs() {
        System.out.println("\n=== Bibliographic Data Extraction FAQs ===");
        scrollToElement(bibliographicDataExtraction);
        waitVisible(bibliographicDataExtraction).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        System.out.println("Clicked Bibliographic Data Extraction dropdown");

        By[] subcategories = {
                usTrademarkSub, usPatentSub, auTrademarkSub,
                epPatentSub, auPatentSub, usTmImageDownloadSub, globalPatentSub
        };
        String[] subcategoryNames = {
                "US Trademark", "US Patent", "AU Trademark",
                "EP Patent", "AU Patent", "US TM Image Download", "Global Patent"
        };
        for (int i = 0; i < subcategories.length; i++) {
            scrollToElement(subcategories[i]);
            Assert.assertTrue(waitVisible(subcategories[i]).isDisplayed(),
                    subcategoryNames[i] + " subcategory is not visible");
            System.out.println("  Subcategory verified: " + subcategoryNames[i]);
        }
        System.out.println("  All " + subcategories.length + " subcategories verified");

        verifySubcategoryFAQs(usTrademarkSub, "US Trademark", US_TRADEMARK_FAQS);
        verifySubcategoryFAQs(usPatentSub, "US Patent", US_PATENT_FAQS);
        verifySubcategoryFAQs(auTrademarkSub, "AU Trademark", AU_TRADEMARK_FAQS);
        verifySubcategoryFAQs(epPatentSub, "EP Patent", EP_PATENT_FAQS);
        verifySubcategoryFAQs(auPatentSub, "AU Patent", AU_PATENT_FAQS);
        verifySubcategoryFAQs(usTmImageDownloadSub, "US TM Image Download", US_TM_IMAGE_DOWNLOAD_FAQS);
        verifySubcategoryFAQs(globalPatentSub, "Global Patent", GLOBAL_PATENT_FAQS);
    }

    // ── Document Generation FAQs ──

    private void verifyDocumentGenerationFAQs() {
        System.out.println("\n=== Document Generation FAQs ===");
        scrollToElement(documentGeneration);
        waitVisible(documentGeneration).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
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
        for (int i = 0; i < subcategories.length; i++) {
            scrollToElement(subcategories[i]);
            Assert.assertTrue(waitVisible(subcategories[i]).isDisplayed(),
                    subcategoryNames[i] + " subcategory is not visible");
            System.out.println("  Subcategory verified: " + subcategoryNames[i]);
        }
        System.out.println("  All " + subcategories.length + " subcategories verified");

        // Subcategories with FAQs
        verifySubcategoryFAQs(changeCorrespondenceAppSub,
                "Change of correspondence address (Application)", CHANGE_CORR_APP_FAQS);
        verifySubcategoryFAQs(rceTransmittalSub, "RCE Transmittal", RCE_TRANSMITTAL_FAQS);

        // Coming Soon
        verifyComingSoon(poaRevocationSub, "PoA/Revocation of PoA/Correspondence Add Change");
        verifyComingSoon(attorneyWithdrawalSub, "Attorney Withdrawal & Address Change Request");

        // Subcategories with FAQs
        verifySubcategoryFAQs(poaGpoaSub, "POA / GPOA", POA_GPOA_FAQS);
        verifySubcategoryFAQs(changeCorrespondencePatentSub,
                "Change of correspondence address (Patent)", CHANGE_CORR_PATENT_FAQS);

        // Coming Soon
        verifyComingSoon(correctApplicantNameSub, "Correct or Update Applicant Name(1.46)");

        // Subcategories with FAQs
        verifySubcategoryFAQs(rceTransmittalEfsSub, "RCE Transmittal (EFS)", RCE_EFS_FAQS);

        // Coming Soon — update when 37 CFR FAQ content is available
        verifyComingSoon(cfrSub, "37 CFR or 3.73(c)");
    }

    // ══════════════════════════════════════════════════════════
    // REUSABLE FAQ VERIFICATION METHODS
    // ══════════════════════════════════════════════════════════

    /**
     * Clicks a subcategory, then verifies all its FAQ questions and answers.
     */
    private void verifySubcategoryFAQs(By subcategoryLocator, String subcategoryName,
                                       Object[][] faqData) {
        System.out.println("\n  --- " + subcategoryName + " FAQs ---");
        scrollToElement(subcategoryLocator);
        waitVisible(subcategoryLocator).click();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        System.out.println("  Clicked " + subcategoryName);

        for (int i = 0; i < faqData.length; i++) {
            verifyAccordionFAQ((By) faqData[i][0], (String) faqData[i][1],
                    (String) faqData[i][2],
                    "faq_" + subcategoryName.replace(" ", "_").replace("/", "_") + "_q" + (i + 1));
        }
        System.out.println("\n  All " + faqData.length + " " + subcategoryName + " FAQs verified");
    }

    /**
     * Clicks a subcategory and verifies it shows "Coming Soon".
     */
    private void verifyComingSoon(By subcategoryLocator, String subcategoryName) {
        System.out.println("\n  --- " + subcategoryName + " ---");
        scrollToElement(subcategoryLocator);
        waitVisible(subcategoryLocator).click();
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }

        Assert.assertTrue(waitVisible(comingSoonText).isDisplayed(),
                "Coming Soon text not visible for " + subcategoryName);
        System.out.println("  Coming Soon page verified");
        ScreenshotUtil.captureScreenshot(driver,
                "faq_coming_soon_" + subcategoryName.replace(" ", "_").replace("/", "_"));
    }

    /**
     * Expands a single FAQ accordion, reads the answer, and verifies the expected text.
     */
    private void verifyAccordionFAQ(By questionLocator, String questionLabel,
                                    String expectedAnswer, String screenshotName) {
        scrollToElement(questionLocator);
        WebElement questionElement = waitVisible(questionLocator);
        System.out.println("\n    " + questionLabel);

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
                "Accordion did not expand for: " + questionLabel);
        System.out.println("    Accordion expanded");

        WebElement accordionRoot = questionElement.findElement(
                By.xpath("ancestor::div[contains(@class,'MuiAccordion-root')]"));
        WebElement answerArea = accordionRoot.findElement(
                By.cssSelector("div.MuiAccordionDetails-root"));
        String answerText = answerArea.getText().replace('\u00A0', ' ').trim();

        Assert.assertTrue(answerText.length() > 10,
                "Answer too short for: " + questionLabel + " (length: " + answerText.length() + ")");
        System.out.println("    A: " + answerText.substring(0,
                Math.min(80, answerText.length())) + "...");

        Assert.assertTrue(answerText.contains(expectedAnswer),
                "Expected answer missing for: " + questionLabel
                        + "\nExpected: " + expectedAnswer
                        + "\nActual: " + answerText);
        System.out.println("    Answer content verified");

        ScreenshotUtil.captureScreenshot(driver, screenshotName);
    }

    // ══════════════════════════════════════════════════════════
    // RELEASE DETAIL VERIFICATION
    // ══════════════════════════════════════════════════════════

    private void verifyReleaseDetailPage(String expectedTitle) {
        WebElement titleElement = waitVisible(releaseDetailTitle);
        String actualTitle = titleElement.getText().replace('\u00A0', ' ').trim();
        Assert.assertEquals(actualTitle, expectedTitle, "Release detail title mismatch");
        System.out.println("  Title verified: " + expectedTitle);

        Assert.assertTrue(waitVisible(keyUpdatesHeading).isDisplayed(),
                "'Key Updates and Enhancements' not visible for " + expectedTitle);
        System.out.println("  'Key Updates and Enhancements' heading verified");

        WebElement contentArea = wait.until(
                ExpectedConditions.presenceOfElementLocated(detailContentArea));
        String pageText = contentArea.getText().replace('\u00A0', ' ').trim();

        Assert.assertTrue(pageText.length() > 50,
                "Detail page content too short for " + expectedTitle
                        + " (length: " + pageText.length() + ")");
        System.out.println("  Content length: " + pageText.length() + " chars");

        boolean hasIntro = pageText.contains("Introducing")
                || pageText.contains("This release introduces")
                || pageText.contains("This release");
        Assert.assertTrue(hasIntro,
                "Intro text missing for " + expectedTitle + ". Content starts with: "
                        + pageText.substring(0, Math.min(100, pageText.length())));
        System.out.println("  Intro text verified");

        List<WebElement> bulletItems = driver.findElements(By.xpath(
                "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div//li"));
        Assert.assertTrue(bulletItems.size() > 0,
                "No feature bullet points found for " + expectedTitle);
        System.out.println("  Bullet points found: " + bulletItems.size());
        for (WebElement bullet : bulletItems) {
            String bulletText = bullet.getText().replace('\u00A0', ' ').trim();
            if (!bulletText.isEmpty()) {
                scrollIntoView(bullet);
                Assert.assertTrue(
                        wait.until(ExpectedConditions.visibilityOf(bullet)).isDisplayed(),
                        "Bullet not visible: " + bulletText);
            }
        }
        System.out.println("  All bullet points verified");

        List<WebElement> featureHeadings = driver.findElements(By.xpath(
                "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div//strong"));
        System.out.println("  Feature headings found: " + featureHeadings.size());
        for (WebElement heading : featureHeadings) {
            String headingText = heading.getText().replace('\u00A0', ' ').trim();
            if (!headingText.isEmpty()) {
                System.out.println("    Feature: " + headingText);
            }
        }

        if (RELEASE_CONTENT.containsKey(expectedTitle)) {
            String[] expectedTexts = RELEASE_CONTENT.get(expectedTitle);
            if (expectedTexts.length > 0) {
                System.out.println("  --- Text content verification ---");
                for (String text : expectedTexts) {
                    Assert.assertTrue(pageText.contains(text),
                            "Text missing in " + expectedTitle + ": \"" + text + "\"");
                    System.out.println("  Text verified: " + text);
                }
                System.out.println("  All " + expectedTexts.length + " text snippets verified");
            }
        } else {
            System.out.println("  WARNING: No content map entry for " + expectedTitle
                    + " — only structural checks applied");
        }
        ScreenshotUtil.captureScreenshot(driver,
                "detail_" + expectedTitle.replace(" ", "_"));
    }

    private void scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
    }
}