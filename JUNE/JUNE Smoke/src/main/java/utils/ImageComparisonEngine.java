package utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageComparisonEngine {

	public static class ComparisonResult {
		private final boolean isMatch;
		private final double ssimScore;
		private final double pixelDifferencePercentage;
		private final String failureReason;
		private final String dimensionsInfo;
		private final BufferedImage diffImage;

		public ComparisonResult(boolean isMatch, double ssimScore, double pixelDifferencePercentage,
				String failureReason, String dimensionsInfo, BufferedImage diffImage) {
			this.isMatch = isMatch;
			this.ssimScore = ssimScore;
			this.pixelDifferencePercentage = pixelDifferencePercentage;
			this.failureReason = failureReason;
			this.dimensionsInfo = dimensionsInfo;
			this.diffImage = diffImage;
		}

		public boolean isMatch() {
			return isMatch;
		}

		public double getSsimScore() {
			return ssimScore;
		}

		public double getPixelDifferencePercentage() {
			return pixelDifferencePercentage;
		}

		public String getFailureReason() {
			return failureReason;
		}

		public String getDimensionsInfo() {
			return dimensionsInfo;
		}

		public BufferedImage getDiffImage() {
			return diffImage;
		}
	}

	/**
	 * Compares actual and expected image files.
	 * 
	 * @param expectedFile  the source expected image
	 * @param actualFile    the downloaded actual image
	 * @param ssimThreshold the similarity threshold (between 0.0 and 1.0)
	 * @return comparison result details
	 */
	public static ComparisonResult compareImages(File expectedFile, File actualFile, double ssimThreshold) {
		BufferedImage expectedImg;
		BufferedImage actualImg;

		try {
			expectedImg = ImageIO.read(expectedFile);
			if (expectedImg == null) {
				return new ComparisonResult(false, 0.0, 100.0,
						"Unsupported expected image format or empty file.", "Unknown", null);
			}
		} catch (IOException e) {
			return new ComparisonResult(false, 0.0, 100.0,
					"Failed to read expected file: " + e.getMessage(), "Unknown", null);
		}

		try {
			actualImg = ImageIO.read(actualFile);
			if (actualImg == null) {
				return new ComparisonResult(false, 0.0, 100.0,
						"Unsupported actual image format or empty file.", "Unknown", null);
			}
		} catch (IOException e) {
			return new ComparisonResult(false, 0.0, 100.0,
					"Failed to read actual file: " + e.getMessage(), "Unknown", null);
		}

		int expWidth = expectedImg.getWidth();
		int expHeight = expectedImg.getHeight();
		int actWidth = actualImg.getWidth();
		int actHeight = actualImg.getHeight();

		String dimensionsInfo = String.format("Expected: %dx%d, Actual: %dx%d", expWidth, expHeight, actWidth, actHeight);

		// Level 3: Dimension Verification
		if (expWidth != actWidth || expHeight != actHeight) {
			String reason = "Dimension mismatch: " + dimensionsInfo;
			return new ComparisonResult(false, 0.0, 100.0, reason, dimensionsInfo, null);
		}

		// Level 1: SSIM Validation
		double ssim = calculateSSIM(expectedImg, actualImg);

		// Level 2: Pixel Difference Analysis
		BufferedImage diffImg = new BufferedImage(expWidth, expHeight, BufferedImage.TYPE_INT_RGB);
		long mismatchCount = 0;
		int pixelDiffThreshold = 15; // Threshold for R, G, B channels

		for (int y = 0; y < expHeight; y++) {
			for (int x = 0; x < expWidth; x++) {
				int rgbExp = expectedImg.getRGB(x, y);
				int rgbAct = actualImg.getRGB(x, y);

				int rExp = (rgbExp >> 16) & 0xFF;
				int gExp = (rgbExp >> 8) & 0xFF;
				int bExp = rgbExp & 0xFF;

				int rAct = (rgbAct >> 16) & 0xFF;
				int gAct = (rgbAct >> 8) & 0xFF;
				int bAct = rgbAct & 0xFF;

				int diffR = Math.abs(rExp - rAct);
				int diffG = Math.abs(gExp - gAct);
				int diffB = Math.abs(bExp - bAct);

				if (diffR > pixelDiffThreshold || diffG > pixelDiffThreshold || diffB > pixelDiffThreshold) {
					// Highlight in bright Magenta/Fuschia
					diffImg.setRGB(x, y, Color.MAGENTA.getRGB());
					mismatchCount++;
				} else {
					// Draw a faded grayscale ghost background
					int gray = (int) (0.299 * rExp + 0.587 * gExp + 0.114 * bExp);
					// Blend with 70% white to make it a light gray ghost background
					int faded = (int) (gray * 0.3 + 178);
					int rgbFaded = (faded << 16) | (faded << 8) | faded;
					diffImg.setRGB(x, y, rgbFaded);
				}
			}
		}

		double totalPixels = expWidth * expHeight;
		double pixelDiffPct = (mismatchCount * 100.0) / totalPixels;

		boolean ssimPassed = ssim >= ssimThreshold;
		boolean isMatch = ssimPassed;

		String failureReason = "";
		if (!ssimPassed) {
			failureReason = String.format("SSIM score %.4f below threshold %.4f.", ssim, ssimThreshold);
		}

		return new ComparisonResult(isMatch, ssim, pixelDiffPct, failureReason, dimensionsInfo, diffImg);
	}

	/**
	 * Calculates the Mean Structural Similarity Index (MSSIM) between two images.
	 */
	private static double calculateSSIM(BufferedImage img1, BufferedImage img2) {
		int w = img1.getWidth();
		int h = img1.getHeight();

		double[][] gray1 = getGrayscale(img1);
		double[][] gray2 = getGrayscale(img2);

		int windowSize = 8;
		double K1 = 0.01;
		double K2 = 0.03;
		double L = 255.0;
		double C1 = (K1 * L) * (K1 * L);
		double C2 = (K2 * L) * (K2 * L);

		double ssimSum = 0;
		int numWindows = 0;

		for (int y = 0; y + windowSize <= h; y += windowSize) {
			for (int x = 0; x + windowSize <= w; x += windowSize) {
				double meanX = 0;
				double meanY = 0;

				for (int wy = 0; wy < windowSize; wy++) {
					for (int wx = 0; wx < windowSize; wx++) {
						meanX += gray1[y + wy][x + wx];
						meanY += gray2[y + wy][x + wx];
					}
				}
				meanX /= (windowSize * windowSize);
				meanY /= (windowSize * windowSize);

				double varX = 0;
				double varY = 0;
				double covXY = 0;

				for (int wy = 0; wy < windowSize; wy++) {
					for (int wx = 0; wx < windowSize; wx++) {
						double diffX = gray1[y + wy][x + wx] - meanX;
						double diffY = gray2[y + wy][x + wx] - meanY;
						varX += diffX * diffX;
						varY += diffY * diffY;
						covXY += diffX * diffY;
					}
				}

				double denomVar = windowSize * windowSize - 1;
				// Stabilize variance calculation
				varX = denomVar > 0 ? varX / denomVar : 0.0;
				varY = denomVar > 0 ? varY / denomVar : 0.0;
				covXY = denomVar > 0 ? covXY / denomVar : 0.0;

				double numerator = (2.0 * meanX * meanY + C1) * (2.0 * covXY + C2);
				double denominator = (meanX * meanX + meanY * meanY + C1) * (varX + varY + C2);

				double ssimVal = denominator != 0.0 ? numerator / denominator : 1.0;
				ssimSum += ssimVal;
				numWindows++;
			}
		}

		return numWindows > 0 ? (ssimSum / numWindows) : 1.0;
	}

	private static double[][] getGrayscale(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		double[][] gray = new double[h][w];

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = img.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;
				gray[y][x] = 0.299 * r + 0.587 * g + 0.114 * b;
			}
		}
		return gray;
	}
}
