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
import utils.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class referenceDownloaderMenuPage extends BasePage {

    // ── Timeouts ──────────────────────────────────────────────────────────────
    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String WORKFLOW_NAME = "Reference Downloader";
    private static final String QUERY_PLACEHOLDER = "Enter your query or select a task to get started";
    private static final String CANCELLED_TEXT = "Request cancelled.";
    private static final String INSTRUCTION_TEXT = "What are the reference numbers you'd like me to look up?";
    private static final String SUBMITTED_TEXT = "has been submitted successfully";
    private static final String NOTIFICATION_TEXT = "You will receive a notification once it is completed";

    // Matches "request ID 123" (completed message) and "ID: 123" (submitted-ack message).
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID:?\\s*(\\d+)");

    // ── Sidebar locators ──────────────────────────────────────────────────────
    private static final By IDS_DROPDOWN = By.xpath("//div[@role='combobox' and .//span[text()='IDS']]");

    private static final By REFERENCE_DOWNLOADER_OPTION = By.xpath("//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "')]");

    // ── Chat transcript: workflow bubble ───────────────────────────────────────
    // Anchored on this option's aria-label rather than excluding <li> ancestors - the
    // transcript bubble can itself sit under a <li> in the chat's message list, which
    // made an ancestor exclusion intermittently exclude the real bubble and time out
    // (same failure mode already fixed this way for Reference Count's own bubble).
    private static final By REFERENCE_DOWNLOADER_BUBBLE = By.xpath("(//span[@aria-label='Download references in bulk.'][normalize-space()='" + WORKFLOW_NAME + "'])[last()]");

    // ── Bot prompt: asking for reference numbers ──────────────────────────────
    // Double-quoted XPath string literal: INSTRUCTION_TEXT contains an apostrophe
    // ("you'd"), which would otherwise terminate a single-quoted literal early.
    private static final By ALL_INSTRUCTIONS = By.xpath(
            "//span[contains(text(),\"" + INSTRUCTION_TEXT + "\")]");

    // ── Chat input & controls ─────────────────────────────────────────────────
    private static final By QUERY_INPUT = By.cssSelector("textarea[placeholder='" + QUERY_PLACEHOLDER + "']");

    private static final By SUBMIT_BUTTON = By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']");

    private static final By STOP_BUTTON = By.xpath("//*[name()='svg' and @data-testid='StopCircleOutlinedIcon']");

    // ── Task completion messages ──────────────────────────────────────────────
    private static final By ALL_COMPLETED_MESSAGES = By.xpath("//p[contains(text(),'Your task with request ID') and contains(text(),'has been completed')]");

    private static final By TASK_COMPLETED_MESSAGE = By.xpath("(//p[contains(text(),'Your task with request ID')][contains(text(),'has been completed')])[last()]");

    // ── Submission acknowledgment (async path: request queued, no download link yet) ──
    private static final By ALL_SUBMITTED_MESSAGES = By.xpath("//p[contains(text(),'" + SUBMITTED_TEXT + "') and contains(text(),'" + NOTIFICATION_TEXT + "')]");

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

    // ── Submission outcomes ──────────────────────────────────────────────────
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

        private TerminalMessage(String requestId, SubmissionOutcome outcome) {
            this.requestId = requestId;
            this.outcome = outcome;
        }

        private String requestId() {
            return requestId;
        }

        private SubmissionOutcome outcome() {
            return outcome;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private String lastRequestId = null;
    private SubmissionOutcome lastOutcome = null;
    private final List<String> requestIds = new ArrayList<>();

    public referenceDownloaderMenuPage(WebDriver driver) {
        super(driver);
    }

    // ── Sidebar actions ───────────────────────────────────────────────────────

    public void clickIdsDropdown() {
        safeClick(IDS_DROPDOWN);
        Log.info("Opened the IDS dropdown");
    }

    public void selectReferenceDownloader() {
        waitVisible(REFERENCE_DOWNLOADER_OPTION).click();
        Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        safeClick(IDS_DROPDOWN);
        boolean optionReappeared = isVisibleWithin(REFERENCE_DOWNLOADER_OPTION, QUICK_PROBE_TIMEOUT);

        // Close the dropdown again so this check leaves the UI exactly as it found it -
        // an un-closed open here would otherwise toggle the dropdown shut on the next
        // real clickIdsDropdown() call.
        closeDropdown();

        Assert.assertTrue(optionReappeared,
                "IDS dropdown should offer '" + WORKFLOW_NAME + "' again once the workflow has exited");
        Log.pass("IDS dropdown is available again after the {} workflow", WORKFLOW_NAME);
    }

    private void closeDropdown() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    // Transcript verifications

    public void verifyReferenceDownloaderBubble() {
        Assert.assertTrue(waitVisible(REFERENCE_DOWNLOADER_BUBBLE).isDisplayed(),
                "'" + WORKFLOW_NAME + "' bubble is not displayed in the transcript after selection");
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

    // ── Query entry ───────────────────────────────────────────────────────────

    public void enterQuery(String query) {
        Assert.assertNotNull(query, "Query to enter must not be null");

        WebElement input = focusAndClearQueryBox();

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

    // ── Submission & completion ────────────────────────────────────────────────

    public void baselineExistingRequestIds() {
        List<WebElement> existing = driver.findElements(ALL_TERMINAL_MESSAGES);
        lastRequestId = existing.isEmpty()
                ? null
                : parseRequestId(existing.get(existing.size() - 1).getText());
        Log.info("Baselined transcript - most recent pre-existing Request ID: {}", lastRequestId);
    }

    /**
     * Waits for either outcome of a submitted query: an immediate completion (with a
     * Download link) or an async submission acknowledgment (queued, notified later).
     * Use {@link #lastSubmissionOutcome()} after calling this to see which one occurred -
     * only verify the download link when it was COMPLETED.
     */
    public String awaitTaskCompletion() {
        Log.info(" Validating {} task submission outcome ", WORKFLOW_NAME);

        String previousId = lastRequestId;
        TerminalMessage result = waitForNewTerminalMessage(previousId);

        Assert.assertTrue(result.requestId().matches("\\d+"),
                "Request ID should be numeric but was: " + result.requestId());

        lastRequestId = result.requestId();
        lastOutcome = result.outcome();
        requestIds.add(result.requestId());

        if (result.outcome() == SubmissionOutcome.ACKNOWLEDGED) {
            Assert.assertTrue(isVisibleWithin(ALL_SUBMITTED_MESSAGES, QUICK_PROBE_TIMEOUT),
                    "Submission-acknowledgment message not visible (Request ID " + result.requestId() + ")");
            Log.pass("Request acknowledged (async) - ID {} - queued for later delivery; "
                            + "actual completion must be verified separately (e.g. via Collab), not here",
                    result.requestId());
            return result.requestId();
        }

        // Wait for the stop button to disappear (generation done).
        boolean stillGenerating = !new WebDriverWait(driver, BACKEND_TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(STOP_BUTTON));
        Assert.assertFalse(stillGenerating,
                "Stop button is still present - the request has not finished generating");

        // Verify the Download link is present and clickable.
        WebElement downloadLink = waitVisible(DOWNLOAD_LINK);
        Assert.assertTrue(downloadLink.isDisplayed(),
                "Completed message is missing its 'Download' link (Request ID " + result.requestId() + ")");

        String href = downloadLink.getAttribute("href");
        Assert.assertNotNull(href,
                "'Download' link has no href attribute (Request ID " + result.requestId() + ")");
        Assert.assertFalse(href.isBlank(),
                "'Download' link href is blank (Request ID " + result.requestId() + ")");

        Log.pass("Task completed - ID {}, download link present and valid", result.requestId());
        return result.requestId();
    }

    /**
     * Outcome of the most recent {@link #awaitTaskCompletion()} call.
     */
    public SubmissionOutcome lastSubmissionOutcome() {
        return lastOutcome;
    }

    public void verifyDownloadLinkContainsRequestId(String requestId) {
        WebElement downloadLink = waitVisible(DOWNLOAD_LINK);
        String href = downloadLink.getAttribute("href");
        Assert.assertNotNull(href, "Download link href is null");
        Assert.assertTrue(href.contains(requestId),
                "Download link href does not contain Request ID " + requestId
                        + ". Actual href: " + href);
        Log.pass("Download link href contains Request ID {}", requestId);
    }

    public List<String> completedRequestIds() {
        return List.copyOf(requestIds);
    }

    // ── Continue dialog ───────────────────────────────────────────────────────

    public void clickContinueYes() {
        waitVisible(CONTINUE_CONFIRMATION);
        int before = instructionCount();

        safeClick(CONTINUE_YES);
        Log.info("Clicked YES to continue with '{}'", WORKFLOW_NAME);

        verifyNewReferenceNumbersInstruction(before);
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
            throw new TimeoutException(
                    "Continue dialog for " + WORKFLOW_NAME
                            + " is still live (NO still enabled) after clicking NO", e);
        }
        Log.pass("Continue dialog retired - workflow exited");
    }

    // ── Cancellation ──────────────────────────────────────────────────────────

    public void verifyRequestCancelled() {
        Assert.assertTrue(waitVisible(CANCELLED_MESSAGE).isDisplayed(),
                "Expected a '" + CANCELLED_TEXT + "' message after abandoning the pending prompt");
        Log.pass("'{}' message displayed", CANCELLED_TEXT);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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
                        ? new TerminalMessage(candidate, classifyOutcome(text))
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