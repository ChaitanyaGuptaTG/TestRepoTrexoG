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

public class removeEmbeddedFontsMenuPage extends BasePage {

    // Timeouts
    private static final Duration UI_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration QUICK_PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration OPTION_VISIBLE_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration BUBBLE_RETRY_PROBE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration RETRY_SETTLE_BUFFER = Duration.ofSeconds(2);
    private static final int MAX_SELECTION_ATTEMPTS = 3;

    private static final Duration BACKEND_TIMEOUT = Duration.ofSeconds(90);
    private static final long EXPECTED_ACK_SECONDS = 60L;

    private static final int DOWNLOAD_TIMEOUT_SECONDS = 30;
    private static final long DOWNLOAD_POLL_MILLIS = 1000L;
    private static final long CLOCK_SKEW_SLACK_SECONDS = 1L;

    // Constants
    private static final String WORKFLOW_NAME = "Remove Embedded Fonts";
    // Confirmed live: the continue-dialog text names the workflow differently
    // than the dropdown option/bubble does.
    private static final String CONTINUE_WORKFLOW_NAME = "Embedded Fonts Removal";
    private static final String UPLOAD_INSTRUCTION_TEXT = "Please upload PDF files or a ZIP containing PDFs";

    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("ID\\s*(\\d+)");

    // Sidebar locators
    private static final By REMOVE_EMBEDDED_FONTS_OPTION = By.xpath(
            "//li[@role='option' and contains(.,'" + WORKFLOW_NAME + "')]");

    //  Chat transcript: workflow selection bubble
    private static final By ALL_REMOVE_EMBEDDED_FONTS_BUBBLES = By.xpath(
            "//span[normalize-space()='" + WORKFLOW_NAME + "'][not(ancestor::li[@role='option'])]");

    // Bot prompt: asking to upload a file
    private static final By UPLOAD_INSTRUCTION = By.xpath("//*[contains(text(),\"" + UPLOAD_INSTRUCTION_TEXT + "\")]");

    // File upload
    private static final By FILE_INPUT = By.cssSelector("input[type='file']");

    private static final By ALL_ATTACHED_FILE_BUBBLES = By.xpath("//span[contains(text(),'file attached')]");

    private static final By ALL_COMPLETED_MESSAGES = By.xpath(
            "//p[contains(.,'Request ID') and contains(.,'has been completed')]");

    private static final By COMPLETED_DOWNLOAD_LINK = By.xpath(
            "(//p[contains(.,'Request ID') and contains(.,'has been completed')]//a[normalize-space()='Download'])[last()]");

    // Continue dialog: scoped by the CONTINUE dialog's own wording
    private static final String CONTINUE_ANCHOR = "//*[contains(text(),'Would you like to continue with " + CONTINUE_WORKFLOW_NAME + "')]";

    private static final By CONTINUE_CONFIRMATION = By.xpath("(" + CONTINUE_ANCHOR + ")[last()]");

