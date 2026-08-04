package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

public class ExcelDataComparator {

	public static DataComparisonResult compareFullData(Path actualFile, Path expectedFile, int sheetIndex,
			int headerRowIndex, boolean strictHeaderOrder, boolean caseInsensitiveHeaders,
			boolean caseInsensitiveData, boolean ignoreRowOrder) throws IOException {
		return compareFullData(actualFile, expectedFile, sheetIndex, headerRowIndex, strictHeaderOrder,
				caseInsensitiveHeaders, caseInsensitiveData, ignoreRowOrder, "1");
	}

	public static DataComparisonResult compareFullData(Path actualFile, Path expectedFile, int sheetIndex,
			int headerRowIndex, boolean strictHeaderOrder, boolean caseInsensitiveHeaders,
			boolean caseInsensitiveData, boolean ignoreRowOrder, String primaryKeyColumn) throws IOException {

		ExcelHeaderValidator.HeaderComparisonResult headerResult = ExcelHeaderValidator.compareHeaders(actualFile,
				expectedFile, sheetIndex, headerRowIndex, strictHeaderOrder, caseInsensitiveHeaders);

		List<List<String>> actualRows;
		List<List<String>> expectedRows;
		if (strictHeaderOrder || !headerResult.isMatch()) {
			actualRows = readDataRows(actualFile, sheetIndex, headerRowIndex, caseInsensitiveData);
			expectedRows = readDataRows(expectedFile, sheetIndex, headerRowIndex, caseInsensitiveData);
		} else {
			List<String> canonicalHeaders = headerResult.getExpected();
			actualRows = readDataRowsAlignedToHeaders(actualFile, sheetIndex, headerRowIndex, canonicalHeaders,
					caseInsensitiveHeaders, caseInsensitiveData);
			expectedRows = readDataRowsAlignedToHeaders(expectedFile, sheetIndex, headerRowIndex, canonicalHeaders,
					caseInsensitiveHeaders, caseInsensitiveData);
		}

		RowComparisonResult rowResult = ignoreRowOrder
				? compareRowsByPrimaryKey(actualRows, expectedRows, headerResult.getExpected(), primaryKeyColumn)
				: compareRowsWithOrder(actualRows, expectedRows);

		boolean match = headerResult.isMatch() && rowResult.isMatch();
		List<String> discrepancies = buildDiscrepancies(headerResult, rowResult);
		double matchPercentage = rowResult.hasCalculatedMatchPercentage()
				? rowResult.getMatchPercentage()
				: calculateMatchPercentage(headerResult, actualRows, expectedRows);
		return new DataComparisonResult(headerResult, rowResult, match, matchPercentage, discrepancies);
	}

