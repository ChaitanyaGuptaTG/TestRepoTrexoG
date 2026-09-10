package pages.help;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import pages.BasePage;

public class TutorialPage extends BasePage {

    // Playback either starts near-instantly (`paused` flips synchronously on a
    // successful play()) or not at all - waiting the page's general-purpose 30s UI
    // timeout to conclude "not playing" just wastes time, so this dedicated shorter
    // wait is used for that specific check instead.
    private static final Duration VIDEO_PLAYBACK_TIMEOUT = Duration.ofSeconds(8);

    // Topics confirmed to have real tutorial video content. If one of these renders
    // "Coming Soon" instead, that's a regression to flag, not an accepted state -
    // unlike topics genuinely still pending, where either state is fine.
    private static final java.util.Set<String> VIDEO_EXPECTED_TOPICS = java.util.Set.of(
            "Patent File Wrapper Downloader", "Claims Formatter");

    // Fresh instance per verifyAll() run so one broken topic is recorded as a
    // failure but doesn't stop the rest of the page from being checked.
    private SoftAssert softAssert = new SoftAssert();

    private final By tutorialTitle = By.xpath("//h1[contains(normalize-space(), 'Tutorial')]");
    private final By tutorialBackButton = By.xpath("//h1[contains(normalize-space(), 'Tutorial')]//button");

    // Sidebar categories
    private final By admin = By.xpath(
            "//p[contains(@class,'MuiTypography-body2')][contains(text(),'Admin')]");
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

    // Patent File Wrapper Downloader graduated out of Beta - no badge expected for it anymore.

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

    // Content area states
    private final By comingSoonText = By.xpath(
            "//*[contains(text(),'This will be available shortly')]");
    private final By videoCard = By.xpath(
            "//div[contains(@class,'MuiBox-root')][.//img or .//video]");
    private final By playButton = By.xpath("//*[@data-testid='PlayCircleOutlineIcon']");
    private final By videoPlayer = By.cssSelector("video");
    private final By modalBackdrop = By.cssSelector(".MuiBackdrop-root");

    public TutorialPage(WebDriver driver) {
        super(driver);
    }

    public void verifyAll() {
        softAssert = new SoftAssert();

        Assert.assertTrue(waitVisible(tutorialTitle).isDisplayed(), "Tutorial title is not visible");
        System.out.println("Tutorial title is displayed");

        verifySidebarCategories();
        runCategorySoft("Admin", this::verifyAdminTutorial);
        runCategorySoft("Bibliographic Data Extraction", this::verifyBibliographicTutorials);
        runCategorySoft("Document Generation", this::verifyDocumentGenerationTutorials);
        runCategorySoft("IDS", this::verifyIDSTutorials);
        runCategorySoft("Patent File Wrapper Downloader", this::verifyPatentFileWrapperDownloaderTutorial);
        runCategorySoft("AppGen", this::verifyAppGenTutorial);
        runCategorySoft("Oath/Dec & ADS Downloader", this::verifyOathDecAdsTutorial);
        runCategorySoft("OA Shell Draft", this::verifyOAShellDraftTutorial);
        runCategorySoft("Claims Formatter", this::verifyClaimsFormatterTutorial);

        scrollToElement(tutorialBackButton);
        waitVisible(tutorialBackButton).click();
        System.out.println("Clicked the back button");

        softAssert.assertAll();
    }

    // Top-level category verification is wrapped the same way runSubcategoryTopics()
    // and verifyTopicContentSoft() already wrap their own leaf-level checks - so an
    // exception from ANY category (a stuck modal backdrop, a missing dropdown, etc.)
    // is recorded as a soft failure instead of aborting every category checked after it.
    private void runCategorySoft(String categoryName, Runnable verification) {
        try {
            verification.run();
        } catch (Throwable t) {
            System.out.println("  ERROR verifying '" + categoryName + "' category: " + t.getMessage());
            softAssert.fail(categoryName + " category verification failed: " + t.getMessage());
        }
    }

    private void verifySidebarCategories() {
        System.out.println("\n--- Sidebar Categories ---");
        By[] categories = {
                admin, bibliographicDataExtraction, documentGeneration, ids,
                patentFileWrapperDownloader, appGen, oathDecAdsDownloader,
                oaShellDraft, claimsFormatter
        };
        String[] categoryNames = {
                "Admin", "Bibliographic Data Extraction", "Document Generation", "IDS",
                "Patent File Wrapper Downloader", "AppGen", "Oath/Dec & ADS Downloader",
                "OA Shell Draft", "Claims Formatter"
        };
        for (int i = 0; i < categories.length; i++) {
            try {
                scrollToElement(categories[i]);
                Assert.assertTrue(waitVisible(categories[i]).isDisplayed(),
                        categoryNames[i] + " category is not visible");
                System.out.println("  Category verified: " + categoryNames[i]);
            } catch (Throwable t) {
                System.out.println("  ERROR verifying category '" + categoryNames[i] + "': " + t.getMessage());
                softAssert.fail(categoryNames[i] + " category check failed: " + t.getMessage());
            }
        }
    }