    private static final By CONTINUE_YES = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='YES' or normalize-space()='Yes'])[last()]");

    private static final By CONTINUE_NO = By.xpath("(" + CONTINUE_ANCHOR + "/ancestor::div" + "//button[normalize-space()='NO' or normalize-space()='No'])[last()]");

    // State
    private int bubbleCountBeforeSelection = 0;
    private int attachedBubbleBaseline = 0;
    private int completedMessageBaseline = 0;
    private final List<String> requestIds = new ArrayList<>();

    private final IdsDropdownHelper dropdown;
    private final ContinueDialogHelper continueDialog;

    public removeEmbeddedFontsMenuPage(WebDriver driver) {
        super(driver);
        this.dropdown = new IdsDropdownHelper(driver);
        this.continueDialog = new ContinueDialogHelper(driver);
    }

    //  Sidebar actions

    public void clickIdsDropdown() {
        dropdown.clickIdsDropdown();
    }

    public void selectRemoveEmbeddedFonts() {
        bubbleCountBeforeSelection = driver.findElements(ALL_REMOVE_EMBEDDED_FONTS_BUBBLES).size();

        for (int attempt = 1; attempt <= MAX_SELECTION_ATTEMPTS; attempt++) {
            boolean clicked = tryClickRemoveEmbeddedFontsOption();
            if (clicked && newBubbleAppearedWithin(BUBBLE_RETRY_PROBE_TIMEOUT)) {
                return;
            }

            if (attempt == MAX_SELECTION_ATTEMPTS) {
                break;
            }

            Log.warn("No '{}' bubble within {}s of attempt {}/{} - forcing the dropdown closed and " +
                            "reopening before retrying (known app-side glitch can replay a different IDS " +
                            "workflow instead of switching)",
                    WORKFLOW_NAME, BUBBLE_RETRY_PROBE_TIMEOUT.getSeconds(), attempt, MAX_SELECTION_ATTEMPTS);
            dropdown.closeDropdown();
            sleepUninterruptibly(RETRY_SETTLE_BUFFER);
            dropdown.clickIdsDropdown();
        }
    }

    private boolean tryClickRemoveEmbeddedFontsOption() {
        try {
            new WebDriverWait(driver, OPTION_VISIBLE_TIMEOUT).until(ExpectedConditions.visibilityOfElementLocated(REMOVE_EMBEDDED_FONTS_OPTION));

            sleepUninterruptibly(RETRY_SETTLE_BUFFER);

            WebElement option = new WebDriverWait(driver, QUICK_PROBE_TIMEOUT)
                    .until(ExpectedConditions.elementToBeClickable(REMOVE_EMBEDDED_FONTS_OPTION));
            Log.info("Clicking IDS dropdown option with text: '{}'", option.getText());
            safeClick(option);
            Log.info("Selected '{}' from the IDS dropdown", WORKFLOW_NAME);
            return true;
        } catch (TimeoutException e) {
            Log.warn("'{}' option did not become visible/clickable within {}s", WORKFLOW_NAME, OPTION_VISIBLE_TIMEOUT.getSeconds());
            return false;
        }
    }

    private boolean newBubbleAppearedWithin(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(d -> d.findElements(ALL_REMOVE_EMBEDDED_FONTS_BUBBLES).size() > bubbleCountBeforeSelection);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void verifyIdsDropdownAvailableAfterSelection() {
        dropdown.verifyDropdownAvailableAfterSelection(REMOVE_EMBEDDED_FONTS_OPTION, WORKFLOW_NAME, QUICK_PROBE_TIMEOUT);
    }

    public void verifyUploadInstructionShown() {
        Assert.assertTrue(waitVisible(UPLOAD_INSTRUCTION).isDisplayed(),
                "Upload instruction is not displayed for " + WORKFLOW_NAME);
        Log.pass("Upload instruction displayed for {}", WORKFLOW_NAME);
    }

    // File upload & submission

    public void baselineExistingState() {
        attachedBubbleBaseline = driver.findElements(ALL_ATTACHED_FILE_BUBBLES).size();
        completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();
        Log.info("Baselined transcript - {} existing 'file attached' bubble(s), {} existing 'completed' message(s)",
                attachedBubbleBaseline, completedMessageBaseline);
    }

    public void uploadFile(Path file) {
        sleepUninterruptibly(RETRY_SETTLE_BUFFER);
        WebElement input = new WebDriverWait(driver, QUICK_PROBE_TIMEOUT)
                .until(ExpectedConditions.presenceOfElementLocated(FILE_INPUT));
        input.sendKeys(file.toAbsolutePath().toString());
        Log.info("Uploaded file: {}", file.getFileName());
    }

    public void verifyFileAttachedBubble(String fileName) {
        try {
            new WebDriverWait(driver, UI_TIMEOUT)
                    .until(d -> d.findElements(ALL_ATTACHED_FILE_BUBBLES).size() > attachedBubbleBaseline);
        } catch (TimeoutException e) {
            throw new TimeoutException("No NEW 'file attached' bubble appeared after uploading " + fileName, e);
        }
        List<WebElement> bubbles = driver.findElements(ALL_ATTACHED_FILE_BUBBLES);
        String lastBubbleText = bubbles.get(bubbles.size() - 1).getText();
        Assert.assertTrue(lastBubbleText.contains(fileName),
                "Attached-file bubble does not contain '" + fileName + "'. Actual: " + lastBubbleText);
        attachedBubbleBaseline = bubbles.size();
        Log.pass("'{}' attached and shown in transcript", fileName);
    }

    public void clickSubmitButton() {
        safeClick(By.xpath("//*[name()='svg' and @data-testid='SendOutlinedIcon']"));
        Log.info("Submitted the query");
    }

    public String awaitSubmissionAcknowledgement() {
        Log.info(" Validating {} submission ", WORKFLOW_NAME);

        String requestId = waitForNewCompletion();
        Assert.assertTrue(requestId.matches("\\d+"), "Request ID should be numeric but was: " + requestId);
        requestIds.add(requestId);

        WebElement downloadLink = waitVisible(COMPLETED_DOWNLOAD_LINK);
        Assert.assertTrue(downloadLink.isDisplayed(),
                "Completed message for Request ID " + requestId + " is missing its 'Download' link");

        Instant beforeClick = Instant.now().minusSeconds(CLOCK_SKEW_SLACK_SECONDS);
        safeClick(downloadLink);
        Log.info("Clicked the 'Download' link for Request ID {}", requestId);

        Path downloaded = awaitDownloadedFile(requestId, beforeClick);
        Log.pass("Task completed - ID {}, downloaded {} ({} KB)", requestId, downloaded.getFileName(), DownloadFileHelper.sizeInKb(downloaded));

        return requestId;
    }

    private String waitForNewCompletion() {
        long start = System.currentTimeMillis();
        try {
            String requestId = new WebDriverWait(driver, BACKEND_TIMEOUT).until(d -> {
                List<WebElement> completed = d.findElements(ALL_COMPLETED_MESSAGES);
                if (completed.size() <= completedMessageBaseline) {
                    return null;
                }
                return parseRequestId(completed.get(completed.size() - 1).getText());
            });

            long elapsedMillis = System.currentTimeMillis() - start;
            Log.info("Backend completed the request in {} ms ({}s)", elapsedMillis, elapsedMillis / 1000);
            if (elapsedMillis > EXPECTED_ACK_SECONDS * 1000) {
                Log.warn("Completion took {}s - above the expected {}s ceiling", elapsedMillis / 1000, EXPECTED_ACK_SECONDS);
            }

            completedMessageBaseline = driver.findElements(ALL_COMPLETED_MESSAGES).size();
            return requestId;

        } catch (TimeoutException e) {
            throw new TimeoutException(
                    "Timed out after " + BACKEND_TIMEOUT.getSeconds()
                            + "s waiting for a NEW 'has been completed' message for " + WORKFLOW_NAME, e);
        }
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
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the Remove Embedded Fonts download", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed while checking for the downloaded file for Request ID " + requestId, e);
        }
    }

    public List<String> submittedRequestIds() {
        return List.copyOf(requestIds);
    }

    // Continue dialog

    public void clickContinueYes() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickYes(CONTINUE_YES, CONTINUE_WORKFLOW_NAME);

        waitVisible(UPLOAD_INSTRUCTION);
        Log.pass("Upload prompt reappeared - ready for next input");
    }

    public void clickContinueNo() {
        continueDialog.waitForConfirmation(CONTINUE_CONFIRMATION);
        continueDialog.clickNo(CONTINUE_NO, CONTINUE_WORKFLOW_NAME);
        continueDialog.waitUntilNoButtonDisabled(CONTINUE_NO, CONTINUE_WORKFLOW_NAME);
    }

    // Private helpers

    private static void sleepUninterruptibly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.warn("Interrupted while waiting before retrying the '{}' selection", WORKFLOW_NAME);
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
