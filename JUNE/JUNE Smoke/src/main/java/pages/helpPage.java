package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utils.ScreenshotUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Keys;

public class helpPage extends BasePage {

    // ── Locators ──

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
    private final By releaseNotesTitle = By.xpath("//h1[contains(normalize-space(), 'Release Notes')]");
    private final By releaseItems = By.xpath("//p[starts-with(normalize-space(), 'Release')]");
    private final By backButton = By.xpath("//h1[contains(normalize-space(), 'Release Notes')]//button");
    private final By searchField = By.xpath("//input[@placeholder='Search']");

    // Release detail page (right panel)
    private final By releaseDetailTitle = By.cssSelector("h2.MuiTypography-h2");
    private final By keyUpdatesHeading = By.xpath("//*[contains(normalize-space(), 'Key Updates and Enhancements')]");
    private final By detailContentArea = By.xpath("//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div");

    // ── Release content map ──

    private static final Map<String, String[]> RELEASE_CONTENT = new LinkedHashMap<>();

    static {
        RELEASE_CONTENT.put("Release 11.0", new String[]{
                "Introducing the latest JUNE update with enhancements designed to improve flexibility and usability",
                "Patent Image File Wrapper Downloader",
                "greater flexibility when downloading prosecution documents",
                "Dual Download Options",
                "Collated File",
                "complete prosecution history with bookmarks",
                "Individual Files",
                "Each prosecution document is downloaded as a separate file",
                "Descriptive File Naming",
                "Application Number",
                "Mailing Date",
                "Prosecution Event"
        });

        RELEASE_CONTENT.put("Release 10.0", new String[]{
                "Introducing the latest JUNE update with new features and enhancements",
                "Global Patent Bibliographic Search",
                "Search patent or publication numbers across multiple jurisdictions",
                "bibliographic data directly from the EPO",
                "AppGen Module",
                "Customize document titles for CIP, 371, and Bypass Continuation",
                "SB-08 Generator",
                "supported reference limit to 4,000 references",
                "1,000 references for both DOCX and PDF outputs",
                "Embedded Font Removal",
                "USPTO-compatible PDFs with reduced file size",
                "OA Shell",
                "automatically retrieves and extracts claims",
                "eliminating the need to upload a separate claims document"
        });

        RELEASE_CONTENT.put("Release 9.0", new String[]{
                "Introducing trademark image retrieval capability within Bibliographic Data Extraction",
                "US Trademark Image Download",
                "now available under Bibliographic Data Extraction",
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
                "select, generate, and download the SB-30 form",
                "SB-08 Generator",
                "Excel file containing a structured list of all reference numbers",
                "easier tracking, review, and downstream processing",
                "1449 and 892 Downloader",
                "access to recently filed SB-08 documents",
                "not yet have been reviewed or processed by the examiner",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 7.0", new String[]{
                "Introducing new automation capabilities for reference retrieval and document generation",
                "Reference Downloader",
                "Retrieve multiple patent references through a single request",
                "USPTO-ready output package",
                "Bulk reference retrieval",
                "Built-in validation checks",
                "Delivery status tracking",
                "Support for English and non-English references",
                "37 CFR 1.46",
                "Correct or Update Applicant Name",
                "generate the 1.46 form directly from the Document Generation module",
                "POA/GPOA and 37 CFR 3.73(c) workflows",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 6.0", new String[]{
                "enhancements to the Reference Extractor",
                "improving extraction accuracy and reporting capabilities",
                "Reference Extractor",
                "Improved extraction accuracy for both patent and non-patent literature",
                "structured Excel output with separate Patent and NPL reference columns",
                "identified page numbers",
                "probable NPL publication titles",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 5.0", new String[]{
                "Introducing new features and module enhancements across JUNE",
                "Patent File Wrapper Downloader",
                "Download the complete prosecution history for U.S. patents",
                "single, collated Image File Wrapper",
                "EP Bibliographic Data Extraction",
                "bibliographic data extraction for European Patent (EP) publications",
                "Claims Formatter",
                "clean and standardize claims by updating identifiers",
                "removing unwanted formatting",
                "generating observation reports",
                "SB-08 Generator",
                "now supports DOCX output",
                "AppGen Module",
                "Preliminary Amendment documents for 371 filing packages",
                "Initial IDS documents can now be auto-generated",
                "option to enable or disable IDS generation",
                "Specifications, Drawings, Power of Attorney",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 4.0", new String[]{
                "Introducing new forms and module enhancements across JUNE",
                "AIA-83",
                "Request for Withdrawal as Attorney or Agent",
                "AIA-81A",
                "Power of Attorney (PoA) or Revocation of PoA",
                "generate the above forms by simply following the system prompts",
                "Bibliographic Data Extraction (US Patent)",
                "Entity Size",
                "Office Action Mailing Date"
        });

        RELEASE_CONTENT.put("Release 3.0", new String[]{
                "streamline workflows and improve usability across the Trexo Platform",
                "Help Section",
                "access support and learning resources directly within JUNE",
                "Feedback",
                "Share suggestions or report issues",
                "Release Notes",
                "Stay informed about the latest changes",
                "FAQs",
                "Searchable, feature-specific answers",
                "Tutorials",
                "Watch short video walkthroughs",
                "OA Shell Draft",
                "Office Action response drafts by simply entering the application number",
                "auto-retrieves the necessary documents",
                "AppGen",
                "Bypass Continuation Filing",
                "Non-English Filing Package",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 2.0", new String[]{
                "Introducing new forms and workflow enhancements",
                "Document Generation module",
                "Change of Correspondence Address",
                "AIA-122",
                "AIA-123",
                "RCE Transmittal (SB-30)",
                "POA + 37 CFR",
                "merged copy of POA and 37 CFR in a single PDF",
                "Refer to the FAQ and Tutorial sections"
        });

        RELEASE_CONTENT.put("Release 1.0", new String[]{
                "Introducing the new 1449 and 892 Downloader in JUNE",
                "simplify and accelerate IDS preparation",
                "1449 and 892 Downloader",
                "1449 (applicant-cited) and 892 (examiner-cited) references",
                "one or multiple U.S. application numbers",
                "fetches all documents in one go",
                "organizes them by mailing date",
                "Refer to the FAQ and Tutorial sections"
        });
    }