    private void verifyBibliographicTutorials() {
        System.out.println("\n=== Bibliographic Data Extraction Tutorials ===");
        expandDropdown(bibliographicDataExtraction);

        By[] subcategories = {
                usTrademarkSub, usPatentSub, auTrademarkSub,
                epPatentSub, auPatentSub, usTmImageDownloadSub, globalPatentSub
        };
        String[] subcategoryNames = {
                "US Trademark", "US Patent", "AU Trademark",
                "EP Patent", "AU Patent", "US TM Image Download", "Global Patent"
        };
        runSubcategoryTopics(subcategories, subcategoryNames, "bibliographic");
    }

    private void verifyDocumentGenerationTutorials() {
        System.out.println("\n=== Document Generation Tutorials ===");
        expandDropdown(documentGeneration);

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
        runSubcategoryTopics(subcategories, subcategoryNames, "document_generation");
    }

    private void verifyIDSTutorials() {
        System.out.println("\n=== IDS Tutorials ===");
        expandDropdown(ids);

        By[] subcategories = {
                downloader1449892Sub, /* correspondingRefCheckSub, */ sb08GeneratorSub,
                removeEmbeddedFontsSub, referenceExtractorSub, referenceDownloaderSub, referenceCountSub
        };
        String[] subcategoryNames = {
                "1449 and 892 Downloader", /* "Corresponding RefCheck", */ "SB-08 Generator",
                "Remove Embedded Fonts", "Reference Extractor", "Reference Downloader", "Reference Count"
        };
        runSubcategoryTopics(subcategories, subcategoryNames, "ids");
    }

    private void verifyAdminTutorial() {
        System.out.println("\n=== Admin Tutorial ===");
        verifyTopicContentSoft(admin, "Admin", "tutorial_admin");
    }

    private void verifyPatentFileWrapperDownloaderTutorial() {
        System.out.println("\n=== Patent File Wrapper Downloader Tutorial ===");
        verifyTopicContentSoft(patentFileWrapperDownloader, "Patent File Wrapper Downloader",
                "tutorial_patent_file_wrapper_downloader");
    }

    private void verifyAppGenTutorial() {
        System.out.println("\n=== AppGen Tutorial ===");
        verifyTopicContentSoft(appGen, "AppGen", "tutorial_appgen");
    }

    private void verifyOathDecAdsTutorial() {
        System.out.println("\n=== Oath/Dec & ADS Downloader Tutorial ===");
        verifyTopicContentSoft(oathDecAdsDownloader, "Oath/Dec & ADS Downloader",
                "tutorial_oath_dec_ads_downloader");
    }

    private void verifyOAShellDraftTutorial() {
        System.out.println("\n=== OA Shell Draft Tutorial ===");
        verifyTopicContentSoft(oaShellDraft, "OA Shell Draft", "tutorial_oa_shell_draft");
    }

    private void verifyClaimsFormatterTutorial() {
        System.out.println("\n=== Claims Formatter Tutorial ===");
        verifyTopicContentSoft(claimsFormatter, "Claims Formatter", "tutorial_claims_formatter");
    }

    private void runSubcategoryTopics(By[] subcategories, String[] names, String screenshotPrefix) {
        for (int i = 0; i < subcategories.length; i++) {
            System.out.println("\n  --- " + names[i] + " ---");
            try {
                expandDropdown(subcategories[i]);
                verifyTopicContent(names[i], "tutorial_" + screenshotPrefix + "_"
                        + names[i].replace(" ", "_").replace("/", "_"));
            } catch (Throwable t) {
                System.out.println("  ERROR verifying " + names[i] + " tutorial: " + t.getMessage());
                softAssert.fail(names[i] + " tutorial failed: " + t.getMessage());
            }
        }
    }

    private void verifyTopicContentSoft(By dropdownLocator, String topicName, String screenshotName) {
        try {
            expandDropdown(dropdownLocator);
            verifyTopicContent(topicName, screenshotName);
        } catch (Throwable t) {
            System.out.println("  ERROR verifying " + topicName + " tutorial: " + t.getMessage());
            softAssert.fail(topicName + " tutorial failed: " + t.getMessage());
        }
    }

