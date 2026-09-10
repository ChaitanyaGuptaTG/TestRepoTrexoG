# JUNE Smoke — Project Structure

Generated 2026-09-03 (re-verified). Reflects the working tree on branch `codeimpone`.

A Selenium + TestNG UI automation suite for the June chat application (login, help pages,
IDS document workflows, bibliographic data extraction), with Extent reporting, a
lightweight REST API test layer under `src/main/java/api` (USPTO Patent Application
Documents API), and a JMeter performance-test hook wired into the Maven build.

## Repository root

```
JUNE Smoke/
├── src/                              Application + test source (see full tree below)
├── source/                           Test-data fixtures (Excel scenario sheets, TM image zips)
├── Documentation/                    Phase write-ups (.docx) + IDS_Manual_Test_Steps.md
├── reports/                          Ad-hoc saved Extent HTML reports
├── output/                           Runtime output root (comparison/, generated/, incoming/) — gitignored except .gitkeep
├── ExecutionResults/                 Per-run execution artifacts (Run_YYYYMMDD_HHMMSS/{Downloads,Logs,ComparisonReports}) — generated, not gitignored
├── logs/                             application.log etc. — gitignored
├── test-output/                      TestNG/Extent output + screenshots — gitignored
├── .github/                          java-upgrade/, modernize/ — GitHub Actions workflow assets
├── .claude/                          Claude Code session state (scheduled_tasks.lock)
├── .idea/, .settings/, .vscode/,     IDE project metadata — .idea and Eclipse files gitignored
│   .project, .classpath
├── .metadata/                        Eclipse workspace metadata — gitignored
├── apache-maven-3.9.12-bin/          Bundled/extracted local Maven distribution
├── apache-maven-3.9.16-bin.tar.gz(.1) Untracked downloaded Maven archives (not extracted, not gitignored yet)
├── pom.xml                           Maven build — Selenium, TestNG, ExtentReports, WebDriverManager,
│                                     Gson, SLF4J/Logback, POI, JMeter (jmeter-maven-plugin), ...
├── testng.xml                        Main suite — registers idsPageTest ("JUNESuite")
├── testng-login.xml, login-testng.xml, testNGQA.xml, testng-temp.xml, probe-testng.xml
│                                     Additional/scratch TestNG suite definitions
├── run_tests.sh, run_tests.bat       Local test-run launcher scripts
├── Jenkinsfile, JENKINS_GUIDE.md     CI pipeline + setup notes
├── .mcp.json                         MCP server config for this workspace
├── .gitignore
├── BRANCH_CHANGE_REPORT.md / .pdf    Branch diff/change report exports
├── Commands.docx                     Notes/command reference
├── PROJECT_STRUCTURE.md              This file
└── Test Results - ....html           Saved TestNG HTML report export
```

## `src/` — full source tree

