package pages.IDSPage;

import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class referenceCountMenuPage extends BasePage {


    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);


    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;


    private static final String WORKFLOW_NAME = "Reference Count";
    private static final String QUERY_PLACEHOLDER = "Enter your query or select a task to get started";
    private static final String CANCELLED_TEXT = "Request cancelled.";
    private static final String INSTRUCTION_TEXT = "Please enter the application numbers separated by new line(s). (Maximum: 500)";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)");


    private static final By IDS_DROPDOWN = By.xpath("//div[@role='combobox' and .//span[text()='IDS']]");

    // "Corresponding RefCheck" is a separate, real IDS option (see FAQsPage/TutorialPage)
    // whose node text can also contain the substring "Reference Count" (e.g. via a
    // subtitle/description) - excluding it explicitly stops that option's node from
    // ever satisfying this locator, so this can no longer resolve to the wrong <li>.
    private static final By REFERENCE_COUNT_OPTION = By.xpath(
            "//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "') and not(contains(.,'Corresponding RefCheck'))]");


    // Anchored on this option's aria-label (a description unique to "Reference Count",
    // e.g. not shared by "Corresponding RefCheck") rather than excluding <li> ancestors -
    // the transcript bubble can itself sit under a <li> in the chat's message list, which
    // made the ancestor exclusion also exclude the real bubble and time out.
    private static final String REFERENCE_COUNT_ARIA_LABEL = "Count total cited references.";

    private static final By REFERENCE_COUNT_BUBBLE = By.xpath(
            "(//span[@aria-label='" + REFERENCE_COUNT_ARIA_LABEL + "' and normalize-space()='" + WORKFLOW_NAME + "'])[last()]");


    private static final By ALL_INSTRUCTIONS = By.xpath("//span[contains(text(),'" + INSTRUCTION_TEXT + "')]");

    private static final By QUERY_INPUT = By.cssSelector("textarea[placeholder='" + QUERY_PLACEHOLDER + "']");

    // SVG lives in its own XML namespace, so //svg does NOT work in XPath.
    private static final By SUBMIT_BUTTON = By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']");

    private static final By STOP_BUTTON = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");


    private static final By ALL_SUBMITTED_MESSAGES = By.xpath("//span[contains(text(),'is now submitted')]");

    // The other acknowledgement shape - request completed synchronously, no status page to visit.
    // contains(., ...) (not contains(text(), ...)): this message is two sentences ("The
    // total references count is N." then "Your task with Request ID ... has been
    // completed ... Download ..."), so the <p> has multiple text-node children (split
    // further by the nested Download <a>) - contains(text(), ...) only tests the FIRST
    // text node under XPath 1.0's node-set-to-string coercion, so it silently never
    // matched this shape and every sync completion fell through to the retry-resubmit path.
    private static final By ALL_COMPLETED_MESSAGES = By.xpath("//p[contains(.,'Request ID') and contains(.,'has been completed')]");

    private static final By STATUS_LINK = By.xpath("(//span[contains(text(),'is now submitted')]//span[normalize-space()='click here'])[last()]");

    private static final By CANCELLED_MESSAGE = By.xpath("(//*[normalize-space(text())='" + CANCELLED_TEXT + "'])[last()]");

    // ── Status page (reached via the async "click here" link) ──────────────────
    private static final String TASK_TYPE_LABEL = "Task Type:";
    private static final String STATUS_LABEL = "Status:";
    private static final String SUBTASK_LABEL = "Subtask:";
    private static final String EXPECTED_TASK_TYPE = "IDS";
    private static final String EXPECTED_SUBTASK = "Reference Count";
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

    // ── Synchronous completion path: inline "Download" link inside the chat message ──
    private static final By COMPLETED_DOWNLOAD_LINK = By.xpath(
            "(//p[contains(.,'Request ID') and contains(.,'has been completed')]//a[normalize-space()='Download'])[last()]");

    // How long to wait after the one refresh-and-recheck for a still-"Pending" row.
    private static final long STATUS_POLL_INTERVAL_MILLIS = 5000L;

    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;
    private static final long DOWNLOAD_POLL_MILLIS = 1000L;
    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    // ── Continue dialog: scoped by WORKFLOW NAME, because the downloader's dialog
    // ── is still sitting in this same transcript.
    private static final String CONTINUE_ANCHOR = "//*[contains(text(),'Would you like to continue with " + WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    // Element counts, not "last ID seen" - the "has been completed" message shape is
    // shared with the 1449/892 Downloader, so a stale Downloader message sitting earlier
    // in the same transcript would otherwise be misread as a "new" Reference Count
    // acknowledgement just because its ID differs from the last one we tracked.
    private int submittedMessageBaseline = 0;
    private int completedMessageBaseline = 0;

    // Recorded so a full acknowledgement timeout can retry by re-submitting the same query.
    private String lastQuery;

    private final List<String> requestIds = new ArrayList<>();

    public referenceCountMenuPage(WebDriver driver) {
        super(driver);
    }

    public void clickIdsDropdown() {
        safeClick(IDS_DROPDOWN);
        Log.info("Opened the IDS dropdown");
    }

    public void selectReferenceCount() {
        waitVisible(REFERENCE_COUNT_OPTION).click();
        Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        safeClick(IDS_DROPDOWN);
        boolean optionReappeared = isVisibleWithin(REFERENCE_COUNT_OPTION, QUICK_PROBE_TIMEOUT);

        // Close the dropdown again so this check leaves the UI exactly as it found it -
        // an un-closed open here would otherwise toggle the dropdown shut on the next
        // real clickIdsDropdown() call.
        closeDropdown();

        Assert.assertTrue(optionReappeared, "IDS dropdown should offer '" + WORKFLOW_NAME + "' again once the workflow has exited");
        Log.pass("IDS dropdown is available again after the {} workflow", WORKFLOW_NAME);
    }

    private void closeDropdown() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    public void verifyReferenceCountBubble() {
        Assert.assertTrue(waitVisible(REFERENCE_COUNT_BUBBLE).isDisplayed(), "'" + WORKFLOW_NAME + "' bubble is not displayed in the transcript after selection");
        Log.pass("'{}' bubble is displayed in the transcript", WORKFLOW_NAME);
    }

    public int instructionCount() {
        return driver.findElements(ALL_INSTRUCTIONS).size();
    }

    public void verifyNewApplicationNumbersInstruction(int previousCount) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT).until(d -> d.findElements(ALL_INSTRUCTIONS).size() > previousCount);
        } catch (TimeoutException e) {
            throw new TimeoutException("No NEW application-numbers instruction appeared for " + WORKFLOW_NAME + " - still showing the " + previousCount + " from earlier turns", e);
        }
        Log.pass("New application-numbers instruction displayed for {}", WORKFLOW_NAME);
    }

    public void enterQuery(String query) {
        Assert.assertNotNull(query, "Query to enter must not be null");
        lastQuery = query;

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


    public void baselineExistingRequestIds() {
        submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
        completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();
        Log.info("Baselined transcript - {} existing 'submitted' message(s), {} existing 'completed' message(s)",
                submittedMessageBaseline, completedMessageBaseline);
    }

    /**
     * The backend acknowledges a submission in one of two shapes:
     * - async: "...is now submitted...click here to view its current status..."
     * -> the request is still Pending; follow the status-page link to confirm it.
     * - sync:  "...has been completed. You can download the file..."
     * -> nothing further to verify here.
     * Which one shows up is a backend timing decision, not a test choice, so both
     * are valid outcomes and are branched on rather than treated as one expected
     * shape with the other as a fallback/error path.
     */
    public String awaitSubmissionAcknowledgement() {
        Log.info(" Validating {} submission ", WORKFLOW_NAME);

        Acknowledgement ack;
        try {
            ack = waitForNewAcknowledgement();
        } catch (TimeoutException firstAttempt) {
            // Nothing arrived at all in BACKEND_TIMEOUT (not even a late one) - treat this
            // as backend/environment slowness rather than a defect, and retry ONCE by
            // re-submitting the same query rather than failing the whole workflow outright.
            Log.warn("No acknowledgement arrived within {}s for '{}' - retrying once by re-submitting the same query",
                    BACKEND_TIMEOUT.getSeconds(), lastQuery);
            Assert.assertNotNull(lastQuery, "Cannot retry - no query was recorded to resubmit");

            enterQuery(lastQuery);
            clickSubmitButton();

            ack = waitForNewAcknowledgement();
            Log.pass("Retry succeeded - acknowledgement arrived on the second attempt");
        }

        Assert.assertTrue(ack.requestId.matches("\\d+"), "Request ID should be numeric but was: " + ack.requestId);

        requestIds.add(ack.requestId);

        // The send/stop icons swap in the same slot, so "stop is gone" == "generation done".
        // BACKEND_TIMEOUT, not UI_TIMEOUT: the icon swap is driven by the same slow
        // backend round-trip, so a 30s UI-grade wait would be the next thing to break.
        boolean stillGenerating = !new WebDriverWait(driver, BACKEND_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(STOP_BUTTON));
        Assert.assertFalse(stillGenerating, "Stop button is still present - the request has not finished generating");

        if (ack.isAsync) {
            followAsyncStatusFlow(ack.requestId);
        } else {
            Log.pass("Request {} completed synchronously ('has been completed') - no status page to check", ack.requestId);
            downloadFromCompletedMessage(ack.requestId);
        }

        return ack.requestId;
    }

    public List<String> submittedRequestIds() {
        return List.copyOf(requestIds);
    }


    public void clickContinueYes() {
        waitVisible(CONTINUE_CONFIRMATION);
        int before = instructionCount();

        safeClick(CONTINUE_YES);
        Log.info("Clicked YES to continue with '{}'", WORKFLOW_NAME);

        // Wait for the EFFECT, not just the click.
        verifyNewApplicationNumbersInstruction(before);
        Log.pass("Instruction prompt reappeared - ready for next input");
    }


    public void clickContinueNo() {
        waitVisible(CONTINUE_CONFIRMATION);
        safeClick(CONTINUE_NO);
        Log.info("Clicked NO - exiting the '{}' workflow", WORKFLOW_NAME);

        try {
            new WebDriverWait(driver, UI_TIMEOUT).until(d -> {
                List<WebElement> no = d.findElements(CONTINUE_NO);
                return !no.isEmpty() && !no.get(0).isEnabled();
            });
        } catch (TimeoutException e) {
            throw new TimeoutException("Continue dialog for " + WORKFLOW_NAME + " is still live (NO still enabled) after clicking NO", e);
        }
        Log.pass("Continue dialog retired - workflow exited");
    }


    public void verifyRequestCancelled() {
        Assert.assertTrue(waitVisible(CANCELLED_MESSAGE).isDisplayed(), "Expected a '" + CANCELLED_TEXT + "' message after abandoning the pending prompt");
        Log.pass("'{}' message displayed", CANCELLED_TEXT);
    }


    /**
     * A message only counts as a NEW acknowledgement if its locator's element COUNT grew
     * past the last baseline - not merely because its ID differs from the last one we
     * tracked. The "has been completed" shape is reused by the 1449/892 Downloader, so
     * comparing by ID alone would let a stale Downloader message in the same transcript
     * be misread as a fresh Reference Count acknowledgement.
     */
    private Acknowledgement waitForNewAcknowledgement() {
        long start = System.currentTimeMillis();
        try {
            Acknowledgement ack = new WebDriverWait(driver, BACKEND_TIMEOUT).until(d -> {
                List<WebElement> submitted = d.findElements(ALL_SUBMITTED_MESSAGES);
                if (submitted.size() > submittedMessageBaseline) {
                    String id = parseRequestId(submitted.get(submitted.size() - 1).getText());
                    if (id != null) {
                        return new Acknowledgement(id, true);
                    }
                }
                List<WebElement> completed = d.findElements(ALL_COMPLETED_MESSAGES);
                if (completed.size() > completedMessageBaseline) {
                    String id = parseRequestId(completed.get(completed.size() - 1).getText());
                    if (id != null) {
                        return new Acknowledgement(id, false);
                    }
                }
                return null;
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            Log.info("Backend acknowledged the request in {} ms ({}s) - {}",
                    elapsedMillis, elapsedMillis / 1000, ack.isAsync ? "async (submitted)" : "sync (completed)");

            if (elapsedMillis > EXPECTED_ACK_SECONDS * 1000) {
                Log.warn("Acknowledgement took {}s - above the expected {}s ceiling", elapsedMillis / 1000, EXPECTED_ACK_SECONDS);
            }

            // Re-baseline to the current totals so the NEXT submission in this workflow
            // (e.g. the batch continuation) only matches messages newer than this one.
            submittedMessageBaseline = driver.findElements(ALL_SUBMITTED_MESSAGES).size();
            completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();

            return ack;

        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW acknowledgement ('is now submitted' or 'has been completed') "
                            + "(normally arrives in 40-60s).", e);
        }
    }

    private static final class Acknowledgement {
        final String requestId;
        final boolean isAsync;

        Acknowledgement(String requestId, boolean isAsync) {
            this.requestId = requestId;
            this.isAsync = isAsync;
        }
    }

    /**
     * Async path only: click through to the status page, refresh to the full list,
     * expand the row for this Request ID, verify it, then return to the chat.
     */
    private void followAsyncStatusFlow(String requestId) {
        WebElement statusLink = waitVisible(STATUS_LINK);
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

    /**
     * Reads the status row for this Request ID. "Success" downloads immediately.
     * "Pending" gets exactly one refresh-and-recheck - if it's still "Pending" after
     * that, the request genuinely never completed in time to verify its download,
     * so this fails rather than silently passing: a green result must mean the
     * download was actually verified, not that verification was skipped.
     * Anything other than "Success"/"Pending" is a genuine anomaly and fails hard.
     */
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

        Assert.fail("Request ID " + requestId + " is still '" + STATUS_PENDING + "' after one refresh-and-recheck "
                + "(waited " + (STATUS_POLL_INTERVAL_MILLIS / 1000) + "s) - the backend did not complete the request "
                + "in time to verify its download");
    }

    private void assertPending(String status, String requestId) {
        Assert.assertTrue(STATUS_PENDING.equalsIgnoreCase(status),
                "Unexpected Status '" + status + "' for Request ID " + requestId
                        + " - expected '" + STATUS_SUCCESS + "' or '" + STATUS_PENDING + "'");
    }

    // The row stays expanded across a refresh once opened once, so the "down" chevron
    // this clicks is only there to find on the first check - clicking it again on a
    // later check would fail with no such element.
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
        Log.pass("Downloaded file for Request ID {} - {} ({} KB)", requestId, downloaded.getFileName(), sizeInKb(downloaded));
    }

    private void downloadFromCompletedMessage(String requestId) {
        WebElement downloadLink = waitVisible(COMPLETED_DOWNLOAD_LINK);
        Assert.assertTrue(downloadLink.isDisplayed(),
                "Completed message for Request ID " + requestId + " is missing its 'Download' link");

        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(downloadLink);
        Log.info("Clicked the 'Download' link for Request ID {}", requestId);

        Path downloaded = awaitDownloadedFile(requestId, beforeClick);
        Log.pass("Downloaded file for Request ID {} - {} ({} KB)", requestId, downloaded.getFileName(), sizeInKb(downloaded));
    }

    private Path awaitDownloadedFile(String requestId, Instant after) {
        try {
            Path downloadDir = OutputFileWorkflowManager.resolveIncomingDownloadDirectory(ConfigReader.loadConfig());
            Instant deadline = Instant.now().plusSeconds(DOWNLOAD_TIMEOUT_SECONDS);

            while (Instant.now().isBefore(deadline)) {
                Path candidate;
                // Files.list holds an OPEN OS directory handle - it must be closed,
                // or a long-running suite exhausts its file descriptors.
                try (Stream<Path> files = Files.list(downloadDir)) {
                    candidate = files
                            .filter(Files::isRegularFile)
                            .filter(referenceCountMenuPage::isNotBrowserTempFile)
                            .filter(path -> isModifiedAfter(path, after))
                            .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                            .orElse(null);
                }

                if (candidate != null) {
                    Assert.assertTrue(Files.size(candidate) > 0, "Downloaded file is empty: " + candidate);
                    return candidate;
                }
                sleepQuietly(DOWNLOAD_POLL_MILLIS);
            }

            throw new TimeoutException("Download did not complete for Request ID " + requestId
                    + ": no new file appeared in " + downloadDir + " within " + DOWNLOAD_TIMEOUT_SECONDS + "s");

        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for the downloaded file for Request ID " + requestId, e);
        }
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

    private static boolean isModifiedAfter(Path path, Instant after) {
        return Instant.ofEpochMilli(path.toFile().lastModified()).isAfter(after);
    }

    private static long sizeInKb(Path file) {
        try {
            return Files.size(file) / 1024;
        } catch (IOException e) {
            return -1;
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Catching InterruptedException CLEARS the interrupt flag - restore it so
            // whoever is shutting this thread down upstream still sees the cancellation.
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
        // Several nested ancestor divs can match this predicate; the innermost one
        // (last in document order for this axis) is the actual row container.
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
        Assert.assertTrue(rowText.contains(EXPECTED_SUBTASK),
                "Expanded row for Request ID " + requestId + " does not show Subtask: " + EXPECTED_SUBTASK);

        Log.pass("Verified status-page row for Request ID {} - Task Type: {}, Subtask: {}",
                requestId, EXPECTED_TASK_TYPE, EXPECTED_SUBTASK);
    }

    private static String parseRequestId(String messageText) {
        if (messageText == null) {
            return null;
        }
        Matcher matcher = REQUEST_ID_PATTERN.matcher(messageText);
        return matcher.find() ? matcher.group(1) : null;
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

    private boolean isVisibleWithin(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}