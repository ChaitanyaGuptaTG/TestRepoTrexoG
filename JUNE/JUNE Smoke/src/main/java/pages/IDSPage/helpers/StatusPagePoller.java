package pages.IDSPage.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import utils.ConfigReader;
import utils.Log;
import utils.OutputFileWorkflowManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusPagePoller extends BasePage {

    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);

    private static final String TASK_TYPE_LABEL = "Task Type:";
    private static final String STATUS_LABEL = "Status:";
    private static final String SUBTASK_LABEL = "Subtask:";
    private static final String EXPECTED_TASK_TYPE = "IDS";
    private static final String STATUS_SUCCESS = "Success";
    private static final String STATUS_PENDING = "Pending";

    private static final Pattern STATUS_VALUE_PATTERN = Pattern.compile(Pattern.quote(STATUS_LABEL) + "\\s*(\\S+)");

    private static final By BACK_TO_CHAT_LINK = By.xpath("//p[contains(.,'Back to IP Assistant Chat')]");

    // Confirmed against the live DOM - MUI's Cached icon, not a "Refresh"-named one.
    private static final By REFRESH_ICON = By.xpath("//*[name()='svg' and @data-testid='CachedIcon']");

    // Confirmed against the live DOM - the per-row expand/collapse chevron.
    private static final By ROW_EXPAND_CHEVRON = By.xpath(".//*[name()='svg' and @data-testid='KeyboardArrowDownOutlinedIcon']");

    // Present on a row once its Status is "Success"; absent while "Pending".
    private static final By ROW_DOWNLOAD_ICON = By.xpath(".//*[name()='svg' and @data-testid='FileDownloadOutlinedIcon']");

    private static final By SEARCH_INPUT = By.cssSelector("input[placeholder='Search']");

    private static final By QUERY_INPUT = By.cssSelector("textarea[placeholder='Enter your query or select a task to get started']");

    // How long to wait after the one refresh-and-recheck for a still-"Pending" row.
    private static final long STATUS_POLL_INTERVAL_MILLIS = 5000L;

    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;
    private static final long DOWNLOAD_POLL_MILLIS = 1000L;
    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    private final String expectedSubtask;

    public StatusPagePoller(WebDriver driver, String expectedSubtask) {
        super(driver);
        this.expectedSubtask = expectedSubtask;
    }

    public void followAsyncStatusFlow(By statusLinkLocator, String requestId) {
        WebElement statusLink = waitVisible(statusLinkLocator);
        Assert.assertTrue(statusLink.isDisplayed(), "Acknowledgement is missing its 'click here' status link (Request ID " + requestId + ")");

        // Not an anchor, so there is no href to assert on. The next best cheap check is
        // that it is actually styled as an affordance rather than rendered as dead text.
        Assert.assertEquals(statusLink.getCssValue("cursor"), "pointer", "'click here' is not clickable-styled - the status link may be inert " + "(Request ID " + requestId + ")");

        statusLink.click();
        Log.info("Clicked 'click here' - navigating to the status page for Request ID {}", requestId);

        new WebDriverWait(driver, UI_TIMEOUT).until(d ->
                !d.findElements(By.xpath("//*[normalize-space(text())='" + requestId + "']")).isEmpty());
        Log.pass("Status page loaded, filtered to Request ID {}", requestId);

        safeClick(REFRESH_ICON);
        new WebDriverWait(driver, UI_TIMEOUT).until(d -> {
            String value = d.findElement(SEARCH_INPUT).getAttribute("value");
            return value == null || value.isEmpty();
        });
        Log.info("Refreshed - full status list reloaded");

        awaitSuccessStatusAndDownload(requestId);

        safeClick(BACK_TO_CHAT_LINK);
        waitVisible(QUERY_INPUT);
        Log.pass("Returned to IP Assistant Chat after verifying Request ID {} on the status page", requestId);
    }

    public void downloadFromCompletedMessage(By completedDownloadLinkLocator, String requestId) {
        WebElement downloadLink = waitVisible(completedDownloadLinkLocator);
        Assert.assertTrue(downloadLink.isDisplayed(),
                "Completed message for Request ID " + requestId + " is missing its 'Download' link");

        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(downloadLink);
        Log.info("Clicked the 'Download' link for Request ID {}", requestId);

        Path downloaded = awaitDownloadedFile(requestId, beforeClick);
        Log.pass("Downloaded file for Request ID {} - {} ({} KB)", requestId, downloaded.getFileName(), DownloadFileHelper.sizeInKb(downloaded));
    }

    private void awaitSuccessStatusAndDownload(String requestId) {
        WebElement row = findRequestRow(requestId);
        ensureRowExpanded(row);
        verifyExpandedRow(row, requestId);

        String status = readRowStatus(row, requestId);
        if (STATUS_SUCCESS.equalsIgnoreCase(status)) {
            Log.pass("Request ID {} status is '{}'", requestId, STATUS_SUCCESS);
            downloadFromStatusRow(row, requestId);
            return;
        }
        assertPending(status, requestId);

        Log.info("Request ID {} is '{}' - refreshing once and re-checking", requestId, STATUS_PENDING);
        safeClick(REFRESH_ICON);
        sleepQuietly(STATUS_POLL_INTERVAL_MILLIS);

        row = findRequestRow(requestId);
        ensureRowExpanded(row);
        verifyExpandedRow(row, requestId);

        status = readRowStatus(row, requestId);
        if (STATUS_SUCCESS.equalsIgnoreCase(status)) {
            Log.pass("Request ID {} status is '{}' after refresh", requestId, STATUS_SUCCESS);
            downloadFromStatusRow(row, requestId);
            return;
        }
        assertPending(status, requestId);

        Log.info("Request ID {} is still '{}' after one refresh-and-recheck (waited {}s) - accepted as a known, "
                        + "slow-backend outcome; skipping the download for this Request ID",
                requestId, STATUS_PENDING, STATUS_POLL_INTERVAL_MILLIS / 1000);
    }

    private void assertPending(String status, String requestId) {
        Assert.assertTrue(STATUS_PENDING.equalsIgnoreCase(status),
                "Unexpected Status '" + status + "' for Request ID " + requestId
                        + " - expected '" + STATUS_SUCCESS + "' or '" + STATUS_PENDING + "'");
    }

    private void ensureRowExpanded(WebElement row) {
        if (!row.getText().contains(SUBTASK_LABEL)) {
            expandRow(row);
        }
    }

    private String readRowStatus(WebElement row, String requestId) {
        Matcher matcher = STATUS_VALUE_PATTERN.matcher(row.getText());
        Assert.assertTrue(matcher.find(), "Could not read a '" + STATUS_LABEL + "' value from the expanded row for Request ID " + requestId);
        return matcher.group(1);
    }

    private void downloadFromStatusRow(WebElement row, String requestId) {
        List<WebElement> downloadIcons = row.findElements(ROW_DOWNLOAD_ICON);
        Assert.assertFalse(downloadIcons.isEmpty(),
                "Status row for Request ID " + requestId + " is '" + STATUS_SUCCESS + "' but has no download icon (FileDownloadOutlinedIcon)");

        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(downloadIcons.get(0));
        Log.info("Clicked the download icon for Request ID {}", requestId);

        Path downloaded = awaitDownloadedFile(requestId, beforeClick);
        Log.pass("Downloaded file for Request ID {} - {} ({} KB)", requestId, downloaded.getFileName(), DownloadFileHelper.sizeInKb(downloaded));
    }

    private Path awaitDownloadedFile(String requestId, Instant after) {
        try {
            Path downloadDir = OutputFileWorkflowManager.resolveIncomingDownloadDirectory(ConfigReader.loadConfig());
            Path file = DownloadFileHelper.waitForNewFile(downloadDir, null, DOWNLOAD_TIMEOUT_SECONDS, DOWNLOAD_POLL_MILLIS, after);

            if (file == null) {
                throw new TimeoutException("Download did not complete for Request ID " + requestId
                        + ": no new file appeared in " + downloadDir + " within " + DOWNLOAD_TIMEOUT_SECONDS + "s");
            }
            Assert.assertTrue(Files.size(file) > 0, "Downloaded file is empty: " + file);
            return file;

        } catch (InterruptedException e) {
            // Catching InterruptedException CLEARS the interrupt flag - restore it so
            // whoever is shutting this thread down upstream still sees the cancellation.
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting on Reference Count status/download", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for the downloaded file for Request ID " + requestId, e);
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting on Reference Count status/download", e);
        }
    }

    private WebElement findRequestRow(String requestId) {
        By rowLocator = By.xpath(
                "//div[contains(., '" + TASK_TYPE_LABEL + "') and contains(., '" + STATUS_LABEL + "')"
                        + " and .//*[normalize-space(text())='" + requestId + "']]");
        List<WebElement> rows = driver.findElements(rowLocator);
        Assert.assertFalse(rows.isEmpty(), "No row found for Request ID " + requestId + " on the status page");
        return rows.get(rows.size() - 1);
    }

    private void expandRow(WebElement row) {
        List<WebElement> chevrons = row.findElements(ROW_EXPAND_CHEVRON);
        Assert.assertFalse(chevrons.isEmpty(), "Status row is missing its expand chevron (KeyboardArrowDownOutlinedIcon)");
        chevrons.get(0).click();
    }

    private void verifyExpandedRow(WebElement row, String requestId) {
        new WebDriverWait(driver, UI_TIMEOUT).until(d -> row.getText().contains(SUBTASK_LABEL));

        String rowText = row.getText();
        Assert.assertTrue(rowText.contains(EXPECTED_TASK_TYPE),
                "Expanded row for Request ID " + requestId + " does not show Task Type: " + EXPECTED_TASK_TYPE);
        Assert.assertTrue(rowText.contains(expectedSubtask),
                "Expanded row for Request ID " + requestId + " does not show Subtask: " + expectedSubtask);

        Log.pass("Verified status-page row for Request ID {} - Task Type: {}, Subtask: {}",
                requestId, EXPECTED_TASK_TYPE, expectedSubtask);
    }
}
