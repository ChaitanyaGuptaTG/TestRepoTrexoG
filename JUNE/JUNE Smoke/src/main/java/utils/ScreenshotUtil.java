package utils;

import org.openqa.selenium.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;

public class ScreenshotUtil {
	// this utility is created to take screenshot when TC Fail.

	public static String captureScreenshot(WebDriver driver, String testName) {

		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String screenshotPath = "test-output/screenshots/" + testName + "_" + timestamp + ".png";

		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			File destination = new File(screenshotPath);
			FileUtils.copyFile(source, destination);
		} catch (IOException e) {
			Log.error("Failed to capture screenshot: " + e.getMessage());
		}

		return screenshotPath;
	}

	public static void cleanScreenshotsDir() {
	    File dir = new File("test-output/screenshots");
	    if (dir.exists()) {
	        for (File file : dir.listFiles()) {
	            file.delete();
	        }
	    }
	}

	public static void deleteScreenshots(String folderPath) {
	    File folder = new File(folderPath);
	    if (folder.exists()) {
	        File[] files = folder.listFiles();
	        if (files != null) {
	            for (File file : files) {
	                file.delete();
	            }
	        }
	        System.out.println("Screenshots deleted successfully.");
	    }
	}

	public static String getRetryArtifactsDir(String intentName, String scenarioName) {
		String cleanIntent = intentName.replaceAll("[^a-zA-Z0-9_-]", "_");
		String cleanScenario = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
		if (ExecutionRunManager.isRunSpecificEnabled()) {
			return ExecutionRunManager.getRetryArtifactsDir().resolve(cleanIntent).resolve(cleanScenario).toAbsolutePath().toString();
		}
		return "test-output/RetryArtifacts/" + cleanIntent + "/" + cleanScenario;
	}

	public static String captureRetryScreenshot(WebDriver driver, String intentName, String scenarioName, String name) {
		String dirPath = getRetryArtifactsDir(intentName, scenarioName);
		new File(dirPath).mkdirs();
		String screenshotPath = dirPath + "/" + name + ".png";
		try {
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			File destination = new File(screenshotPath);
			FileUtils.copyFile(source, destination);
			System.out.println("✅ Saved retry screenshot to: " + screenshotPath);
		} catch (Exception e) {
			Log.error("Failed to capture retry screenshot: " + e.getMessage());
		}
		return screenshotPath;
	}

	public static void saveResponseTextLog(String intentName, String scenarioName, String logContent) {
		String dirPath = getRetryArtifactsDir(intentName, scenarioName);
		new File(dirPath).mkdirs();
		File logFile = new File(dirPath + "/ResponseLog.txt");
		try (java.io.FileWriter writer = new java.io.FileWriter(logFile, true)) {
			writer.write(logContent);
			writer.write("\n\n");
			System.out.println("✅ Saved retry response log to: " + logFile.getAbsolutePath());
		} catch (Exception e) {
			Log.error("Failed to save response text log: " + e.getMessage());
		}
	}

	public static void cleanRetryArtifactsDir() {
		File dir = new File(ExecutionRunManager.isRunSpecificEnabled()
				? ExecutionRunManager.getRetryArtifactsDir().toAbsolutePath().toString()
				: "test-output/RetryArtifacts");
		if (dir.exists()) {
			deleteFolder(dir);
		}
		File zip = new File(ExecutionRunManager.isRunSpecificEnabled()
				? ExecutionRunManager.getRunRoot().resolve("RetryArtifacts.zip").toAbsolutePath().toString()
				: "test-output/RetryArtifacts.zip");
		if (zip.exists()) {
			zip.delete();
		}
	}

	private static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}
}