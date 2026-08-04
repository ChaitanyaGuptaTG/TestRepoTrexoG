package test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ImageComparisonEngine;
import utils.ImageComparisonReportStore;

public class ImageComparisonTest {

	private Path tempDir;
	private File expectedFile;
	private File matchFile;
	private File mismatchFile;
	private File sizeMismatchFile;

	@BeforeClass
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("img_comp_test");

		// Create expected image: 100x100 white square with a blue rectangle
		BufferedImage expected = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = expected.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 100, 100);
		g2d.setColor(Color.BLUE);
		g2d.fillRect(20, 20, 60, 60);
		g2d.dispose();
		expectedFile = tempDir.resolve("expected.png").toFile();
		ImageIO.write(expected, "png", expectedFile);

		// Create matching image: identical
		matchFile = tempDir.resolve("match.png").toFile();
		ImageIO.write(expected, "png", matchFile);

		// Create mismatching image: slight visual mismatch (a yellow dot inside)
		BufferedImage mismatch = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		g2d = mismatch.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 100, 100);
		g2d.setColor(Color.BLUE);
		g2d.fillRect(20, 20, 60, 60);
		g2d.setColor(Color.YELLOW);
		g2d.fillRect(45, 45, 10, 10); // visual difference
		g2d.dispose();
		mismatchFile = tempDir.resolve("mismatch.png").toFile();
		ImageIO.write(mismatch, "png", mismatchFile);

		// Create dimension mismatch image: 120x120
		BufferedImage sizeMismatch = new BufferedImage(120, 120, BufferedImage.TYPE_INT_RGB);
		g2d = sizeMismatch.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 120, 120);
		g2d.dispose();
		sizeMismatchFile = tempDir.resolve("size_mismatch.png").toFile();
		ImageIO.write(sizeMismatch, "png", sizeMismatchFile);
	}

	@AfterClass
	public void tearDown() throws IOException {
		// Cleanup temp directory files
		if (expectedFile.exists()) expectedFile.delete();
		if (matchFile.exists()) matchFile.delete();
		if (mismatchFile.exists()) mismatchFile.delete();
		if (sizeMismatchFile.exists()) sizeMismatchFile.delete();
		Files.deleteIfExists(tempDir);
	}

	@Test
	public void testExactMatch() {
		ImageComparisonEngine.ComparisonResult result = ImageComparisonEngine.compareImages(expectedFile, matchFile, 0.99);
		Assert.assertTrue(result.isMatch(), "Identical images should match.");
		Assert.assertEquals(result.getSsimScore(), 1.0, 0.001, "SSIM score for identical images should be 1.0");
		Assert.assertEquals(result.getPixelDifferencePercentage(), 0.0, 0.001, "Pixel difference for identical images should be 0%");
	}

	@Test
	public void testVisualMismatch() {
		ImageComparisonEngine.ComparisonResult result = ImageComparisonEngine.compareImages(expectedFile, mismatchFile, 0.99);
		Assert.assertFalse(result.isMatch(), "Visual differences should fail the threshold.");
		Assert.assertTrue(result.getSsimScore() < 1.0, "SSIM score should be less than 1.0");
		Assert.assertTrue(result.getPixelDifferencePercentage() > 0.0, "Pixel difference should be greater than 0%");
		Assert.assertNotNull(result.getDiffImage(), "Difference image must be generated");

		// Record result in store to test email summary construction
		ImageComparisonReportStore.clear();
		ImageComparisonReportStore.record(new ImageComparisonReportStore.ImageScenarioResult(
				"Test Scenario Mismatch", expectedFile.toPath(), mismatchFile.toPath(), mismatchFile.toPath(),
				result.getSsimScore(), result.getPixelDifferencePercentage(), result.getDimensionsInfo(),
				"FAIL", result.getFailureReason()
		));

		String summary = ImageComparisonReportStore.buildEmailSummary();
		System.out.println("Email Summary output:\n" + summary);
		Assert.assertTrue(summary.contains("Test Scenario Mismatch"), "Summary should list scenario name.");
		Assert.assertTrue(summary.contains("FAIL"), "Summary should report failure status.");
	}

	@Test
	public void testDimensionMismatch() {
		ImageComparisonEngine.ComparisonResult result = ImageComparisonEngine.compareImages(expectedFile, sizeMismatchFile, 0.99);
		Assert.assertFalse(result.isMatch(), "Dimension mismatch should fail visual comparison.");
		Assert.assertTrue(result.getFailureReason().contains("Dimension mismatch"), "Failure reason must indicate dimension issue.");
		Assert.assertEquals(result.getPixelDifferencePercentage(), 100.0, "Mismatched dimensions should report 100% pixel difference.");
	}
}
