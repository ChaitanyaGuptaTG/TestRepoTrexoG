package pages.IDSPage;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import pages.BasePage;
import pages.IDSPage.helpers.ContinueDialogHelper;
import pages.IDSPage.helpers.DownloadFileHelper;
import pages.IDSPage.helpers.IdsDropdownHelper;
import pages.IDSPage.helpers.QueryInputHelper;
import utils.ConfigReader;
import utils.Log;
import utils.OutputFileWorkflowManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class referenceDownloaderMenuPage extends BasePage {

    // ── Timeouts ──────────────────────────────────────────────────────────────
    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration BUBBLE_RETRY_PROBE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration OPTION_VISIBLE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration RETRY_SETTLE_BUFFER = Duration.ofSeconds(2);
    private static final int MAX_SELECTION_ATTEMPTS = 3;
    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;

    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;
    private static final long DOWNLOAD_POLL_MILLIS = 1000L;
    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String WORKFLOW_NAME = "Reference Downloader";
    private static final String CANCELLED_TEXT = "Request cancelled.";
    private static final String INSTRUCTION_TEXT = "What are the reference numbers you'd like me to look up?";
    private static final String SUBMITTED_TEXT = "has been submitted successfully";
    // e.g. "The expected delivery timeline is 07-Sep-2026, 09:41 AM EST." - the date/time
    // itself is backend-generated and not worth pinning down, but its presence is.
    private static final String DELIVERY_TIMELINE_TEXT = "The expected delivery timeline is";
    private static final String NOTIFICATION_TEXT = "You will receive a notification once it is completed";

    // Matches "request ID 123" (completed message) and "ID: 123" (submitted-ack message).
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID:?\\s*(\\d+)");

    // ── Sidebar locators ──────────────────────────────────────────────────────
    private static final By REFERENCE_DOWNLOADER_OPTION = By.xpath("//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "')]");

    // ── Chat transcript: workflow bubble ───────────────────────────────────────
    private static final By ALL_REFERENCE_DOWNLOADER_BUBBLES = By.xpath("//span[normalize-space()='" + WORKFLOW_NAME + "'][not(ancestor::li[@role='option'])]");

    // ── Bot prompt: asking for reference numbers ──────────────────────────────
    private static final By ALL_INSTRUCTIONS = By.xpath(
            "//span[contains(text(),\"" + INSTRUCTION_TEXT + "\")]");

    // ── Chat input & controls ─────────────────────────────────────────────────
    private static final By STOP_BUTTON = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");

    // ── Either a completion or a submission-acknowledgment message ────────────
    private static final By ALL_TERMINAL_MESSAGES = By.xpath("//p[(contains(text(),'Your task with request ID') and contains(text(),'has been completed'))"
            + " or (contains(text(),'" + SUBMITTED_TEXT + "') and contains(text(),'" + NOTIFICATION_TEXT + "'))]");

    // ── Download link inside completed message ────────────────────────────────
    private static final By DOWNLOAD_LINK = By.xpath("(//p[contains(text(),'Your task with request ID')]//a[normalize-space()='Download'])[last()]");

    // ── Cancelled message ─────────────────────────────────────────────────────
    private static final By CANCELLED_MESSAGE = By.xpath("(//*[normalize-space(text())='" + CANCELLED_TEXT + "'])[last()]");

    // ── Continue dialog: scoped by WORKFLOW NAME ──────────────────────────────
    private static final String CONTINUE_ANCHOR = "//*[contains(text(),'Would you like to continue with " + WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    // Submission outcomes
    public enum SubmissionOutcome {
        /**
         * Synchronous path: "has been completed" message with a Download link.
         */
        COMPLETED,
        /**
         * Async path: "submitted successfully... will receive a notification" - no download link yet.
         * Actual completion must be verified elsewhere (e.g. Collab), not by this page object.
         */
        ACKNOWLEDGED
    }

    private static final class TerminalMessage {
        private final String requestId;
        private final SubmissionOutcome outcome;
        private final String text;

        private TerminalMessage(String requestId, SubmissionOutcome outcome, String text) {
            this.requestId = requestId;
            this.outcome = outcome;
            this.text = text;
        }

        private String requestId() {
            return requestId;
        }

        private SubmissionOutcome outcome() {
            return outcome;
        }

        private String text() {
            return text;
        }
    }

    // State
    private String lastRequestId = null;
    private SubmissionOutcome lastOutcome = null;
    private final List<String> requestIds = new ArrayList<>();
    private int bubbleCountBeforeSelection = 0;

    private final IdsDropdownHelper dropdown;
    private final QueryInputHelper queryInput;
    private final ContinueDialogHelper continueDialog;

    public referenceDownloaderMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.queryInput = new QueryInputHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
    }

    // Sidebar actions

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectReferenceDownloader() {
//        bubbleCountBeforeSelection = driver.findElements(ALL_REFERENCE_DOWNLOADER_BUBBLES).size();

        for (int attempt = 1; attempt <= MAX_SELECTION_ATTEMPTS; attempt++) {
            boolean clicked = tryClickReferenceDownloaderOption();
            if (clicked && newBubbleAppearedWithin(BUBBLE_RETRY_PROBE_TIMEOUT)) {
                return;
            }

            if (attempt == MAX_SELECTION_ATTEMPTS) {
                break;
            }

            Log.warn("No '{}' bubble within {}s of attempt {}/{} - forcing the dropdown closed and " +
                            "reopening before retrying (known app-side glitch can replay the previous IDS " +
                            "workflow, or leave the option list stuck, instead of switching)",
                    WORKFLOW_NAME, BUBBLE_RETRY_PROBE_TIMEOUT.getSeconds(), attempt, MAX_SELECTION_ATTEMPTS);
            dropdown.closeDropdown();
            sleepUninterruptibly(RETRY_SETTLE_BUFFER);
            dropdown.clickIdsDropdown();
        }
    }

    private boolean tryClickReferenceDownloaderOption() {
        try {
            new WebDriverWait(driver, OPTION_VISIBLE_TIMEOUT)
                    .until(ExpectedConditions.visibilityOfElementLocated(REFERENCE_DOWNLOADER_OPTION));

            sleepUninterruptibly(RETRY_SETTLE_BUFFER);

            WebElement option = new WebDriverWait(driver, QUICK_PROBE_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(REFERENCE_DOWNLOADER_OPTION));
            Log.info("Clicking IDS dropdown option with text: '{}'", option.getText());
            safeClick(option);
            Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
            return true;
        } catch (TimeoutException e) {
            Log.warn("'{}' option did not become visible/clickable within {}s", WORKFLOW_NAME, OPTION_VISIBLE_TIMEOUT.getSeconds());
            return false;
        }
    }

    private static void sleepUninterruptibly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            Log.warn("Interrupted while waiting before retrying the '{}' selection", WORKFLOW_NAME);
        }
    }

    private boolean newBubbleAppearedWithin(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> d.findElements(ALL_REFERENCE_DOWNLOADER_BUBBLES).size() > bubbleCountBeforeSelection);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(REFERENCE_DOWNLOADER_OPTION, WORKFLOW_NAME, QUICK_PROBE_TIMEOUT);
    }


    public void verifyReferenceDownloaderBubble() {
        int previousCount = bubbleCountBeforeSelection;
        try {
            new WebDriverWait(driver, UI_TIMEOUT)
                    .until(d -> d.findElements(ALL_REFERENCE_DOWNLOADER_BUBBLES).size() > previousCount);
        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "No NEW '" + WORKFLOW_NAME + "' bubble appeared in the transcript after selection "
                            + "- still showing the " + previousCount + " from earlier turns", e);
        }
        Log.pass("'{}' bubble is displayed in the transcript", WORKFLOW_NAME);
    }

    public int instructionCount() {
        return driver.findElements(ALL_INSTRUCTIONS).size();
    }

    public void verifyNewReferenceNumbersInstruction(int previousCount) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT)
                    .until(d -> d.findElements(ALL_INSTRUCTIONS).size() > previousCount);
        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "No NEW reference-numbers instruction appeared for " + WORKFLOW_NAME
                            + " - still showing the " + previousCount + " from earlier turns", e);
        }
        Log.pass("New reference-numbers instruction displayed for {}", WORKFLOW_NAME);
    }

    // Query entry

    public void enterQuery(String query) {
        queryInput.enterQuery(query);
    }

    public void enterIntentAsQuery(String intentText) {
        queryInput.enterIntentAsQuery(intentText);
    }

    public void clickSubmitButton() {
        queryInput.clickSubmitButton();
    }


    public void baselineExistingRequestIds() {
        List<WebElement> existing = driver.findElements(ALL_TERMINAL_MESSAGES);
        lastRequestId = existing.isEmpty()
                ? null
                : parseRequestId(existing.get(existing.size() - 1).getText());
        Log.info("Baselined transcript - most recent pre-existing Request ID: {}", lastRequestId);
    }

    public String awaitTaskCompletion() {
        Log.info(" Validating {} task submission outcome ", WORKFLOW_NAME);

        String previousId = lastRequestId;
        TerminalMessage result = waitForNewTerminalMessage(previousId);

        Assert.assertTrue(result.requestId().matches("\\d+"), "Request ID should be numeric but was: " + result.requestId());

        lastRequestId = result.requestId();
        lastOutcome = result.outcome();
        requestIds.add(result.requestId());

        if (result.outcome() == SubmissionOutcome.ACKNOWLEDGED) {
            verifyAsyncAcknowledgementMessage(result.text(), result.requestId());
            Log.pass("Request acknowledged (async) - ID {} - queued for later delivery; " + "actual completion must be verified separately (e.g. via Collab), not here",
                    result.requestId());
            return result.requestId();
        }


        boolean stillGenerating = !new WebDriverWait(driver, BACKEND_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(STOP_BUTTON));
        Assert.assertFalse(stillGenerating, "Stop button is still present - the request has not finished generating");


        WebElement downloadLink = waitVisible(DOWNLOAD_LINK);
        Assert.assertTrue(downloadLink.isDisplayed(), "Completed message is missing its 'Download' link (Request ID " + result.requestId() + ")");

        String href = downloadLink.getAttribute("href");
        Assert.assertNotNull(href, "'Download' link has no href attribute (Request ID " + result.requestId() + ")");
        Assert.assertFalse(href.isBlank(), "'Download' link href is blank (Request ID " + result.requestId() + ")");


        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(downloadLink);
        Log.info("Clicked the 'Download' link for Request ID {}", result.requestId());

        Path downloaded = awaitDownloadedFile(result.requestId(), beforeClick);
        Log.pass("Task completed - ID {}, downloaded {} ({} KB)", result.requestId(), downloaded.getFileName(), DownloadFileHelper.sizeInKb(downloaded));
        return result.requestId();
    }

    private void verifyAsyncAcknowledgementMessage(String messageText, String requestId) {
        Assert.assertTrue(messageText.contains("(ID: " + requestId + ")"), "Async acknowledgement message does not reference Request ID " + requestId + " as '(ID: " + requestId + ")'. Actual: " + messageText);
        Assert.assertTrue(messageText.contains(SUBMITTED_TEXT), "Async acknowledgement message missing '" + SUBMITTED_TEXT + "'. Actual: " + messageText);
        Assert.assertTrue(messageText.contains(DELIVERY_TIMELINE_TEXT), "Async acknowledgement message missing '" + DELIVERY_TIMELINE_TEXT + "'. Actual: " + messageText);
        Assert.assertTrue(messageText.contains(NOTIFICATION_TEXT), "Async acknowledgement message missing '" + NOTIFICATION_TEXT + "'. Actual: " + messageText);
    }

    private Path awaitDownloadedFile(String requestId, Instant after) {
        try {
            Path downloadDir = OutputFileWorkflowManager.resolveIncomingDownloadDirectory(ConfigReader.loadConfig());
            Path file = DownloadFileHelper.waitForNewFile(downloadDir, null, DOWNLOAD_TIMEOUT_SECONDS, DOWNLOAD_POLL_MILLIS, after);

            if (file == null) {
                throw new TimeoutException("Download did not complete for Request ID " + requestId + ": no new file appeared in " + downloadDir + " within " + DOWNLOAD_TIMEOUT_SECONDS + "s");
            }
            Assert.assertTrue(Files.size(file) > 0, "Downloaded file is empty: " + file);
            return file;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the Reference Downloader download", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for the downloaded file for Request ID " + requestId, e);
        }
    }

    public SubmissionOutcome lastSubmissionOutcome() {
        return lastOutcome;
    }

    public void verifyDownloadLinkContainsRequestId(String requestId) {
        WebElement downloadLink = waitVisible(DOWNLOAD_LINK);
        String href = downloadLink.getAttribute("href");
        Assert.assertNotNull(href, "Download link href is null");
        Assert.assertTrue(href.contains(requestId), "Download link href does not contain Request ID " + requestId + ". Actual href: " + href);
        Log.pass("Download link href contains Request ID {}", requestId);
    }

    public List<String> completedRequestIds() {
        return List.copyOf(requestIds);
    }

    // Continue dialog

    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        int before = instructionCount();

        continueDialog.clickYes(CONTINUE_YES, WORKFLOW_NAME);

        verifyNewReferenceNumbersInstruction(before);
        Log.pass("Instruction prompt reappeared - ready for next input");
    }

    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, WORKFLOW_NAME);
        continueDialog.waitUntilNoButtonDisabled(CONTINUE_NO, WORKFLOW_NAME);
    }

    // Cancellation

    public void verifyRequestCancelled() {
        Assert.assertTrue(waitVisible(CANCELLED_MESSAGE).isDisplayed(),
                "Expected a '" + CANCELLED_TEXT + "' message after abandoning the pending prompt");
        Log.pass("'{}' message displayed", CANCELLED_TEXT);
    }

    //  Private helpers

    private TerminalMessage waitForNewTerminalMessage(String previousId) {
        long start = System.currentTimeMillis();
        try {
            TerminalMessage result = new WebDriverWait(driver, BACKEND_TIMEOUT).until(d -> {
                List<WebElement> messages = d.findElements(ALL_TERMINAL_MESSAGES);
                if (messages.isEmpty()) {
                    return null;
                }
                String text = messages.get(messages.size() - 1).getText();
                String candidate = parseRequestId(text);
                return (candidate != null && !candidate.equals(previousId))
                        ? new TerminalMessage(candidate, classifyOutcome(text), text)
                        : null;
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            Log.info("Backend responded in {} ms ({}s) - outcome: {}",
                    elapsedMillis, elapsedMillis / 1000, result.outcome());

            if (result.outcome() == SubmissionOutcome.COMPLETED
                    && elapsedMillis > EXPECTED_ACK_SECONDS * 1000) {
                Log.warn("Completion took {}s - above the expected {}s ceiling",
                        elapsedMillis / 1000, EXPECTED_ACK_SECONDS);
            }
            return result;

        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW completion or submission-acknowledgment message "
                            + "for " + WORKFLOW_NAME + ". "
                            + "Still showing the previous Request ID: " + previousId, e);
        }
    }

    private static SubmissionOutcome classifyOutcome(String messageText) {
        return (messageText.contains(SUBMITTED_TEXT) && messageText.contains(NOTIFICATION_TEXT)) ? SubmissionOutcome.ACKNOWLEDGED : SubmissionOutcome.COMPLETED;
    }

    private static String parseRequestId(String messageText) {
        if (messageText == null) {
            return null;
        }
        Matcher matcher = REQUEST_ID_PATTERN.matcher(messageText);
        return matcher.find() ? matcher.group(1) : null;
    }
}