    // ── Constructor ──

    public helpPage(WebDriver driver) {
        super(driver);
    }

    // ── Actions ──

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

    public void clickReleaseNotes() {
        waitVisible(releaseNotesOption).click();
        System.out.println("Clicked Release Notes");

        // Verify title
        Assert.assertTrue(waitVisible(releaseNotesTitle).isDisplayed(), "Release Notes title is not visible");
        System.out.println("Release Notes title is displayed");

        // Dynamically discover all releases from the sidebar
        List<WebElement> releases = driver.findElements(releaseItems);
        int totalReleases = releases.size();
        Assert.assertTrue(totalReleases > 0, "No releases found in sidebar");
        System.out.println("Total releases found: " + totalReleases);

        // Collect all release labels dynamically
        String[] releaseLabels = new String[totalReleases];
        for (int i = 0; i < totalReleases; i++) {
            releaseLabels[i] = releases.get(i).getText().replace('\u00A0', ' ').trim();
        }

        // Click each release and verify its detail page
        for (int i = 0; i < releaseLabels.length; i++) {
            String label = releaseLabels[i];

            // Re-fetch list each time (DOM may refresh after click)
            releases = driver.findElements(releaseItems);
            WebElement releaseElement = releases.get(i);

            // Scroll and click
            scrollIntoView(releaseElement);
            wait.until(ExpectedConditions.visibilityOf(releaseElement)).click();
            System.out.println("\n--- Clicked " + label + " in sidebar ---");

            // Verify the right panel
            verifyReleaseDetailPage(label);
        }

        ScreenshotUtil.captureScreenshot(driver, "allReleasesVerified");

        // Navigate back
        scrollToElement(backButton);
        waitVisible(backButton).click();
        System.out.println("Clicked the back button");
    }

