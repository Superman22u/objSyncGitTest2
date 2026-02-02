package gui;

import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExporterPOI {

    public static boolean exportTableToExcel(JTable table, String sheetName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as Excel");
        fileChooser.setSelectedFile(new java.io.File(sheetName + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (Workbook workbook = new XSSFWorkbook();
                    FileOutputStream fileOut = new FileOutputStream(fileToSave)) {

                Sheet sheet = workbook.createSheet(sheetName);
                TableModel model = table.getModel();

                // Create header row with style
                Row headerRow = sheet.createRow(0);
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                CellStyle currencyStyle = workbook.createCellStyle();
                currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

                for (int rowIdx = 0; rowIdx < model.getRowCount(); rowIdx++) {
                    Row row = sheet.createRow(rowIdx + 1);

                    for (int colIdx = 0; colIdx < model.getColumnCount(); colIdx++) {
                        Cell cell = row.createCell(colIdx);
                        Object value = model.getValueAt(rowIdx, colIdx);

                        if (value != null) {
                            if (value instanceof Number) {
                                cell.setCellValue(((Number) value).doubleValue());
                                if (colIdx == 3 || colIdx == 5 || colIdx == 8) { // Amount columns
                                    cell.setCellStyle(currencyStyle);
                                }
                            } else {
                                cell.setCellValue(value.toString());
                            }
                        }
                    }
                }

                // Auto-size columns
                for (int i = 0; i < model.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(fileOut);

                JOptionPane.showMessageDialog(null,
                        "Excel file created successfully:\n" + fileToSave.getAbsolutePath(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                return true;

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error creating Excel file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}