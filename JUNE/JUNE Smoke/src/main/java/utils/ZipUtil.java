package utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static String zipScreenshots(String sourceDirPath) {
		// This utility is created to zip failed screenshot and send over email
		// Author : Yash Shrivastava
		String zipFilePath = "test-output/screenshots.zip";

		try (FileOutputStream fos = new FileOutputStream(zipFilePath); ZipOutputStream zos = new ZipOutputStream(fos)) {

			File sourceDir = new File(sourceDirPath);
			File[] files = sourceDir.listFiles();

			if (files == null || files.length == 0) {
				return null; // No screenshots
			}

			for (File file : files) {
				if (file.isFile()) {
					try (FileInputStream fis = new FileInputStream(file)) {
						ZipEntry zipEntry = new ZipEntry(file.getName());
						zos.putNextEntry(zipEntry);

						byte[] buffer = new byte[1024];
						int length;
						while ((length = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, length);
						}

						zos.closeEntry();
					}
				}
			}

			Log.info("✅ Screenshots zipped successfully");

		} catch (IOException e) {
			Log.error("Error zipping screenshots: " + e.getMessage());
		}

		return zipFilePath;
	}

	public static String zipDirectory(String sourceDirPath, String zipFilePath) {
		try {
			File sourceDir = new File(sourceDirPath);
			if (!sourceDir.exists()) {
				Log.warn("⚠️ Source directory does not exist: " + sourceDirPath);
				return null;
			}
			try (FileOutputStream fos = new FileOutputStream(zipFilePath);
					ZipOutputStream zos = new ZipOutputStream(fos)) {
				zipFileOrDirectory(sourceDir, sourceDir, zos);
				Log.info("✅ Directory zipped successfully: " + zipFilePath);
				return zipFilePath;
			}
		} catch (IOException e) {
			Log.error("Error zipping directory " + sourceDirPath + ": " + e.getMessage());
			return null;
		}
	}

	private static void zipFileOrDirectory(File rootDir, File file, ZipOutputStream zos) throws IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					zipFileOrDirectory(rootDir, child, zos);
				}
			}
		} else {
			byte[] buffer = new byte[1024];
			String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();
			try (FileInputStream fis = new FileInputStream(file)) {
				zos.putNextEntry(new ZipEntry(relativePath));
				int length;
				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
			}
		}
	}
}