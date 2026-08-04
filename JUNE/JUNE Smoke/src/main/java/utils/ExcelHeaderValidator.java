package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelHeaderValidator {

	public static HeaderComparisonResult compareHeaders(Path actualFile, Path expectedFile, int sheetIndex,
			int headerRowIndex, boolean strictOrder, boolean caseInsensitive) throws IOException {
		List<String> actual = readHeader(actualFile, sheetIndex, headerRowIndex, caseInsensitive);
		List<String> expected = readHeader(expectedFile, sheetIndex, headerRowIndex, caseInsensitive);

		if (strictOrder) {
			return compareWithOrder(actual, expected);
		}
		return compareWithoutOrder(actual, expected);
	}

	public static List<String> readHeader(Path file, int sheetIndex, int headerRowIndex, boolean caseInsensitive)
			throws IOException {
		try (InputStream is = Files.newInputStream(file); Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet index not found: " + sheetIndex);
			}

			Row row = sheet.getRow(headerRowIndex);
			if (row == null) {
				throw new IllegalArgumentException("Header row not found: " + headerRowIndex);
			}

			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			List<String> headers = new ArrayList<>();
			short lastCell = row.getLastCellNum();
			for (int i = 0; i < lastCell; i++) {
				Cell cell = row.getCell(i);
				String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
				headers.add(normalize(value, caseInsensitive));
			}

			// Remove trailing empty header cells.
			int lastNonEmpty = headers.size() - 1;
			while (lastNonEmpty >= 0 && headers.get(lastNonEmpty).isEmpty()) {
				lastNonEmpty--;
			}
			return headers.subList(0, lastNonEmpty + 1);
		}
	}

	private static HeaderComparisonResult compareWithOrder(List<String> actual, List<String> expected) {
		List<HeaderMismatch> mismatches = new ArrayList<>();
		int max = Math.max(actual.size(), expected.size());
		for (int i = 0; i < max; i++) {
			String actualValue = i < actual.size() ? actual.get(i) : null;
			String expectedValue = i < expected.size() ? expected.get(i) : null;
			if (!Objects.equals(actualValue, expectedValue)) {
				mismatches.add(new HeaderMismatch(i, expectedValue, actualValue));
			}
		}

		boolean match = mismatches.isEmpty();
		return HeaderComparisonResult.ordered(actual, expected, mismatches, match);
	}

	private static HeaderComparisonResult compareWithoutOrder(List<String> actual, List<String> expected) {
		Map<String, Integer> actualCounts = countValues(actual);
		Map<String, Integer> expectedCounts = countValues(expected);

		List<String> missing = new ArrayList<>();
		List<String> extra = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : expectedCounts.entrySet()) {
			int actualCount = actualCounts.getOrDefault(entry.getKey(), 0);
			for (int i = actualCount; i < entry.getValue(); i++) {
				missing.add(entry.getKey());
			}
		}

		for (Map.Entry<String, Integer> entry : actualCounts.entrySet()) {
			int expectedCount = expectedCounts.getOrDefault(entry.getKey(), 0);
			for (int i = expectedCount; i < entry.getValue(); i++) {
				extra.add(entry.getKey());
			}
		}

		boolean match = missing.isEmpty();
		return HeaderComparisonResult.unordered(actual, expected, missing, extra, match);
	}

	private static Map<String, Integer> countValues(List<String> values) {
		Map<String, Integer> counts = new HashMap<>();
		for (String value : values) {
			counts.put(value, counts.getOrDefault(value, 0) + 1);
		}
		return counts;
	}

	private static String normalize(String value, boolean caseInsensitive) {
		if (value == null) {
			return "";
		}
		String normalized = value.trim().replaceAll("\\s+", " ");
		return caseInsensitive ? normalized.toLowerCase() : normalized;
	}

	public static class HeaderComparisonResult {
		private final List<String> actual;
		private final List<String> expected;
		private final List<HeaderMismatch> mismatches;
		private final List<String> missing;
		private final List<String> extra;
		private final boolean match;
		private final boolean ordered;

		private HeaderComparisonResult(List<String> actual, List<String> expected, List<HeaderMismatch> mismatches,
				List<String> missing, List<String> extra, boolean match, boolean ordered) {
			this.actual = actual;
			this.expected = expected;
			this.mismatches = mismatches;
			this.missing = missing;
			this.extra = extra;
			this.match = match;
			this.ordered = ordered;
		}

		public static HeaderComparisonResult ordered(List<String> actual, List<String> expected,
				List<HeaderMismatch> mismatches, boolean match) {
			return new HeaderComparisonResult(actual, expected, mismatches, new ArrayList<>(), new ArrayList<>(), match,
					true);
		}

		public static HeaderComparisonResult unordered(List<String> actual, List<String> expected,
				List<String> missing, List<String> extra, boolean match) {
			return new HeaderComparisonResult(actual, expected, new ArrayList<>(), missing, extra, match, false);
		}

		public List<String> getActual() {
			return actual;
		}

		public List<String> getExpected() {
			return expected;
		}

		public List<HeaderMismatch> getMismatches() {
			return mismatches;
		}

		public List<String> getMissing() {
			return missing;
		}

		public List<String> getExtra() {
			return extra;
		}

		public boolean isMatch() {
			return match;
		}

		public boolean isOrdered() {
			return ordered;
		}

		public String toReportString() {
			StringBuilder sb = new StringBuilder();
			if (ordered) {
				sb.append("Header order check: ").append(match ? "MATCH" : "MISMATCH").append(System.lineSeparator());
				for (HeaderMismatch mismatch : mismatches) {
					sb.append("Index ").append(mismatch.getIndex()).append(": expected=")
							.append(mismatch.getExpected()).append(", actual=").append(mismatch.getActual())
							.append(System.lineSeparator());
				}
			} else {
				sb.append("Header set check: ").append(match ? "MATCH" : "MISMATCH").append(System.lineSeparator());
				if (!missing.isEmpty()) {
					sb.append("Missing: ").append(missing).append(System.lineSeparator());
				}
				if (!extra.isEmpty()) {
					sb.append("Extra: ").append(extra).append(System.lineSeparator());
				}
			}
			return sb.toString();
		}
	}

	public static class HeaderMismatch {
		private final int index;
		private final String expected;
		private final String actual;

		public HeaderMismatch(int index, String expected, String actual) {
			this.index = index;
			this.expected = expected;
			this.actual = actual;
		}

		public int getIndex() {
			return index;
		}

		public String getExpected() {
			return expected;
		}

		public String getActual() {
			return actual;
		}
	}
}
