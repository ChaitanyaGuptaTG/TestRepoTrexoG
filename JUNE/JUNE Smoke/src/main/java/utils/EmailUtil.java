package utils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;


// mvn test "-Denv=qa" "-Dheadless=false" "-DsuiteXmlFile=testNGQA.xml"


public class EmailUtil {

	public static void sendReportEmail(boolean isBuildFailed) {
		sendReportEmail(isBuildFailed, null);
	}

	public static void sendReportEmail(boolean isBuildFailed, String retryZipPath) {
		// this java utility build for sending Extent Report over the email.
		// Author : Yash Shrivastava

		final String username = "noreply@trexoglobal.com";
		final String password = "q9m{Qz3pVaT[e!4KrELf";

		String reportPath = "test-output/ExtentReport.html";
		String screenshotsDir = "test-output/screenshots";
		String zipPath = null;
		if (isBuildFailed) {
			zipPath = ZipUtil.zipScreenshots(screenshotsDir);
		}

		// SMTP properties
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.office365.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		// email To and From
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("yash.shrivastava@trexoglobal.com"));
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("yash.shrivastava@trexoglobal.com"));
			message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(""));

			String subject;
			if (isBuildFailed) {
				subject = "❌ JUNE Smoke Test Automation Report - BUILD FAILED";
			} else {
				subject = "✅ JUNE Smoke Test Automation Report - BUILD PASSED";
			}
			message.setSubject(subject);

			// Email body
			BodyPart messageBodyPart = new MimeBodyPart();

			StringBuilder bodyText = new StringBuilder();
			bodyText.append("Hi Team,\n\n");
			if (isBuildFailed) {
				bodyText.append("Automation execution is completed and the BUILD HAS FAILED.\n\n");
			} else {
				bodyText.append("Automation execution is completed and the BUILD HAS PASSED successfully.\n\n");
			}

			// Centralized data validation and comparison summary
			bodyText.append(utils.DataComparisonReportStore.buildEmailSummary()).append("\n");

			// Visual image validation and comparison summary
			bodyText.append(utils.ImageComparisonReportStore.buildEmailSummary()).append("\n");

			bodyText.append("Please find attached:\n");
			bodyText.append("1. Automation Execution Report (ExtentReport.html)\n");

			int index = 2;
			java.util.List<java.nio.file.Path> comparisonReports = utils.DataComparisonReportStore
					.getComparisonReports();
			for (java.nio.file.Path report : comparisonReports) {
				bodyText.append(index++).append(". Centralized Data Comparison Report (")
						.append(report.getFileName().toString()).append(")\n");
			}
			java.util.List<File> failedImages = utils.ImageComparisonReportStore.getFailedScenarioAttachments();
			for (File img : failedImages) {
				bodyText.append(index++).append(". Image Comparison Mismatch (")
						.append(img.getName()).append(")\n");
			}
			if (isBuildFailed) {
				bodyText.append(index++).append(". Screenshots ZIP (failed tests, if any)\n");
			}
			if (retryZipPath != null && new File(retryZipPath).exists()) {
				bodyText.append(index++).append(". Retry Recovery Screenshots & Logs ZIP (RetryArtifacts.zip)\n");
				bodyText.append("   (Saved locally at: ").append(retryZipPath).append(")\n");
			}

			bodyText.append("\n");
			bodyText.append("Regards,\nYash  Shrivastava");

			messageBodyPart.setText(bodyText.toString());

			// Report Attachment code.
			MimeBodyPart attachmentPart = new MimeBodyPart();
			DataSource source = new FileDataSource(reportPath);
			attachmentPart.setDataHandler(new DataHandler(source));
			attachmentPart.setFileName("ExtentReport.html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachmentPart);

			// Attach generated Excel Comparison Reports
			for (java.nio.file.Path reportPathObj : comparisonReports) {
				File file = reportPathObj.toFile();
				if (file.exists()) {
					MimeBodyPart excelAttachment = new MimeBodyPart();
					DataSource excelSource = new FileDataSource(file);
					excelAttachment.setDataHandler(new DataHandler(excelSource));
					excelAttachment.setFileName(file.getName());
					multipart.addBodyPart(excelAttachment);
					Log.info("[EmailUtil] Email attachment added successfully: " + file.getName());
				}
			}

			// Attach Screenshots ZIP (only if exists)
			if (isBuildFailed && zipPath != null && new File(zipPath).exists()) {
				MimeBodyPart zipAttachment = new MimeBodyPart();
				DataSource zipSource = new FileDataSource(zipPath);
				zipAttachment.setDataHandler(new DataHandler(zipSource));
				zipAttachment.setFileName("Screenshots.zip");
				multipart.addBodyPart(zipAttachment);
			}

			// Attach image comparison failure artifacts (expected, actual, diff)
			for (File file : failedImages) {
				if (file.exists()) {
					MimeBodyPart imgAttachment = new MimeBodyPart();
					DataSource imgSource = new FileDataSource(file);
					imgAttachment.setDataHandler(new DataHandler(imgSource));
					imgAttachment.setFileName(file.getName());
					multipart.addBodyPart(imgAttachment);
					Log.info("[EmailUtil] Email attachment added successfully: " + file.getName());
				}
			}

			// Attach Retry Recovery ZIP (only if exists)
			if (retryZipPath != null && new File(retryZipPath).exists()) {
				MimeBodyPart retryZipAttachment = new MimeBodyPart();
				DataSource zipSource = new FileDataSource(retryZipPath);
				retryZipAttachment.setDataHandler(new DataHandler(zipSource));
				retryZipAttachment.setFileName("RetryArtifacts.zip");
				multipart.addBodyPart(retryZipAttachment);
				System.out.println("✅ Attached Retry Recovery ZIP to email successfully: " + retryZipPath);
			}

			message.setContent(multipart);
			Transport.send(message);

			System.out.println("✅ Extent Report Email Sent Successfully");

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}