    private void verifyTopicContent(String topicName, String screenshotName) {
        // No extra sleep here: expandDropdown() already waited 500ms after the click that got us here.
        if (!driver.findElements(comingSoonText).isEmpty()) {
            if (VIDEO_EXPECTED_TOPICS.contains(topicName)) {
                throw new AssertionError(topicName + " is showing 'Coming Soon' but is expected to have "
                        + "tutorial video content (regression)");
            }
            Assert.assertTrue(waitVisible(comingSoonText).isDisplayed(),
                    "Coming Soon message not visible for " + topicName);
            System.out.println("    Coming Soon content verified for " + topicName);
        } else {
            Assert.assertTrue(waitVisible(videoCard).isDisplayed(), "Video card not visible for " + topicName);
            System.out.println("    Video card verified for " + topicName);
            verifyVideoPlayback(topicName, screenshotName);
        }
    }

    private void verifyVideoPlayback(String topicName, String screenshotName) {
        if (driver.findElements(videoPlayer).isEmpty()) {
            // Not already open (e.g. the default-selected tab may show its video without a play-button click)
            safeClick(playButton);
        }

        // Everything below is wrapped so closeVideoModal() ALWAYS runs, even when a
        // verification below fails (e.g. "did not start playing within 30s"). Without
        // this, a failed check here left the modal (and its backdrop) open, which then
        // blocked the very next click made anywhere else on the page - turning one
        // soft-asserted video failure into a hard crash for every topic checked after it.
        try {
            // Re-locate the video element fresh at each step below: React re-renders the
            // player around playback start, which can stale-out a reference held across steps.
            String src = waitVisible(videoPlayer).getAttribute("src");
            Assert.assertTrue(src != null && !src.isEmpty(), "Video src is empty for " + topicName);
            System.out.println("    Video src present for " + topicName);
            Assert.assertNotNull(waitVisible(videoPlayer).getAttribute("controls"),
                    "Video controls attribute missing for " + topicName);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // The video element being present doesn't mean it's playing - e.g. on the
            // default-selected tab the modal can open with the video already paused at 0:00.
            // Clicking the app's play overlay or the native control bar is unreliable to target
            // precisely, so start playback directly; ChromeOptions disables the autoplay-gesture
            // requirement (see BaseTest) so this isn't blocked by Chrome's autoplay policy.
            if (Boolean.TRUE.equals(js.executeScript("return arguments[0].paused;", waitVisible(videoPlayer)))) {
                js.executeScript("arguments[0].play();", waitVisible(videoPlayer));
            }

            try {
                new WebDriverWait(driver, VIDEO_PLAYBACK_TIMEOUT).until(d -> Boolean.FALSE.equals(
                        js.executeScript("return arguments[0].paused;", waitVisible(videoPlayer))));
            } catch (TimeoutException e) {
                Assert.fail("Video did not start playing for " + topicName + " within "
                        + VIDEO_PLAYBACK_TIMEOUT.getSeconds() + "s");
            }
            System.out.println("    Video playback started for " + topicName);

            // `paused` flips to false synchronously on play(), but readyState climbs
            // asynchronously as data buffers in - wait for it rather than checking once.
            Long readyState = null;
            try {
                readyState = new WebDriverWait(driver, VIDEO_PLAYBACK_TIMEOUT).until(d -> {
                    Long rs = (Long) js.executeScript("return arguments[0].readyState;", waitVisible(videoPlayer));
                    return (rs != null && rs >= 2) ? rs : null;
                });
            } catch (TimeoutException ignored) {
            }
            Assert.assertTrue(readyState != null && readyState >= 2,
                    "Video has not loaded playable data for " + topicName + " (readyState=" + readyState + ")");

            Double t1 = (Double) js.executeScript("return arguments[0].currentTime;", waitVisible(videoPlayer));
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            Double t2 = (Double) js.executeScript("return arguments[0].currentTime;", waitVisible(videoPlayer));
            Assert.assertTrue(t2 > t1,
                    "Video currentTime did not advance for " + topicName + " (t1=" + t1 + ", t2=" + t2 + ")");
            System.out.println("    Video is playing (currentTime advanced from " + t1 + " to " + t2 + ")");
        } finally {
            closeVideoModal();
        }
    }

    private void closeVideoModal() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // Backdrop click is the reliable path; Escape can land on the video's native
        // fullscreen control instead of closing the dialog, so it's only a fallback.
        if (!driver.findElements(modalBackdrop).isEmpty()) {
            safeClick(modalBackdrop);
        } else {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        }

        try {
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(videoPlayer));
            System.out.println("    Closed video modal");
            return;
        } catch (TimeoutException ignored) {
        }

        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        try {
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(videoPlayer));
            System.out.println("    Closed video modal via Escape");
        } catch (TimeoutException ignored) {
            System.out.println("    Video did not close as a modal (likely an inline/default-tab video) - continuing");
        }
    }

    private void expandDropdown(By locator) {
        scrollToElement(locator);
        waitVisible(locator).click();
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