```
src/
├── main/
│   ├── java/
│   │   ├── api/                                 REST API test layer (separate from the UI page objects)
│   │   │   ├── auth/                             (empty package dir — reserved, not yet populated)
│   │   │   ├── clients/
│   │   │   │   └── UsptoDocumentClient.java      Java 11 HttpClient wrapper for the USPTO Patent
│   │   │   │                                     Application Documents API; builds from ConfigReader
│   │   │   │                                     via UsptoDocumentClient.fromConfig()
│   │   │   ├── config/                           (empty package dir — reserved)
│   │   │   ├── constants/                        (empty package dir — reserved)
│   │   │   ├── models/                           (empty package dir — reserved)
│   │   │   │   ├── request/                      (empty — reserved)
│   │   │   │   └── response/                     (empty — reserved)
│   │   │   └── utils/                            (empty package dir — reserved)
│   │   ├── pages/                                Page Object Model
│   │   │   ├── BasePage.java
│   │   │   ├── DashboardPage.java
│   │   │   ├── helpPage.java
│   │   │   ├── IntentTemplatePage.java
│   │   │   ├── JuneChatPage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── LogoutPage.java
│   │   │   ├── bibliographic_data_extraction/
│   │   │   │   ├── AUPatentAllScenariosPage.java
│   │   │   │   ├── AUTrademarkAllScenariosPage.java
│   │   │   │   ├── EPPatentAllScenariosPage.java
│   │   │   │   ├── GlobalPatentAllScenariosPage.java
│   │   │   │   ├── USPatentAllScenariosPage.java
│   │   │   │   ├── USTMImageDownloadPage.java
│   │   │   │   └── USTrademarkAllScenariosPage.java
│   │   │   ├── help/
│   │   │   │   ├── FAQsPage.java
│   │   │   │   ├── FeedbackPage.java
│   │   │   │   ├── HelpMenuPage.java
│   │   │   │   ├── HelpPage.java
│   │   │   │   ├── ReleaseNotesPage.java
│   │   │   │   ├── TutorialPage.java
│   │   │   │   └── data/                         Static FAQ content per help topic
│   │   │   │       ├── AppGenFAQData.java
│   │   │   │       ├── BibliographicFAQData.java
│   │   │   │       ├── ClaimsFormatterFAQData.java
│   │   │   │       ├── DocumentGenerationFAQData.java
│   │   │   │       ├── FAQEntry.java
│   │   │   │       ├── GettingStartedFAQData.java
│   │   │   │       ├── IDSFAQData.java
│   │   │   │       ├── OAShellDraftFAQData.java
│   │   │   │       ├── OathDecAdsFAQData.java
│   │   │   │       ├── PatentFileWrapperDownloaderData.java
│   │   │   │       ├── ReleaseNotesData.java
│   │   │   │       └── SupportHelpFAQData.java
│   │   │   └── IDSPage/                          1449/892 Downloader + Reference Count/Downloader workflows
│   │   │       ├── idsMenuPage.java              Downloader page object (submit, download, verify zip)
│   │   │       ├── idsPage.java                  Facade combining menu + Reference Count + Reference
│   │   │       │                                 Downloader + help menu (moved here from pages/IDEPage/)
│   │   │       ├── referenceCountMenuPage.java   Reference Count page object
│   │   │       └── referenceDownloaderMenuPage.java  Reference Downloader page object (new)
│   │   └── utils/
│   │       ├── ConfigReader.java
│   │       ├── DataComparisonReportStore.java
│   │       ├── DriverManager.java
│   │       ├── EmailUtil.java
│   │       ├── ExcelComparisonEngine.java
│   │       ├── ExcelDataComparator.java
│   │       ├── ExcelHeaderValidator.java
│   │       ├── ExecutionRetentionManager.java
│   │       ├── ExecutionRunManager.java
│   │       ├── ExtentManager.java
│   │       ├── ExtentTestManager.java
│   │       ├── ImageComparisonEngine.java
│   │       ├── ImageComparisonReportStore.java
│   │       ├── Log.java                          Now backed by SLF4J/Logback (see logging/)
│   │       ├── OutputFileWorkflowManager.java
│   │       ├── PlatformRetryReportStore.java
│   │       ├── PlatformRetryUtil.java
│   │       ├── ScreenshotUtil.java
│   │       ├── TableUtil.java
│   │       ├── WaitUtils.java
│   │       ├── ZipUtil.java
│   │       └── logging/
│   │           └── TestLogBuffer.java            Per-thread log buffer attached to Extent reports
│   └── resources/
│       ├── extent-custom.css (+ stray "extent-custom.css " dupe with trailing space)
│       ├── extent-custom.js  (+ stray "extent-custom.js " dupe with trailing space)
│       └── extent.properties
└── test/
    ├── java/
    │   ├── base/
    │   │   └── BaseTest.java                     Driver lifecycle; @Listeners(ExtentTestListener)
    │   ├── listeners/
    │   │   ├── ExtentTestListener.java            Extent reporting, failure screenshots, retry/data-validation sections
    │   │   ├── RetryAnalyzer.java
    │   │   └── ScreenshotOnFailureListener.java
    │   └── test/
    │       ├── AbstractWorkflowTest.java
    │       ├── ImageComparisonTest.java
    │       ├── IntentTemplateTest.java
    │       ├── LoginTest.java
    │       ├── LogoutTest.java
    │       ├── ScenarioConfig.java
    │       ├── helpPageTest.java
    │       ├── idsPageTest.java                  Downloader + Reference Count end-to-end suite
    │       ├── UsptoApiTest.java                 Pure API tests (no browser) against UsptoDocumentClient;
    │       │                                     runs independently of idsPageTest
    │       └── bibliographic_data_extraction/
    │           ├── AUPatentAllScenariosTest.java
    │           ├── AUTrademarkAllScenariosTest.java
    │           ├── EPPatentAllScenariosTest.java
    │           ├── GlobalPatentAllScenariosTest.java
    │           ├── USPatentAllScenariosTest.java
    │           ├── USTMImageDownloadTest.java
    │           └── USTrademarkAllScenariosTest.java
    ├── jmeter/                                    Empty — reserved for jmeter-maven-plugin test plans (.jmx),
    │                                               not yet populated, untracked (empty dirs aren't git-tracked)
    └── resources/
        ├── config-local.properties               gitignored (local overrides)
        ├── config-prod.properties
        ├── config-qa.properties                  Updated (USPTO API base URL/key entries)
        ├── config-uat.properties
        ├── log4j2.xml
        ├── logback.xml                            Logback config backing utils.Log / SLF4J
        ├── api/
        │   ├── payloads/assistant/
        │   ├── schemas/assistant/
        │   └── testdata/
        └── data-verification/
            └── README.md
```

