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
import pages.IDSPage.helpers.IdsDropdownHelper;
import pages.IDSPage.helpers.QueryInputHelper;
import pages.IDSPage.helpers.StatusPagePoller;
import utils.Log;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page object for the "Reference Count" IDS workflow. The mechanics shared
 * with the other IDS workflows (dropdown, query box, continue dialog, the
 * async status-page flow) live in {@code pages.IDSPage.helpers}; this class
 * owns the workflow-specific locators and the submission-acknowledgement
 * logic (sync vs. async outcome, request-ID tracking, the ack-retry policy).
 */
public class referenceCountMenuPage extends BasePage {


    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);


    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;


    private static final String WORKFLOW_NAME = "Reference Count";
    private static final String CANCELLED_TEXT = "Request cancelled.";
    private static final String INSTRUCTION_TEXT = "Please enter the application numbers separated by new line(s). (Maximum: 500)";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)");


    // "Corresponding RefCheck" is a separate, real IDS option (see FAQsPage/TutorialPage)
    // whose node text can also contain the substring "Reference Count" (e.g. via a
    // subtitle/description) - excluding it explicitly stops that option's node from
    // ever satisfying this locator, so this can no longer resolve to the wrong <li>.
    private static final By REFERENCE_COUNT_OPTION = By.xpath(
            "//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "') and not(contains(.,'Corresponding RefCheck'))]");


    // The transcript bubble is a plain <span>Reference Count</span> with no aria-label -
    // the dropdown option is the only place that attribute lives, so anchoring on it (as an
    // earlier revision did) could never match a real bubble and always timed out. By the
    // time this is checked, selectReferenceCount() has already clicked the option and the
    // dropdown has closed, so a plain text match is unambiguous.
    private static final By REFERENCE_COUNT_BUBBLE = By.xpath(
            "(//span[normalize-space()='" + WORKFLOW_NAME + "'])[last()]");


    private static final By ALL_INSTRUCTIONS = By.xpath("//span[contains(text(),'" + INSTRUCTION_TEXT + "')]");

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
    private static final String EXPECTED_SUBTASK = "Reference Count";

    // ── Synchronous completion path: inline "Download" link inside the chat message ──
    private static final By COMPLETED_DOWNLOAD_LINK = By.xpath(
            "(//p[contains(.,'Request ID') and contains(.,'has been completed')]//a[normalize-space()='Download'])[last()]");

    // ── Continue dialog: scoped by WORKFLOW NAME, because the downloader's dialog
    // ── is still sitting in this same transcript.
    private static final String CONTINUE_ANCHOR = "//*[contains(text(),'Would you like to continue with " + WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    // Same shared query-box placeholder every IDS workflow reverts to on exit
    // (see QueryInputHelper.QUERY_PLACEHOLDER) - waiting on it here, rather than
    // just the NO button going disabled, proves the workflow has actually retired
    // and the dropdown has been re-armed, not merely that the click registered.
    private static final By WORKFLOW_EXITED_MESSAGE = By.xpath("(//*[contains(text(),'Enter your query or select a task to get started')])[last()]");

    // Element counts, not "last ID seen" - the "has been completed" message shape is
    // shared with the 1449/892 Downloader, so a stale Downloader message sitting earlier
    // in the same transcript would otherwise be misread as a "new" Reference Count
    // acknowledgement just because its ID differs from the last one we tracked.
    private int submittedMessageBaseline = 0;
    private int completedMessageBaseline = 0;

    // Recorded so a full acknowledgement timeout can retry by re-submitting the same query.
    private String lastQuery;

    private final List<String> requestIds = new ArrayList<>();

    private final IdsDropdownHelper dropdown;
    private final QueryInputHelper queryInput;
    private final ContinueDialogHelper continueDialog;
    private final StatusPagePoller statusPagePoller;

    public referenceCountMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.queryInput = new QueryInputHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
        this.statusPagePoller = new StatusPagePoller(driver, EXPECTED_SUBTASK);
    }

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectReferenceCount() {
        Assert.assertTrue(driver.findElement(REFERENCE_COUNT_OPTION).isDisplayed(), "The workflow option should be visible.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(REFERENCE_COUNT_OPTION)).click();
//        waitVisible(REFERENCE_COUNT_OPTION).click();
        Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(REFERENCE_COUNT_OPTION, WORKFLOW_NAME, QUICK_PROBE_TIMEOUT);
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
        lastQuery = query;
        queryInput.enterQuery(query);
    }

    public void enterIntentAsQuery(String intentText) {
        queryInput.enterIntentAsQuery(intentText);
    }

    public void clickSubmitButton() {
        queryInput.clickSubmitButton();
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
            statusPagePoller.followAsyncStatusFlow(STATUS_LINK, ack.requestId);
        } else {
            Log.pass("Request {} completed synchronously ('has been completed') - no status page to check", ack.requestId);
            statusPagePoller.downloadFromCompletedMessage(COMPLETED_DOWNLOAD_LINK, ack.requestId);
        }

        return ack.requestId;
    }

    public List<String> submittedRequestIds() {
        return List.copyOf(requestIds);
    }


    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        int before = instructionCount();

        continueDialog.clickYes(CONTINUE_YES, WORKFLOW_NAME);

        // Wait for the EFFECT, not just the click.
        verifyNewApplicationNumbersInstruction(before);
        Log.pass("Instruction prompt reappeared - ready for next input");
    }


    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, WORKFLOW_NAME);
        continueDialog.waitUntilNoButtonDisabled(CONTINUE_NO, WORKFLOW_NAME);

        // Wait for the EFFECT, not just the click: the query box reverting to its
        // default placeholder is what proves the dropdown has actually been re-armed.
        waitVisible(WORKFLOW_EXITED_MESSAGE);
        Log.pass("Workflow exited - chat returned to its default prompt");
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

    private static String parseRequestId(String messageText) {
        if (messageText == null) {
            return null;
        }
        Matcher matcher = REQUEST_ID_PATTERN.matcher(messageText);
        return matcher.find() ? matcher.group(1) : null;
    }
}
