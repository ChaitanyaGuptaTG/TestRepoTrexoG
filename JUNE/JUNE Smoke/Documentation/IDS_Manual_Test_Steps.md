    # IDS Manual Test Steps

Manual steps corresponding to the automated suite in `src/test/java/test/idsPageTest.java`.
Each `TC` below maps 1:1 to an `@Test` method in that file, in execution order, so anyone can
run the same flow by hand and compare results against the automation.

**Preconditions:** Logged into the application (valid username/password), landed on the main page.

---

## Section 1: 1449 and 892 Downloader

### TC1 – Select the Downloader
*(automation: `selectDocumentDownloader`)*

1. Click the June chat icon.
2. Click the "IDS" dropdown.
3. Select "1449 and 892 Downloader" from the list.
4. **Expected:** "1449 and 892 Downloader" label appears in the chat; an instruction to
   "enter the application numbers" is shown.

### TC2 – Submit a single application number
*(automation: `submitSingleAppNumberAndValidateZip`)*

1. Type one application number (e.g. `16999215`) into the query box.
2. Click Submit.
3. Wait for the completion message ("...has been completed") and Request ID.
4. Click the download link and let the zip download.
5. Open the zip and check:
   - Only PDF files are present.
   - There is a folder named after the application number.
   - At least one PDF exists under a `1449` subfolder and one under an `892` subfolder.
6. Look up the same application number via the USPTO API and count its `1449` and `892`
   documents (`documentCode` field in the `documentBag` array).
7. **Expected:** Number of `1449` PDFs in the zip == number of `1449` docs from the API;
   same for `892`. A mismatch is a failure.

### TC3 – Continue and submit two application numbers
*(automation: `continueAndSubmitMultipleAppNumbers`)*

1. Click "Yes" on the "Would you like to continue?" prompt.
2. Enter two application numbers on separate lines (e.g. `17954142`, `18139333`).
3. Submit, download the zip, and repeat the same zip/API count check (TC2, steps 5-7) per
   application number.
4. **Expected:** Both applications' zips contain correct `1449`/`892` PDFs, counts match the API.

### TC4 – Decline and exit
*(automation: `declineToContinue`)*

1. Click "No" on the continue prompt.
2. **Expected:** Chat returns to the default "Enter your query..." prompt (workflow exited).

### TC5 – Re-enter downloader by typing intent
*(automation: `reEnterDownloaderByTypingIntent`)*

1. In the query box, type "1449 and 892 Downloader" (instead of using the dropdown) and submit.
2. **Expected:** The "enter application numbers" instruction reappears.

### TC6 – Submit a second single application number
*(automation: `submitSecondSingleAppNumberAndValidateZip`)*

1. Enter one application number (e.g. `18210779`), submit, download the zip.
2. Repeat the zip/API count validation as in TC2.
3. **Expected:** Same pass criteria as TC2.

### TC7 – Exit before moving to Reference Count
*(automation: `exitDownloaderBeforeReferenceCount`)*

1. Click "No" on the continue prompt.
2. **Expected:** Workflow exits cleanly.

---

## Section 2: Reference Count

### TC8 – Select Reference Count
*(automation: `selectReferenceCount`)*

1. Open the IDS dropdown again.
2. Select "Reference Count".
3. **Expected:** "Reference Count" label/bubble appears; a new "enter application numbers"
   instruction is shown.

### TC9 – Submit a single application number
*(automation: `submitSingleAppNumberToReferenceCount`)*