    public void searchReleaseNotes() {
        waitVisible(releaseNotesOption).click();
        System.out.println("Clicked Release Notes");

        Assert.assertTrue(waitVisible(releaseNotesTitle).isDisplayed(),
                "Release Notes title is not visible");

        // Get total release count before searching
        List<WebElement> allReleases = driver.findElements(releaseItems);
        int totalBefore = allReleases.size();
        System.out.println("Total releases before search: " + totalBefore);

        // Dynamically collect all release labels
        String[] allLabels = new String[totalBefore];
        for (int i = 0; i < totalBefore; i++) {
            allLabels[i] = allReleases.get(i).getText().replace('\u00A0', ' ').trim();
        }

        // Search for each release and verify
        for (String term : allLabels) {
            // Clear the search field completely, then type
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

            // Wait for filter to apply
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }

            // Verify filtered results
            List<WebElement> filteredReleases = driver.findElements(releaseItems);
            System.out.println("  Releases shown after filter: " + filteredReleases.size());

            Assert.assertTrue(filteredReleases.size() > 0, "No releases found after searching: " + term);

            // The searched release should be in the filtered list
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

                    Assert.assertTrue(waitVisible(keyUpdatesHeading).isDisplayed(), "'Key Updates and Enhancements' not visible for " + term);
                    System.out.println("  'Key Updates and Enhancements' heading verified");

                    break;
                }
            }
            Assert.assertTrue(found, term + " not found in filtered results");

            ScreenshotUtil.captureScreenshot(driver, "search_" + term.replace(" ", "_"));
        }

        // Clear search and verify all releases return
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
        System.out.println("Releases after clearing search: "
                + allReleasesAfterClear.size());
        Assert.assertEquals(allReleasesAfterClear.size(), totalBefore,
                "Release count mismatch after clearing search");
        System.out.println("All releases restored after clearing search");

        // Navigate back
        scrollToElement(backButton);
        waitVisible(backButton).click();
        System.out.println("Clicked the back button");
    }
    // ── Verification ──

    private void verifyReleaseDetailPage(String expectedTitle) {
        // 1. Verify the h2 detail title matches the sidebar label
        WebElement titleElement = waitVisible(releaseDetailTitle);
        String actualTitle = titleElement.getText().replace('\u00A0', ' ').trim();
        Assert.assertEquals(actualTitle, expectedTitle, "Release detail title mismatch");
        System.out.println("  Title verified: " + expectedTitle);

        // 2. Verify 'Key Updates and Enhancements' heading
        Assert.assertTrue(waitVisible(keyUpdatesHeading).isDisplayed(), "'Key Updates and Enhancements' not visible for " + expectedTitle);
        System.out.println("  'Key Updates and Enhancements' heading verified");

        // 3. Verify content area has meaningful text
        WebElement contentArea = wait.until(
                ExpectedConditions.presenceOfElementLocated(detailContentArea));
        String pageText = contentArea.getText().replace('\u00A0', ' ').trim();

        Assert.assertTrue(pageText.length() > 50, "Detail page content too short for " + expectedTitle + " (length: " + pageText.length() + ")");
        System.out.println("  Content length: " + pageText.length() + " chars");

        // 4. Verify intro line exists
        boolean hasIntro = pageText.contains("Introducing") || pageText.contains("This release introduces") || pageText.contains("This release");
        Assert.assertTrue(hasIntro, "Intro text missing for " + expectedTitle + ". Content starts with: " + pageText.substring(0, Math.min(100, pageText.length())));
        System.out.println("  Intro text verified");

        // 5. Dynamically verify all bullet points are visible
        List<WebElement> bulletItems = driver.findElements(By.xpath("//h2[contains(@class,'MuiTypography-h2')]" + "/ancestor::div[1]/parent::div//li"));
        Assert.assertTrue(bulletItems.size() > 0, "No feature bullet points found for " + expectedTitle);
        System.out.println("  Bullet points found: " + bulletItems.size());

        for (WebElement bullet : bulletItems) {
            String bulletText = bullet.getText().replace('\u00A0', ' ').trim();
            if (!bulletText.isEmpty()) {
                scrollIntoView(bullet);
                Assert.assertTrue(wait.until(ExpectedConditions.visibilityOf(bullet)).isDisplayed(), "Bullet not visible: " + bulletText);
            }
        }
        System.out.println("  All bullet points verified");
        // 6. Dynamically find and log bold feature headings
        List<WebElement> featureHeadings = driver.findElements(By.xpath("//h2[contains(@class,'MuiTypography-h2')]" + "/ancestor::div[1]/parent::div//strong"));
        System.out.println("  Feature headings found: " + featureHeadings.size());

        for (WebElement heading : featureHeadings) {
            String headingText = heading.getText().replace('\u00A0', ' ').trim();
            if (!headingText.isEmpty()) {
                System.out.println("    Feature: " + headingText);
            }
        }
        // 7. Verify all specific text content from the content map
        if (RELEASE_CONTENT.containsKey(expectedTitle)) {
            String[] expectedTexts = RELEASE_CONTENT.get(expectedTitle);
            if (expectedTexts.length > 0) {
                System.out.println("  --- Text content verification ---");
                for (String text : expectedTexts) {
                    Assert.assertTrue(pageText.contains(text), "Text missing in " + expectedTitle + ": \"" + text + "\"");
                    System.out.println("  Text verified: " + text);
                }
                System.out.println("  All " + expectedTexts.length + " text snippets verified");
            }
        } else {
            System.out.println("  WARNING: No content map entry for " + expectedTitle + " — only structural checks applied");
        }
        ScreenshotUtil.captureScreenshot(driver, "detail_" + expectedTitle.replace(" ", "_"));
    }
}