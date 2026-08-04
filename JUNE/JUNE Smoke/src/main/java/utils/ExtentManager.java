package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {

	private static ExtentReports extent;

	public static synchronized ExtentReports getInstance() {
		// this java utility create a custom Extent .html report
		// Author : Yash Shrivastava.

		if (extent == null) {
			ExtentSparkReporter reporter = new ExtentSparkReporter("test-output/ExtentReport.html");

			reporter.config().setReportName("JUNE Automation Test Report");
			reporter.config().setDocumentTitle("Selenium Execution Report");

			extent = new ExtentReports();
			extent.attachReporter(reporter);

			extent.setSystemInfo("OS", System.getProperty("os.name"));
			extent.setSystemInfo("QA", "Yash  Shrivastava");
			extent.setSystemInfo("Browser", "Chrome");
		}
		return extent;
	}
}