1. Enter one application number (e.g. `18139333`) and submit.
2. Observe which acknowledgement message appears — **the backend decides this, not the
   tester**, so both outcomes below are valid and must be handled:

   **Case A — Async ("submitted", not yet done)**
   Message reads: *"Your request with ID `<RequestID>` is now submitted. You can click here
   to view its current status. Once completed, you will also receive a notification in your
   inbox."*

   - 3a. Confirm the "click here" text is present and shows as clickable (pointer cursor).
   - 4a. Click **"click here"**.
   - 5a. **Expected:** Page navigates to the request-status view, showing a row filtered/
     searched to `<RequestID>`.
   - 6a. Click the **refresh icon** (top-right of the status panel).
   - 7a. **Expected:** The search box clears and the full "Today" list of requests reloads
     (all rows, not just the filtered one).
   - 8a. Find the row matching `<RequestID>` and click its **dropdown (chevron)** icon to
     expand it (skip this click if the row is already showing its expanded fields — it can
     stay expanded across a refresh).
   - 9a. **Expected:** Expanded row shows `Task Type: IDS`, `Request ID: <RequestID>`,
     `Status: Pending` or `Success`, `Request Time: <date>, <time>`, `Subtask: Reference Count`.
   - 10a. Read the `Status` value:
     - **If `Success`:** a download icon is present on the row. Click it.
       **Expected:** a new, non-empty file appears in the downloads folder within ~30s.
     - **If `Pending`:** click the refresh icon **once more**, wait a few seconds, then
       re-expand and re-read the row's `Status`.
       - **Now `Success`:** same as above — click the download icon, expect a new file.
       - **Still `Pending`:** this is a known, acceptable outcome — the backend is just
         taking longer than usual. Do **not** treat this as a failure; skip the download
         for this Request ID and move on (its completion isn't verified further here).
       - **Anything other than `Success`/`Pending`:** treat as a genuine failure.
   - 11a. Click **"Back to IP Assistant Chat"** (top-left) — do this regardless of which
     Status outcome above applied.
   - 12a. **Expected:** Returns to the June chat, ready for the "Would you like to continue?"
     prompt.

   **Case B — Synchronous (already completed)**
   Message reads two lines: *"The total references count is `<N>`."* then *"Your task with
   Request ID `<RequestID>` has been completed. You can download the file using the
   following link: Download. An email notification has also been sent for your reference."*

   - 3b. Click the **"Download"** link inside the message.
   - 4b. **Expected:** A new, non-empty file appears in the downloads folder within ~30s.

3. **Expected (both cases):** The extracted Request ID is numeric.

### TC10 – Continue and submit two application numbers
*(automation: `continueAndSubmitMultipleToReferenceCount`)*

1. Click "Yes" to continue.
2. Enter two application numbers on separate lines and submit.
3. Repeat the branching check from TC9 (Case A or Case B) for this new submission's own
   Request ID.
4. **Expected:** A new Request ID is returned/verified for this submission, following
   whichever case applies.

### TC11 – Verify distinct Request IDs
*(automation: `referenceCountRequestIdsAreDistinct`)*

1. Compare the Request ID from TC9 and TC10.
2. **Expected:** Two Request IDs total, both different from each other (no duplicates).

### TC12 – Decline and exit Reference Count
*(automation: `declineToContinueReferenceCount`)*

1. Click "No" on the continue prompt.
2. **Expected:** Workflow exits.

---

## Section 3: Reference Downloader

### TC13 – Select Reference Downloader
*(automation: `selectReferenceDownloader`)*

1. Open the IDS dropdown.
2. Select "Reference Downloader".
3. **Expected:** "Reference Downloader" bubble appears; a new "enter reference numbers"
   instruction is shown.

### TC14 – Submit a single reference number
*(automation: `submitSingleRefNumberToReferenceDownloader`)*

1. Enter one reference/patent number (e.g. `US1234567A`) and submit.
2. Wait for task completion.
3. **Expected:** A numeric Request ID is returned, and the download link's URL contains
   that Request ID.

### TC15 – Continue and submit a batch of reference numbers
*(automation: `continueAndSubmitBatchToReferenceDownloader`)*

1. Click "Yes" to continue.
2. Enter three reference numbers on separate lines (e.g. `US4683202A`, `DE102015013053A1`,
   `AU2010219336A1`) and submit.
3. Wait for the outcome.
4. **Expected:** Either:
   - the task completes and the download link contains the Request ID, **or**
   - the request is acknowledged as queued/async (e.g. for `DE102015013053A1`) — in that
     case completion is checked separately/outside this flow (e.g. via Collab).

### TC16 – Verify distinct Request IDs
*(automation: `referenceDownloaderRequestIdsAreDistinct`)*

1. Compare the Request ID from TC14 and TC15.
2. **Expected:** Two Request IDs total, both distinct.

### TC17 – Decline and exit Reference Downloader
*(automation: `declineToContinueReferenceDownloader`)*

1. Click "No" on the continue prompt.
2. **Expected:** Workflow exits, chat returns to the default prompt.

---

## Notes on the Reference Count status-page and download automation (TC9/TC10)

The automated equivalent of Case A lives in `referenceCountMenuPage.java`
(`followAsyncStatusFlow` → `awaitSuccessStatusAndDownload` → `downloadFromStatusRow`);
Case B is `downloadFromCompletedMessage`. Both funnel into the shared `awaitDownloadedFile`
helper, which watermarks the downloads folder just before the click and waits (up to 30s)
for a new, non-empty, non-partial file to appear. Locators, confirmed against the live DOM:

- **"click here" status link** — `<span style="cursor:pointer">click here</span>` inside
  the `"...is now submitted..."` message span.
- **Refresh icon** — `data-testid="CachedIcon"` (MUI's Cached icon, not a "Refresh"-named
  one — an earlier guess of `RefreshOutlinedIcon`/`RefreshIcon` was wrong and timed out).
- **Row expand/collapse chevron** — `data-testid="KeyboardArrowDownOutlinedIcon"`, one per
  row. Once clicked, the row stays expanded across a refresh — clicking it again on a
  later check fails (no such element), so the automation only expands a row that isn't
  already showing its `Subtask:` field.
- **Row fields** — `Task Type: <span>IDS</span>`, `Request ID: <span>{id}</span>`,
  `Status: <span>Pending|Success</span>` visible on the collapsed row; expanding adds
  `Request Time:`, `Completion Time:`, and `Subtask: <span>Reference Count</span>`.
- **Row download icon** — `data-testid="FileDownloadOutlinedIcon"`, present on a row only
  once its `Status` is `Success` (absent while `Pending`).
- **Search box** — `<input placeholder="Search">`; pre-filled with the Request ID right
  after the "click here" redirect, and cleared once "today's full list" reloads after
  clicking refresh.
- **Back to IP Assistant Chat** — `<p>` containing the `ArrowBackIosNewIcon` svg and the
  text "Back to IP Assistant Chat". Clicked unconditionally at the end of the async flow,
  whether the row ended up `Success` (downloaded) or still `Pending` (skipped) — this is
  what keeps a slow backend from stranding the browser on the status page and cascading
  failures into every test after it.
- **Sync completion "Download" link** — `<a>Download</a>` inside the completed-message
  `<p>`. That `<p>` is two sentences ("The total references count is N." then "Your task
  with Request ID... has been completed..."), so it has multiple text-node children (split
  further by the nested `<a>`). Its locator uses `contains(., ...)` (whole-element string
  value), not `contains(text(), ...)` — the latter only tests XPath 1.0's *first* text node
  under a multi-node coercion, so it silently never matched this two-sentence shape and
  every synchronous completion fell through to a pointless retry-resubmit.

If the UI changes any of these, the fix is isolated to the constants/helper methods in
`referenceCountMenuPage.java` — no other file needs to change.

## Notes on Reference Downloader's transcript bubble (TC13)

`REFERENCE_DOWNLOADER_BUBBLE` in `referenceDownloaderMenuPage.java` is anchored on the
option's `aria-label` (`"Download references in bulk."` + text `"Reference Downloader"`)
rather than excluding `<li>` ancestors — the transcript bubble can itself sit under a
`<li>` in the chat's message list, which made an ancestor-exclusion locator intermittently
exclude the real bubble and time out (the same failure mode already fixed this way for
Reference Count's own bubble, see `REFERENCE_COUNT_BUBBLE` above).