## Notes

- `pages.IDSPage.idsPage` is a facade: `idsPageTest` never touches `idsMenuPage`/`referenceCountMenuPage`/
  `referenceDownloaderMenuPage` directly, it goes through `idsPage`. The facade and its page objects moved
  from `pages/IDEPage/` to `pages/IDSPage/` (git-tracked as a rename).
- New USPTO REST API layer: `api/clients/UsptoDocumentClient.java` (Java 11 `HttpClient`, Gson parsing,
  config-driven via `ConfigReader`) is exercised by `test/UsptoApiTest.java`, a browser-free TestNG class
  that runs standalone. The sibling `api/{auth,config,constants,models,utils}` package dirs exist but are
  currently empty — reserved for future growth of this layer.
- Logging moved onto SLF4J + Logback: `pom.xml` now pulls `slf4j-api` and `logback-classic`, `utils/Log.java`
  is Logback-backed, and `src/test/resources/logback.xml` configures it (alongside the pre-existing `log4j2.xml`).
- `pom.xml` also added `gson` (JSON parsing for the API layer) and a JMeter performance-test hook
  (`ApacheJMeter_core`/`ApacheJMeter_java` deps + the `jmeter-maven-plugin` build plugin, goal `jmeter`).
  By default that plugin scans `src/test/jmeter` for `.jmx` test plans — the directory now exists but is
  still empty, so the `jmeter` build goal currently has nothing to run.
- Generated/output directories (`target/`, `logs/`, `test-output/`, `.idea/`, `.metadata/`, `.settings/`, `output/generated/`, `output/incoming/`) are gitignored. `ExecutionResults/` holds per-run artifacts but is currently **not** gitignored.
- Two untracked Maven distribution archives (`apache-maven-3.9.16-bin.tar.gz`, `.tar.gz.1`) sit at the repo
  root alongside the already-extracted `apache-maven-3.9.12-bin/` — likely scratch/downloaded, not yet
  cleaned up or gitignored.
- `Documentation/` gained `IDS_Manual_Test_Steps.md` alongside the existing phase `.docx` write-ups.
- Several TestNG suite XMLs exist beyond `testng.xml` (`testng-login.xml`, `login-testng.xml`, `testNGQA.xml`, `testng-temp.xml`, `probe-testng.xml`) — `testng.xml` is the one wired to run the full IDS suite.