	public static List<List<String>> readDataRows(Path file, int sheetIndex, int headerRowIndex,
			boolean caseInsensitiveData) throws IOException {
		try (InputStream is = Files.newInputStream(file); Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet index not found: " + sheetIndex);
			}

			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			int lastRow = sheet.getLastRowNum();
			List<List<String>> rows = new ArrayList<>();
			for (int r = headerRowIndex + 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}

				short lastCell = row.getLastCellNum();
				if (lastCell <= 0) {
					continue;
				}

				List<String> values = new ArrayList<>();
				for (int c = 0; c < lastCell; c++) {
					Cell cell = row.getCell(c);
					String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
					values.add(normalize(value, caseInsensitiveData));
				}

				trimTrailingEmpty(values);
				if (!values.isEmpty()) {
					rows.add(values);
				}
			}
			return rows;
		}
	}

	public static List<List<String>> readDataRowsAlignedToHeaders(Path file, int sheetIndex, int headerRowIndex,
			List<String> canonicalHeaders, boolean caseInsensitiveHeaders, boolean caseInsensitiveData)
			throws IOException {
		try (InputStream is = Files.newInputStream(file); Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet index not found: " + sheetIndex);
			}

			Row headerRow = sheet.getRow(headerRowIndex);
			if (headerRow == null) {
				throw new IllegalArgumentException("Header row not found: " + headerRowIndex);
			}

			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			List<String> actualHeaders = new ArrayList<>();
			short lastHeaderCell = headerRow.getLastCellNum();
			for (int c = 0; c < lastHeaderCell; c++) {
				Cell cell = headerRow.getCell(c);
				String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
				actualHeaders.add(normalize(value, caseInsensitiveHeaders));
			}
			trimTrailingEmpty(actualHeaders);

			java.util.Map<String, Integer> headerIndex = new java.util.LinkedHashMap<>();
			for (int i = 0; i < actualHeaders.size(); i++) {
				String header = actualHeaders.get(i);
				if (header.isEmpty()) {
					continue;
				}
				if (headerIndex.containsKey(header)) {
					throw new IllegalArgumentException("Duplicate header found in file " + file + ": " + header);
				}
				headerIndex.put(header, i);
			}

			List<List<String>> rows = new ArrayList<>();
			int lastRow = sheet.getLastRowNum();
			for (int r = headerRowIndex + 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}

				List<String> values = new ArrayList<>();
				boolean hasValue = false;
				for (String header : canonicalHeaders) {
					Integer columnIndex = headerIndex.get(header);
					String value = "";
					if (columnIndex != null) {
						Cell cell = row.getCell(columnIndex);
						value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
					}
					String normalizedValue = normalize(value, caseInsensitiveData);
					if (!normalizedValue.isEmpty()) {
						hasValue = true;
					}
					values.add(normalizedValue);
				}

				if (hasValue) {
					rows.add(values);
				}
			}

			return rows;
		}
	}

	private static RowComparisonResult compareRowsWithOrder(List<List<String>> actual, List<List<String>> expected) {
		List<RowMismatch> mismatches = new ArrayList<>();
		int max = Math.max(actual.size(), expected.size());
		for (int i = 0; i < max; i++) {
			List<String> actualRow = i < actual.size() ? actual.get(i) : null;
			List<String> expectedRow = i < expected.size() ? expected.get(i) : null;
			if (!Objects.equals(actualRow, expectedRow)) {
				mismatches.add(new RowMismatch(i, expectedRow, actualRow));
			}
		}
		boolean match = mismatches.isEmpty();
		return RowComparisonResult.ordered(mismatches, match);
	}

	private static RowComparisonResult compareRowsByPrimaryKey(List<List<String>> actual, List<List<String>> expected,
			List<String> headers, String primaryKeyColumn) {
		int keyIndex = resolvePrimaryKeyIndex(headers, primaryKeyColumn);
		String keyColumnName = resolveHeader(headers, keyIndex);
		Map<String, List<RowReference>> actualByKey = indexRowsByKey(actual, keyIndex);
		Map<String, List<RowReference>> expectedByKey = indexRowsByKey(expected, keyIndex);

		List<String> discrepancies = new ArrayList<>();
		int totalChecks = 0;
		int matchedChecks = 0;

		for (Map.Entry<String, List<RowReference>> entry : expectedByKey.entrySet()) {
			if (entry.getValue().size() > 1) {
				discrepancies.add("Duplicate key in source file. Column='" + keyColumnName + "', key='" + entry.getKey()
						+ "', source rows=" + resolveRowNumbers(entry.getValue()));
			}
		}

		for (Map.Entry<String, List<RowReference>> entry : actualByKey.entrySet()) {
			if (entry.getValue().size() > 1) {
				discrepancies.add("Duplicate key in output file. Column='" + keyColumnName + "', key='" + entry.getKey()
						+ "', output rows=" + resolveRowNumbers(entry.getValue()));
			}
		}

		for (Map.Entry<String, List<RowReference>> expectedEntry : expectedByKey.entrySet()) {
			String key = expectedEntry.getKey();
			List<RowReference> expectedMatches = expectedEntry.getValue();
			List<RowReference> actualMatches = actualByKey.get(key);

			if (actualMatches == null || actualMatches.isEmpty()) {
				for (RowReference expectedRow : expectedMatches) {
					discrepancies.add("Missing record. Column='" + keyColumnName + "', key='" + key
							+ "', source row=" + expectedRow.getExcelRowNumber());
				}
				continue;
			}

			if (expectedMatches.size() != 1 || actualMatches.size() != 1) {
				continue;
			}

			RowReference expectedRow = expectedMatches.get(0);
			RowReference actualRow = actualMatches.get(0);
			int maxCells = Math.max(expectedRow.getValues().size(), actualRow.getValues().size());
			for (int c = 0; c < maxCells; c++) {
				String expectedValue = c < expectedRow.getValues().size() ? expectedRow.getValues().get(c) : "";
				String actualValue = c < actualRow.getValues().size() ? actualRow.getValues().get(c) : "";
				totalChecks++;
				if (Objects.equals(expectedValue, actualValue)) {
					matchedChecks++;
				} else {
					discrepancies.add("Field mismatch. Key Column='" + keyColumnName + "', key='" + key
							+ "', Column Name='" + resolveHeader(headers, c) + "', Expected Value='" + expectedValue
							+ "', Actual Value='" + actualValue + "', Source Row Reference="
							+ expectedRow.getExcelRowNumber() + ", Output Row Reference="
							+ actualRow.getExcelRowNumber());
				}
			}
		}

		for (Map.Entry<String, List<RowReference>> actualEntry : actualByKey.entrySet()) {
			if (!expectedByKey.containsKey(actualEntry.getKey())) {
				for (RowReference actualRow : actualEntry.getValue()) {
					discrepancies.add("Unexpected record. Column='" + keyColumnName + "', key='" + actualEntry.getKey()
							+ "', output row=" + actualRow.getExcelRowNumber());
				}
			}
		}

		boolean match = discrepancies.isEmpty();
		double matchPercentage = totalChecks == 0 ? (match ? 100.0 : 0.0) : (matchedChecks * 100.0) / totalChecks;
		return RowComparisonResult.keyBased(keyColumnName, discrepancies, match, matchPercentage);
	}

	private static Map<String, List<RowReference>> indexRowsByKey(List<List<String>> rows, int keyIndex) {
		Map<String, List<RowReference>> indexedRows = new LinkedHashMap<>();
		for (int i = 0; i < rows.size(); i++) {
			List<String> row = rows.get(i);
			String key = keyIndex < row.size() ? row.get(keyIndex) : "";
			RowReference rowReference = new RowReference(i + 2, row);
			indexedRows.computeIfAbsent(key, ignored -> new ArrayList<>()).add(rowReference);
		}
		return indexedRows;
	}

	private static int resolvePrimaryKeyIndex(List<String> headers, String primaryKeyColumn) {
		if (primaryKeyColumn == null || primaryKeyColumn.isBlank()) {
			return 0;
		}

		String trimmed = primaryKeyColumn.trim();
		try {
			int oneBasedIndex = Integer.parseInt(trimmed);
			if (oneBasedIndex <= 0) {
				throw new IllegalArgumentException("Primary key column number must be 1 or greater: " + primaryKeyColumn);
			}
			return oneBasedIndex - 1;
		} catch (NumberFormatException ignored) {
			for (int i = 0; i < headers.size(); i++) {
				if (trimmed.equalsIgnoreCase(headers.get(i))) {
					return i;
				}
			}
			throw new IllegalArgumentException("Primary key column header not found: " + primaryKeyColumn);
		}
	}

	private static List<Integer> resolveRowNumbers(List<RowReference> rows) {
		List<Integer> rowNumbers = new ArrayList<>();
		for (RowReference row : rows) {
			rowNumbers.add(row.getExcelRowNumber());
		}
		return rowNumbers;
	}

	private static String normalize(String value, boolean caseInsensitive) {
		if (value == null) {
			return "";
		}
		String normalized = value.trim().replaceAll("\\s+", " ");
		return caseInsensitive ? normalized.toLowerCase() : normalized;
	}

	private static void trimTrailingEmpty(List<String> values) {
		int lastNonEmpty = values.size() - 1;
		while (lastNonEmpty >= 0 && values.get(lastNonEmpty).isEmpty()) {
			lastNonEmpty--;
		}
		if (lastNonEmpty < values.size() - 1) {
			values.subList(lastNonEmpty + 1, values.size()).clear();
		}
	}

	private static List<String> buildDiscrepancies(ExcelHeaderValidator.HeaderComparisonResult headerResult,
			RowComparisonResult rowResult) {
		List<String> discrepancies = new ArrayList<>();
		if (headerResult.isOrdered()) {
			for (ExcelHeaderValidator.HeaderMismatch mismatch : headerResult.getMismatches()) {
				discrepancies.add("Header index " + mismatch.getIndex() + ": expected='" + mismatch.getExpected()
						+ "', actual='" + mismatch.getActual() + "'");
			}
		} else {
			for (String missing : headerResult.getMissing()) {
				discrepancies.add("Missing field/header: " + missing);
			}
			for (String extra : headerResult.getExtra()) {
				discrepancies.add("Unexpected field/header: " + extra);
			}
		}

		if (rowResult.isOrdered()) {
			List<String> headers = headerResult.getExpected();
			for (RowMismatch mismatch : rowResult.getMismatches()) {
				List<String> expected = mismatch.getExpected();
				List<String> actual = mismatch.getActual();
				if (expected == null) {
					discrepancies.add("Unexpected row " + (mismatch.getIndex() + 1) + ": actual=" + actual);
					continue;
				}
				if (actual == null) {
					discrepancies.add("Missing row " + (mismatch.getIndex() + 1) + ": expected=" + expected);
					continue;
				}

				int maxCells = Math.max(expected.size(), actual.size());
				for (int c = 0; c < maxCells; c++) {
					String expectedValue = c < expected.size() ? expected.get(c) : "";
					String actualValue = c < actual.size() ? actual.get(c) : "";
					if (!Objects.equals(expectedValue, actualValue)) {
						discrepancies.add("Row " + (mismatch.getIndex() + 1) + ", field '" + resolveHeader(headers, c)
								+ "': expected='" + expectedValue + "', actual='" + actualValue + "'");
					}
				}
			}
		} else {
			discrepancies.addAll(rowResult.getKeyDiscrepancies());
		}

		return discrepancies;
	}

	private static double calculateMatchPercentage(ExcelHeaderValidator.HeaderComparisonResult headerResult,
			List<List<String>> actualRows, List<List<String>> expectedRows) {
		int total = 0;
		int matched = 0;

		if (headerResult.isOrdered()) {
			int maxHeaders = Math.max(headerResult.getActual().size(), headerResult.getExpected().size());
			for (int i = 0; i < maxHeaders; i++) {
				String expectedHeader = i < headerResult.getExpected().size() ? headerResult.getExpected().get(i) : "";
				String actualHeader = i < headerResult.getActual().size() ? headerResult.getActual().get(i) : "";
				total++;
				if (Objects.equals(expectedHeader, actualHeader)) {
					matched++;
				}
			}
		} else {
			total += headerResult.getExpected().size();
			if (headerResult.isMatch()) {
				matched += headerResult.getExpected().size();
			}
		}

		int maxRows = Math.max(actualRows.size(), expectedRows.size());
		for (int r = 0; r < maxRows; r++) {
			List<String> expected = r < expectedRows.size() ? expectedRows.get(r) : new ArrayList<>();
			List<String> actual = r < actualRows.size() ? actualRows.get(r) : new ArrayList<>();
			int maxCells = Math.max(expected.size(), actual.size());
			for (int c = 0; c < maxCells; c++) {
				String expectedValue = c < expected.size() ? expected.get(c) : "";
				String actualValue = c < actual.size() ? actual.get(c) : "";
				total++;
				if (Objects.equals(expectedValue, actualValue)) {
					matched++;
				}
			}
		}

		return total == 0 ? 100.0 : (matched * 100.0) / total;
	}

	private static String resolveHeader(List<String> headers, int index) {
		if (index >= 0 && index < headers.size() && headers.get(index) != null && !headers.get(index).isBlank()) {
			return headers.get(index);
		}
		return "Column " + (index + 1);
	}

	public static class DataComparisonResult {
		private final ExcelHeaderValidator.HeaderComparisonResult headerResult;
		private final RowComparisonResult rowResult;
		private final boolean match;
		private final double matchPercentage;
		private final List<String> discrepancies;

		public DataComparisonResult(ExcelHeaderValidator.HeaderComparisonResult headerResult,
				RowComparisonResult rowResult, boolean match, double matchPercentage, List<String> discrepancies) {
			this.headerResult = headerResult;
			this.rowResult = rowResult;
			this.match = match;
			this.matchPercentage = matchPercentage;
			this.discrepancies = discrepancies == null ? new ArrayList<>() : new ArrayList<>(discrepancies);
		}

		public ExcelHeaderValidator.HeaderComparisonResult getHeaderResult() {
			return headerResult;
		}

		public RowComparisonResult getRowResult() {
			return rowResult;
		}

		public boolean isMatch() {
			return match;
		}

		public double getMatchPercentage() {
			return matchPercentage;
		}

		public List<String> getDiscrepancies() {
			return new ArrayList<>(discrepancies);
		}

		public String toReportString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Validation Success Rate / Match Percentage: ")
					.append(String.format("%.2f", matchPercentage)).append("%").append(System.lineSeparator());
			sb.append(headerResult.toReportString());
			sb.append(rowResult.toReportString());
			if (!discrepancies.isEmpty()) {
				sb.append("Failed Fields / Discrepancies:").append(System.lineSeparator());
				for (String discrepancy : discrepancies) {
					sb.append("- ").append(discrepancy).append(System.lineSeparator());
				}
			}
			return sb.toString();
		}
	}

	public static class RowComparisonResult {
		private final List<RowMismatch> mismatches;
		private final List<List<String>> missing;
		private final List<List<String>> extra;
		private final List<String> keyDiscrepancies;
		private final String primaryKeyColumn;
		private final double matchPercentage;
		private final boolean match;
		private final boolean ordered;

		private RowComparisonResult(List<RowMismatch> mismatches, List<List<String>> missing,
				List<List<String>> extra, List<String> keyDiscrepancies, String primaryKeyColumn,
				double matchPercentage, boolean match, boolean ordered) {
			this.mismatches = mismatches;
			this.missing = missing;
			this.extra = extra;
			this.keyDiscrepancies = keyDiscrepancies;
			this.primaryKeyColumn = primaryKeyColumn;
			this.matchPercentage = matchPercentage;
			this.match = match;
			this.ordered = ordered;
		}

		public static RowComparisonResult ordered(List<RowMismatch> mismatches, boolean match) {
			return new RowComparisonResult(mismatches, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null,
					-1.0, match, true);
		}

		public static RowComparisonResult unordered(List<List<String>> missing, List<List<String>> extra,
				boolean match) {
			return new RowComparisonResult(new ArrayList<>(), missing, extra, new ArrayList<>(), null, -1.0, match,
					false);
		}

		public static RowComparisonResult keyBased(String primaryKeyColumn, List<String> keyDiscrepancies,
				boolean match, double matchPercentage) {
			return new RowComparisonResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
					new ArrayList<>(keyDiscrepancies), primaryKeyColumn, matchPercentage, match, false);
		}

		public List<RowMismatch> getMismatches() {
			return mismatches;
		}

		public List<List<String>> getMissing() {
			return missing;
		}

		public List<List<String>> getExtra() {
			return extra;
		}

		public List<String> getKeyDiscrepancies() {
			return new ArrayList<>(keyDiscrepancies);
		}

		public boolean hasCalculatedMatchPercentage() {
			return matchPercentage >= 0.0;
		}

		public double getMatchPercentage() {
			return matchPercentage;
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
				sb.append("Row order check: ").append(match ? "MATCH" : "MISMATCH").append(System.lineSeparator());
				for (RowMismatch mismatch : mismatches) {
					sb.append("Row ").append(mismatch.getIndex()).append(": expected=")
							.append(mismatch.getExpected()).append(", actual=").append(mismatch.getActual())
							.append(System.lineSeparator());
				}
			} else {
				sb.append("Key-based unordered row check");
				if (primaryKeyColumn != null) {
					sb.append(" using primary key column '").append(primaryKeyColumn).append("'");
				}
				sb.append(": ").append(match ? "MATCH" : "MISMATCH").append(System.lineSeparator());
				for (String discrepancy : keyDiscrepancies) {
					sb.append(discrepancy).append(System.lineSeparator());
				}
			}
			return sb.toString();
		}
	}

	private static class RowReference {
		private final int excelRowNumber;
		private final List<String> values;

		private RowReference(int excelRowNumber, List<String> values) {
			this.excelRowNumber = excelRowNumber;
			this.values = values;
		}

		public int getExcelRowNumber() {
			return excelRowNumber;
		}

		public List<String> getValues() {
			return values;
		}
	}

	public static class RowMismatch {
		private final int index;
		private final List<String> expected;
		private final List<String> actual;

		public RowMismatch(int index, List<String> expected, List<String> actual) {
			this.index = index;
			this.expected = expected;
			this.actual = actual;
		}

		public int getIndex() {
			return index;
		}

		public List<String> getExpected() {
			return expected;
		}

		public List<String> getActual() {
			return actual;
		}
	}
}
