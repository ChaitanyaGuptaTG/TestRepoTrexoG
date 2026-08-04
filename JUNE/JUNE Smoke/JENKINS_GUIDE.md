# Trexo Automation - Jenkins Integration & Scheduling Guide

This guide describes how to configure, schedule, and run this TestNG/Maven automation project in a Jenkins environment.

---

## 1. Prerequisites on Jenkins Controller/Agent

Ensure that your Jenkins agent has the following installed and configured:
1. **Java JDK 11** (or higher) installed and set in environment variables (`JAVA_HOME`).
2. **Maven**: Optional, but recommended to have Maven configured in Jenkins (via **Manage Jenkins** -> **Global Tool Configuration**). If not configured, the scripts will automatically default to using the bundled Maven distribution (`apache-maven-3.9.12`) included in this repository.
3. **Browser Drivers**: The project uses `webdrivermanager` which automatically resolves browser drivers (e.g. ChromeDriver) at runtime. Ensure the agent has an internet connection to download these drivers, or that appropriate drivers are available in the system path.
4. **Browsers**: The target browsers (Chrome, Firefox, etc.) must be installed on the agent. If running in non-headless mode, the agent must have a GUI environment configured. We recommend using `HEADLESS=true` for standard server-based agents.

---

## 2. Setting Up a Parameterized Pipeline Job

We recommend using a **Pipeline** job to run the declarative pipeline defined in the `Jenkinsfile`.

### Step-by-Step Setup:
1. Open Jenkins and click on **New Item**.
2. Enter a name (e.g., `Trexo-Automation-Smoke-Tests`) and select **Pipeline**. Click **OK**.
3. Under the **General** section:
   - Check **This project is parameterized**.
   - Add the following parameters (these match the parameters defined in the `Jenkinsfile` so Jenkins can read them):
     - **Choice Parameter**:
       - Name: `BROWSER`
       - Choices: `chrome`, `firefox`, `edge`, `safari` (each on a new line)
       - Default Value: `chrome`
       - Description: `Browser for automation execution`
     - **Choice Parameter**:
       - Name: `TEST_ENV`
       - Choices: `qa`, `uat`, `prod` (each on a new line)
       - Default Value: `qa`
       - Description: `Environment to run tests against`
     - **Boolean Parameter**:
       - Name: `HEADLESS`
       - Default Value: Checked (`true`)
       - Description: `Run tests in headless mode`
     - **String Parameter**:
       - Name: `SUITE_XML_FILE`
       - Default Value: `testng.xml`
       - Description: `TestNG Suite XML file to execute`
4. Under the **Pipeline** section:
   - Definition: Select **Pipeline script from SCM**.
   - SCM: Select **Git**.
   - Repository URL: Enter the URL of your Git repository.
   - Credentials: Select the appropriate credentials to pull from the repository.
   - Branch Specifier: Enter the branch (e.g., `*/main` or `*/master`).
   - Script Path: Enter `Jenkinsfile` (the default).
5. Click **Save**.

---

## 3. Scheduled / Cron Execution

The project's schedule is defined directly inside the [Jenkinsfile](file:///c:/Users/YashShrivastava%28Trex/Trexo-Automation/trexo-selenium%20-%20JUNE/JUNE%20Smoke/JUNE/JUNE%20Smoke/Jenkinsfile) via the `triggers` block:

```groovy
triggers {
    // Runs every weekday (Monday through Friday) at 1:00 AM (Server Time)
    cron('0 1 * * 1-5')
}
```

### Changing the Schedule:
If you want to change the schedule, you can edit the cron expression in the `Jenkinsfile` and commit the changes to Git. 
Common cron examples:
- **Every day at midnight**: `cron('0 0 * * *')`
- **Every weekday at 6:00 PM**: `cron('0 18 * * 1-5')`
- **Every Saturday morning at 3:00 AM**: `cron('0 3 * * 6')`

> **Note**: After committing the `Jenkinsfile` with the cron trigger, you need to run the Jenkins job manually **at least once** so that Jenkins can scan the file and register the schedule trigger.

---

## 4. Recommended Jenkins Plugins

To enhance the display of results in Jenkins, install the following plugins:

### 1. TestNG Results Plugin
- **Purpose**: Displays a graphical trend chart and detailed breakdown of passed, failed, and skipped TestNG tests directly in Jenkins.
- **How to view results**: Once installed, the pipeline will execute the `testng()` post-action. You will see a **TestNG Results** link in the sidebar of the Jenkins job page.

### 2. HTML Publisher Plugin
- **Purpose**: Displays the ExtentReports HTML report directly inside Jenkins.
- **How to configure**: If you wish to display reports in the Jenkins UI, add a step under the `post { always { ... } }` block in the `Jenkinsfile`:
  ```groovy
  publishHTML([
      allowMissing: true,
      alwaysLinkToLastBuild: true,
      keepAll: true,
      reportDir: 'test-output',
      reportFiles: 'ExtentReport.html', // Replace with the actual Extent Report filename
      reportName: 'Extent Report'
  ])
  ```

---

## 5. Alternative: Running via Freestyle Job (using Runner Scripts)

If you are using a Jenkins **Freestyle project** instead of a Pipeline:
1. In the job configuration, add a **Build Step** based on your OS:
   - **Windows Agent**: Select **Execute Windows batch command** and run:
     ```cmd
     run_tests.bat browser=chrome env=qa headless=true suite=testng.xml
     ```
   - **Linux/Unix Agent**: Select **Execute shell** and run:
     ```bash
     chmod +x run_tests.sh
     ./run_tests.sh browser=chrome env=qa headless=true suite=testng.xml
     ```
2. Schedule this job using the **Build Triggers** section -> **Build periodically** and enter your desired cron expression.
