package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ExcelComparisonEngine {

	public static class ScenarioResult {
		private final String scenarioName;
		private final String scenarioKey;
		private final Path expectedFile;
		private final Path actualFile;
		private final boolean match;
		private final int matchedCount;
		private final int mismatchedCount;
		private final List<String> discrepancies;

		public ScenarioResult(String scenarioName, String scenarioKey, Path expectedFile, Path actualFile,
							  boolean match, int matchedCount, int mismatchedCount, List<String> discrepancies) {
			this.scenarioName = scenarioName;
			this.scenarioKey = scenarioKey;
			this.expectedFile = expectedFile;
			this.actualFile = actualFile;
			this.match = match;
			this.matchedCount = matchedCount;
			this.mismatchedCount = mismatchedCount;
			this.discrepancies = discrepancies;
		}

		public String getScenarioName() { return scenarioName; }
		public String getScenarioKey() { return scenarioKey; }
		public Path getExpectedFile() { return expectedFile; }
		public Path getActualFile() { return actualFile; }
		public boolean isMatch() { return match; }
		public int getMatchedCount() { return matchedCount; }
		public int getMismatchedCount() { return mismatchedCount; }
		public List<String> getDiscrepancies() { return discrepancies; }
	}

	public static class ComparisonSummary {
		private final String intentName;
		private final String workflowKey;
		private final Path reportPath;
		private final boolean success;
		private final int totalMatchedRecords;
		private final int totalMismatches;
		private final List<String> detailsLog;
		private final List<ScenarioResult> scenarioResults;

		public ComparisonSummary(String intentName, String workflowKey, Path reportPath, boolean success,
								 int totalMatchedRecords, int totalMismatches, List<String> detailsLog,
								 List<ScenarioResult> scenarioResults) {
			this.intentName = intentName;
			this.workflowKey = workflowKey;
			this.reportPath = reportPath;
			this.success = success;
			this.totalMatchedRecords = totalMatchedRecords;
			this.totalMismatches = totalMismatches;
			this.detailsLog = detailsLog;
			this.scenarioResults = scenarioResults;
		}

		public String getIntentName() { return intentName; }
		public String getWorkflowKey() { return workflowKey; }
		public Path getReportPath() { return reportPath; }
		public boolean isSuccess() { return success; }
		public int getTotalMatchedRecords() { return totalMatchedRecords; }
		public int getTotalMismatches() { return totalMismatches; }
		public List<String> getDetailsLog() { return detailsLog; }
		public List<ScenarioResult> getScenarioResults() { return scenarioResults; }
	}

	public static ComparisonSummary compareAndGenerateReport(
			String intentName,
			String workflowKey,
			List<ScenarioComparisonTask> tasks,
			Path comparisonOutputDir
	) throws IOException {

		Log.info("[ExcelComparisonEngine] Starting validation and comparison for intent: " + intentName);
		Files.createDirectories(comparisonOutputDir);

		String sanitizedIntent = intentName.replaceAll("[^a-zA-Z0-9_-]", "_");
		Path reportPath = comparisonOutputDir.resolve(sanitizedIntent + "_Comparison_Report.xlsx");

		int totalMatched = 0;
		int totalMismatches = 0;
		boolean overallSuccess = true;
		List<String> detailsLog = new ArrayList<>();
		List<ScenarioResult> scenarioResults = new ArrayList<>();

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			// Predefine some common styles and colors
			XSSFColor headerFillColor = new XSSFColor(new java.awt.Color(31, 78, 121), null); // Dark slate blue #1F4E79
			XSSFColor expectedFillColor = new XSSFColor(new java.awt.Color(242, 244, 248), null); // Light grey-blue #F2F4F8
			XSSFColor mismatchFillColor = new XSSFColor(new java.awt.Color(255, 199, 206), null); // Soft red #FFC7CE
			XSSFColor mismatchFontColor = new XSSFColor(new java.awt.Color(156, 0, 6), null); // Dark red text #9C0006
			XSSFColor warningFillColor = new XSSFColor(new java.awt.Color(255, 235, 156), null); // Soft yellow for warning

			// Styles
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(headerFillColor);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setBorderBottom(BorderStyle.MEDIUM);
			headerStyle.setBorderTop(BorderStyle.THIN);
			headerStyle.setBorderLeft(BorderStyle.THIN);
			headerStyle.setBorderRight(BorderStyle.THIN);
			Font headerFont = workbook.createFont();
			headerFont.setColor(IndexedColors.WHITE.getIndex());
			headerFont.setBold(true);
			headerFont.setFontName("Segoe UI");
			headerStyle.setFont(headerFont);

			CellStyle expectedStyle = workbook.createCellStyle();
			expectedStyle.setFillForegroundColor(expectedFillColor);
			expectedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			expectedStyle.setBorderBottom(BorderStyle.HAIR);
			expectedStyle.setBorderTop(BorderStyle.HAIR);
			expectedStyle.setBorderLeft(BorderStyle.THIN);
			expectedStyle.setBorderRight(BorderStyle.THIN);
			Font dataFont = workbook.createFont();
			dataFont.setFontName("Segoe UI");
			expectedStyle.setFont(dataFont);

			CellStyle actualStyle = workbook.createCellStyle();
			actualStyle.setBorderBottom(BorderStyle.THIN);
			actualStyle.setBorderTop(BorderStyle.HAIR);
			actualStyle.setBorderLeft(BorderStyle.THIN);
			actualStyle.setBorderRight(BorderStyle.THIN);
			actualStyle.setFont(dataFont);

			XSSFCellStyle mismatchStyle = workbook.createCellStyle();
			mismatchStyle.setFillForegroundColor(mismatchFillColor);
			mismatchStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			mismatchStyle.setBorderBottom(BorderStyle.THIN);
			mismatchStyle.setBorderTop(BorderStyle.HAIR);
			mismatchStyle.setBorderLeft(BorderStyle.THIN);
			mismatchStyle.setBorderRight(BorderStyle.THIN);
			Font mismatchFont = workbook.createFont();
			mismatchFont.setColor(mismatchFontColor.getIndex());
			mismatchFont.setBold(true);
			mismatchFont.setFontName("Segoe UI");
			mismatchStyle.setFont(mismatchFont);

			CellStyle warningStyle = workbook.createCellStyle();
			warningStyle.setFillForegroundColor(warningFillColor);
			warningStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			warningStyle.setBorderBottom(BorderStyle.THIN);
			warningStyle.setBorderTop(BorderStyle.THIN);
			warningStyle.setBorderLeft(BorderStyle.THIN);
			warningStyle.setBorderRight(BorderStyle.THIN);
			Font warningFont = workbook.createFont();
			warningFont.setColor(IndexedColors.DARK_YELLOW.getIndex());
			warningFont.setFontName("Segoe UI");
			warningStyle.setFont(warningFont);

			for (ScenarioComparisonTask task : tasks) {
				String sheetName = task.getScenarioName();
				// Sheet name must be less than 31 characters and have no invalid characters
				sheetName = sheetName.replaceAll("[\\\\*?/\\[\\]]", "");
				if (sheetName.length() > 30) {
					sheetName = sheetName.substring(0, 30);
				}

				Sheet sheet = workbook.createSheet(sheetName);
				sheet.setDisplayGridlines(true);

				Path expectedFile = task.getExpectedFile();
				Path actualFile = task.getActualFile();

				// Handle missing files gracefully
				if (expectedFile == null || !Files.exists(expectedFile)) {
					String msg = "Missing expected source file for scenario " + task.getScenarioName();
					Log.warn("[ExcelComparisonEngine] " + msg);
					detailsLog.add("Scenario " + task.getScenarioName() + " | Status: FAILED | " + msg);
					writeErrorSheet(sheet, msg, warningStyle);
					overallSuccess = false;
					scenarioResults.add(new ScenarioResult(
						task.getScenarioName(), task.getScenarioKey(), expectedFile, actualFile,
						false, 0, 0, Arrays.asList(msg)
					));
					continue;
				}

				if (actualFile == null || !Files.exists(actualFile)) {
					String msg = "Missing downloaded output file for scenario " + task.getScenarioName();
					Log.warn("[ExcelComparisonEngine] " + msg);
					detailsLog.add("Scenario " + task.getScenarioName() + " | Status: FAILED | " + msg);
					writeErrorSheet(sheet, msg, warningStyle);
					overallSuccess = false;
					scenarioResults.add(new ScenarioResult(
						task.getScenarioName(), task.getScenarioKey(), expectedFile, actualFile,
						false, 0, 0, Arrays.asList(msg)
					));
					continue;
				}

				Log.info("[ExcelComparisonEngine] File matched and found for comparison: expected='" + expectedFile.getFileName() + "', actual='" + actualFile.getFileName() + "'");
				Log.info("[ExcelComparisonEngine] Comparing: " + expectedFile.getFileName() + " vs " + actualFile.getFileName());

				// Read headers & records
				List<String> expectedHeaders = ExcelHeaderValidator.readHeader(
						expectedFile, task.getSheetIndex(), task.getHeaderRowIndex(), task.isCaseInsensitiveHeaders());
				List<String> actualHeaders = ExcelHeaderValidator.readHeader(
						actualFile, task.getSheetIndex(), task.getHeaderRowIndex(), task.isCaseInsensitiveHeaders());

				ExcelHeaderValidator.HeaderComparisonResult headerResult = ExcelHeaderValidator.compareHeaders(
						actualFile, expectedFile, task.getSheetIndex(), task.getHeaderRowIndex(),
						task.isStrictHeaderOrder(), task.isCaseInsensitiveHeaders());

				List<List<String>> rawExpectedRows;
				List<List<String>> rawActualRows;

				if (task.isStrictHeaderOrder() || !headerResult.isMatch()) {
					rawExpectedRows = readDataRowsRaw(expectedFile, task.getSheetIndex(), task.getHeaderRowIndex());
					rawActualRows = readDataRowsRaw(actualFile, task.getSheetIndex(), task.getHeaderRowIndex());
				} else {
					rawExpectedRows = readDataRowsAligned(expectedFile, task.getSheetIndex(), task.getHeaderRowIndex(), expectedHeaders, task.isCaseInsensitiveHeaders());
					rawActualRows = readDataRowsAligned(actualFile, task.getSheetIndex(), task.getHeaderRowIndex(), expectedHeaders, task.isCaseInsensitiveHeaders());
				}

				// Build the headers in our output worksheet
				Row headerRow = sheet.createRow(0);
				// To keep original column headers completely unchanged, we write exactly expectedHeaders
				for (int i = 0; i < expectedHeaders.size(); i++) {
					Cell cell = headerRow.createCell(i);
					cell.setCellValue(expectedHeaders.get(i));
					cell.setCellStyle(headerStyle);
				}

				// Comparison Logic
				int sheetRowIdx = 1;
				int localMatched = 0;
				int localMismatches = 0;
				List<String> localDiscrepancies = new ArrayList<>();

				if (task.isIgnoreRowOrder()) {
					// Compare using primary key
					String primaryKey = task.getPrimaryKeyColumn();
					int keyIdx = resolvePrimaryKeyIndex(expectedHeaders, primaryKey);

					Map<String, List<IndexedRow>> expectedByKey = indexRowsByKey(rawExpectedRows, keyIdx);
					Map<String, List<IndexedRow>> actualByKey = indexRowsByKey(rawActualRows, keyIdx);

					Set<String> allKeys = new LinkedHashSet<>();
					allKeys.addAll(expectedByKey.keySet());
					allKeys.addAll(actualByKey.keySet());

					for (String key : allKeys) {
						List<IndexedRow> expMatches = expectedByKey.getOrDefault(key, Collections.emptyList());
						List<IndexedRow> actMatches = actualByKey.getOrDefault(key, Collections.emptyList());

						int maxMatches = Math.max(expMatches.size(), actMatches.size());
						for (int m = 0; m < maxMatches; m++) {
							List<String> expRow = m < expMatches.size() ? expMatches.get(m).values : null;
							List<String> actRow = m < actMatches.size() ? actMatches.get(m).values : null;

							sheetRowIdx = writeComparisonPair(sheet, sheetRowIdx, expectedHeaders, expRow, actRow,
									expectedStyle, actualStyle, mismatchStyle, warningStyle,
									task.isCaseInsensitiveData());

							if (expRow != null && actRow != null) {
								int maxCols = expectedHeaders.size();
								for (int col = 0; col < maxCols; col++) {
									String val1 = col < expRow.size() ? expRow.get(col) : "";
									String val2 = col < actRow.size() ? actRow.get(col) : "";
									if (normalize(val1, task.isCaseInsensitiveData()).equals(normalize(val2, task.isCaseInsensitiveData()))) {
										localMatched++;
									} else {
										localMismatches++;
										String disc = "Field mismatch at primary key '" + key 
												+ "', column '" + expectedHeaders.get(col) 
												+ "': expected='" + val1 + "', actual='" + val2 + "'";
										localDiscrepancies.add(disc);
										Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
									}
								}
							} else if (expRow != null) {
								localMismatches += expectedHeaders.size();
								String disc = "Missing record for primary key: '" + key + "'";
								localDiscrepancies.add(disc);
								Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
							} else {
								localMismatches += expectedHeaders.size();
								String disc = "Unexpected record for primary key: '" + key + "'";
								localDiscrepancies.add(disc);
								Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
							}
						}
					}

				} else {
					// Sequential ordered comparison
					int maxRows = Math.max(rawExpectedRows.size(), rawActualRows.size());
					for (int r = 0; r < maxRows; r++) {
						List<String> expRow = r < rawExpectedRows.size() ? rawExpectedRows.get(r) : null;
						List<String> actRow = r < rawActualRows.size() ? rawActualRows.get(r) : null;

						sheetRowIdx = writeComparisonPair(sheet, sheetRowIdx, expectedHeaders, expRow, actRow,
								expectedStyle, actualStyle, mismatchStyle, warningStyle,
								task.isCaseInsensitiveData());

						if (expRow != null && actRow != null) {
							int maxCols = expectedHeaders.size();
							for (int col = 0; col < maxCols; col++) {
								String val1 = col < expRow.size() ? expRow.get(col) : "";
								String val2 = col < actRow.size() ? actRow.get(col) : "";
								if (normalize(val1, task.isCaseInsensitiveData()).equals(normalize(val2, task.isCaseInsensitiveData()))) {
									localMatched++;
								} else {
									localMismatches++;
									String disc = "Field mismatch at row " + (r + 1) 
											+ ", column '" + expectedHeaders.get(col) 
											+ "': expected='" + val1 + "', actual='" + val2 + "'";
									localDiscrepancies.add(disc);
									Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
								}
							}
						} else if (expRow != null) {
							localMismatches += expectedHeaders.size();
							String disc = "Missing row at index " + (r + 1);
							localDiscrepancies.add(disc);
							Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
						} else {
							localMismatches += expectedHeaders.size();
							String disc = "Unexpected row at index " + (r + 1);
							localDiscrepancies.add(disc);
							Log.warn("[ExcelComparisonEngine] Discrepancy detected: " + disc);
						}
					}
				}

				// Auto-size all columns
				for (int col = 0; col < expectedHeaders.size(); col++) {
					sheet.autoSizeColumn(col);
					// Set minimum width to avoid truncated column headers
					int currentWidth = sheet.getColumnWidth(col);
					if (currentWidth < 3000) {
						sheet.setColumnWidth(col, 3500);
					}
				}

				totalMatched += localMatched;
				totalMismatches += localMismatches;

				boolean scenarioPassed = (localMismatches == 0);
				if (!scenarioPassed) {
					overallSuccess = false;
				}

				String status = scenarioPassed ? "PASSED" : "FAILED";
				detailsLog.add("Scenario " + task.getScenarioName() + " | Status: " + status
						+ " | Matched: " + localMatched + " | Mismatches: " + localMismatches);

				scenarioResults.add(new ScenarioResult(
					task.getScenarioName(), task.getScenarioKey(), expectedFile, actualFile,
					scenarioPassed, localMatched, localMismatches, localDiscrepancies
				));

				Log.info("[ExcelComparisonEngine] Scenario " + task.getScenarioName() + " comparison completed: " + status);
			}

			// Write to file
			try (FileOutputStream fos = new FileOutputStream(reportPath.toFile())) {
				workbook.write(fos);
			}
		}

		Log.info("[ExcelComparisonEngine] Excel comparison report successfully generated at: " + reportPath.toAbsolutePath());
		return new ComparisonSummary(intentName, workflowKey, reportPath, overallSuccess, totalMatched, totalMismatches, detailsLog, scenarioResults);
	}

	private static int writeComparisonPair(Sheet sheet, int startRowIdx, List<String> headers,
											List<String> expectedRow, List<String> actualRow,
											CellStyle expectedStyle, CellStyle actualStyle,
											CellStyle mismatchStyle, CellStyle warningStyle,
											boolean caseInsensitiveData) {
		int colCount = headers.size();

		// Row 1: Expected (Source)
		Row rowExp = sheet.createRow(startRowIdx++);
		if (expectedRow != null) {
			for (int col = 0; col < colCount; col++) {
				Cell cell = rowExp.createCell(col);
				String val = col < expectedRow.size() ? expectedRow.get(col) : "";
				cell.setCellValue(val);
				cell.setCellStyle(expectedStyle);
			}
		} else {
			// Expected row is missing (unexpected record in actual)
			for (int col = 0; col < colCount; col++) {
				Cell cell = rowExp.createCell(col);
				if (col == 0) {
					cell.setCellValue("[UNEXPECTED RECORD]");
				} else {
					cell.setCellValue("");
				}
				cell.setCellStyle(warningStyle);
			}
		}

		// Row 2: Actual (Downloaded Output)
		Row rowAct = sheet.createRow(startRowIdx++);
		if (actualRow != null) {
			for (int col = 0; col < colCount; col++) {
				Cell cell = rowAct.createCell(col);
				String valActual = col < actualRow.size() ? actualRow.get(col) : "";
				cell.setCellValue(valActual);

				if (expectedRow != null) {
					String valExp = col < expectedRow.size() ? expectedRow.get(col) : "";
					if (normalize(valActual, caseInsensitiveData).equals(normalize(valExp, caseInsensitiveData))) {
						cell.setCellStyle(actualStyle);
					} else {
						cell.setCellStyle(mismatchStyle);
					}
				} else {
					cell.setCellStyle(mismatchStyle);
				}
			}
		} else {
			// Actual row is missing (missing record)
			for (int col = 0; col < colCount; col++) {
				Cell cell = rowAct.createCell(col);
				cell.setCellValue("[MISSING RECORD]");
				cell.setCellStyle(mismatchStyle);
			}
		}

		return startRowIdx;
	}

	private static void writeErrorSheet(Sheet sheet, String message, CellStyle style) {
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("ERROR: " + message);
		cell.setCellStyle(style);
		sheet.autoSizeColumn(0);
	}

	private static List<List<String>> readDataRowsRaw(Path file, int sheetIndex, int headerRowIndex) throws IOException {
		try (InputStream is = Files.newInputStream(file); Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) {
				return Collections.emptyList();
			}
			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			List<List<String>> rows = new ArrayList<>();
			int lastRow = sheet.getLastRowNum();
			for (int r = headerRowIndex + 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) continue;

				short lastCell = row.getLastCellNum();
				List<String> values = new ArrayList<>();
				boolean hasValue = false;
				for (int c = 0; c < lastCell; c++) {
					Cell cell = row.getCell(c);
					String val = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
					values.add(val);
					if (!val.isEmpty()) hasValue = true;
				}
				if (hasValue) {
					rows.add(values);
				}
			}
			return rows;
		}
	}

	private static List<List<String>> readDataRowsAligned(Path file, int sheetIndex, int headerRowIndex,
														 List<String> expectedHeaders, boolean caseInsensitiveHeaders) throws IOException {
		try (InputStream is = Files.newInputStream(file); Workbook workbook = WorkbookFactory.create(is)) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			if (sheet == null) return Collections.emptyList();

			Row headerRow = sheet.getRow(headerRowIndex);
			if (headerRow == null) return Collections.emptyList();

			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			List<String> actualHeaders = new ArrayList<>();
			short lastHeaderCell = headerRow.getLastCellNum();
			for (int c = 0; c < lastHeaderCell; c++) {
				Cell cell = headerRow.getCell(c);
				String value = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
				actualHeaders.add(normalize(value, caseInsensitiveHeaders));
			}

			Map<String, Integer> headerIndex = new LinkedHashMap<>();
			for (int i = 0; i < actualHeaders.size(); i++) {
				String h = actualHeaders.get(i);
				if (!h.isEmpty() && !headerIndex.containsKey(h)) {
					headerIndex.put(h, i);
				}
			}

			List<List<String>> rows = new ArrayList<>();
			int lastRow = sheet.getLastRowNum();
			for (int r = headerRowIndex + 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) continue;

				List<String> values = new ArrayList<>();
				boolean hasValue = false;
				for (String header : expectedHeaders) {
					String normHeader = normalize(header, caseInsensitiveHeaders);
					Integer colIdx = headerIndex.get(normHeader);
					String value = "";
					if (colIdx != null) {
						Cell cell = row.getCell(colIdx);
						value = cell == null ? "" : formatter.formatCellValue(cell, evaluator).trim();
					}
					values.add(value);
					if (!value.isEmpty()) hasValue = true;
				}
				if (hasValue) {
					rows.add(values);
				}
			}
			return rows;
		}
	}

	private static Map<String, List<IndexedRow>> indexRowsByKey(List<List<String>> rows, int keyIdx) {
		Map<String, List<IndexedRow>> indexed = new LinkedHashMap<>();
		for (int i = 0; i < rows.size(); i++) {
			List<String> row = rows.get(i);
			String key = (keyIdx >= 0 && keyIdx < row.size()) ? row.get(keyIdx).trim().toLowerCase() : "";
			indexed.computeIfAbsent(key, k -> new ArrayList<>()).add(new IndexedRow(i, row));
		}
		return indexed;
	}

	private static int resolvePrimaryKeyIndex(List<String> headers, String primaryKeyColumn) {
		if (primaryKeyColumn == null || primaryKeyColumn.isBlank()) {
			return 0;
		}
		String trimmed = primaryKeyColumn.trim();
		try {
			int oneBased = Integer.parseInt(trimmed);
			return Math.max(0, oneBased - 1);
		} catch (NumberFormatException ignored) {
			for (int i = 0; i < headers.size(); i++) {
				if (trimmed.equalsIgnoreCase(headers.get(i))) {
					return i;
				}
			}
			return 0;
		}
	}

	private static String normalize(String value, boolean caseInsensitive) {
		if (value == null) return "";
		String normalized = value.trim().replaceAll("\\s+", " ");
		return caseInsensitive ? normalized.toLowerCase() : normalized;
	}

	private static class IndexedRow {
		final int originalIndex;
		final List<String> values;

		IndexedRow(int originalIndex, List<String> values) {
			this.originalIndex = originalIndex;
			this.values = values;
		}
	}

	public static class ScenarioComparisonTask {
		private final String scenarioName;
		private final String scenarioKey;
		private final Path expectedFile;
		private final Path actualFile;
		private final int sheetIndex;
		private final int headerRowIndex;
		private final boolean strictHeaderOrder;
		private final boolean caseInsensitiveHeaders;
		private final boolean caseInsensitiveData;
		private final boolean ignoreRowOrder;
		private final String primaryKeyColumn;

		public ScenarioComparisonTask(String scenarioName, String scenarioKey, Path expectedFile, Path actualFile,
									  int sheetIndex, int headerRowIndex, boolean strictHeaderOrder,
									  boolean caseInsensitiveHeaders, boolean caseInsensitiveData,
									  boolean ignoreRowOrder, String primaryKeyColumn) {
			this.scenarioName = scenarioName;
			this.scenarioKey = scenarioKey;
			this.expectedFile = expectedFile;
			this.actualFile = actualFile;
			this.sheetIndex = sheetIndex;
			this.headerRowIndex = headerRowIndex;
			this.strictHeaderOrder = strictHeaderOrder;
			this.caseInsensitiveHeaders = caseInsensitiveHeaders;
			this.caseInsensitiveData = caseInsensitiveData;
			this.ignoreRowOrder = ignoreRowOrder;
			this.primaryKeyColumn = primaryKeyColumn;
		}

		public String getScenarioName() { return scenarioName; }
		public String getScenarioKey() { return scenarioKey; }
		public Path getExpectedFile() { return expectedFile; }
		public Path getActualFile() { return actualFile; }
		public int getSheetIndex() { return sheetIndex; }
		public int getHeaderRowIndex() { return headerRowIndex; }
		public boolean isStrictHeaderOrder() { return strictHeaderOrder; }
		public boolean isCaseInsensitiveHeaders() { return caseInsensitiveHeaders; }
		public boolean isCaseInsensitiveData() { return caseInsensitiveData; }
		public boolean isIgnoreRowOrder() { return ignoreRowOrder; }
		public String getPrimaryKeyColumn() { return primaryKeyColumn; }
	}
}
