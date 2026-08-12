package pages.help;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import pages.BasePage;
import pages.help.data.ReleaseNotesData;
import utils.ScreenshotUtil;

import java.util.List;

public class ReleaseNotesPage extends BasePage {

    private final By releaseNotesTitle = By.xpath(
            "//h1[contains(normalize-space(), 'Release Notes')]");
    private final By releaseItems = By.xpath(
            "//p[starts-with(normalize-space(), 'Release')]");
    private final By backButton = By.xpath(
            "//h1[contains(normalize-space(), 'Release Notes')]//button");
    private final By searchField = By.xpath("//input[@placeholder='Search']");

    private final By releaseDetailTitle = By.cssSelector("h2.MuiTypography-h2");
    private final By keyUpdatesHeading = By.xpath(
            "//*[contains(normalize-space(), 'Key Updates and Enhancements')]");
    private final By detailContentArea = By.xpath(
            "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div");
    private final By featureBullets = By.xpath(
            "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div//li");
    private final By featureHeadings = By.xpath(
            "//h2[contains(@class,'MuiTypography-h2')]/ancestor::div[1]/parent::div//strong");

    public ReleaseNotesPage(WebDriver driver) {
        super(driver);
    }

    public void verifyAllReleases() {
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
            verifyDetailPage(label);
        }

        ScreenshotUtil.captureScreenshot(driver, "allReleasesVerified");
        navigateBack();
    }

    public void verifySearch() {
        Assert.assertTrue(waitVisible(releaseNotesTitle).isDisplayed(), "Release Notes title is not visible");

        List<WebElement> allReleases = driver.findElements(releaseItems);
        int totalBefore = allReleases.size();
        System.out.println("Total releases before search: " + totalBefore);

        String[] allLabels = new String[totalBefore];
        for (int i = 0; i < totalBefore; i++) {
            allLabels[i] = allReleases.get(i).getText().replace('\u00A0', ' ').trim();
        }

        for (String term : allLabels) {
            typeInSearch(term);
            verifySearchResults(term);
        }

        typeInSearch("");
        clearSearch(totalBefore);
        navigateBack();
    }

    private void verifyDetailPage(String expectedTitle) {
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

        List<WebElement> bulletItems = driver.findElements(featureBullets);
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

        List<WebElement> headings = driver.findElements(featureHeadings);
        System.out.println("  Feature headings found: " + headings.size());
        for (WebElement heading : headings) {
            String headingText = heading.getText().replace('\u00A0', ' ').trim();
            if (!headingText.isEmpty()) {
                System.out.println("    Feature: " + headingText);
            }
        }

        if (ReleaseNotesData.hasContent(expectedTitle)) {
            String[] expectedTexts = ReleaseNotesData.getTexts(expectedTitle);
            System.out.println("  --- Text content verification ---");
            for (String text : expectedTexts) {
                Assert.assertTrue(pageText.contains(text),
                        "Text missing in " + expectedTitle + ": \"" + text + "\"");
                System.out.println("  Text verified: " + text);
            }
            System.out.println("  All " + expectedTexts.length + " text snippets verified");
        } else {
            System.out.println("  WARNING: No content map entry for " + expectedTitle
                    + " — only structural checks applied");
        }
        ScreenshotUtil.captureScreenshot(driver,
                "detail_" + expectedTitle.replace(" ", "_"));
    }

    private void typeInSearch(String term) {
        WebElement search = waitVisible(searchField);
        search.click();
        search.sendKeys(Keys.CONTROL + "a");
        search.sendKeys(Keys.DELETE);
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }

        if (!term.isEmpty()) {
            search.sendKeys(term);
            System.out.println("\n--- Searched: " + term + " ---");
        } else {
            System.out.println("\n--- Cleared search ---");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    private void verifySearchResults(String term) {
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

    private void clearSearch(int expectedCount) {
        List<WebElement> releasesAfterClear = driver.findElements(releaseItems);
        System.out.println("Releases after clearing search: " + releasesAfterClear.size());
        Assert.assertEquals(releasesAfterClear.size(), expectedCount,
                "Release count mismatch after clearing search");
        System.out.println("All releases restored after clearing search");
    }

    private void navigateBack() {
        scrollToElement(backButton);
        waitVisible(backButton).click();
        System.out.println("Clicked the back button");
    }

    private void scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        scrollIntoView(element);
    }
}
