package gui;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ExcelExporter {

    public static boolean exportTableToCSV(JTable table, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save " + title + " to CSV");

        // Generate default filename with timestamp
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setSelectedFile(new java.io.File(title + "_" + timestamp + ".csv"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave)) {
                TableModel model = table.getModel();

                // Write headers
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");

                // Write data
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        String cellValue = (value != null) ? value.toString() : "";

                        // Quote strings that contain commas
                        if (cellValue.contains(",") || cellValue.contains("\"")) {
                            cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                        }

                        writer.write(cellValue);
                        if (j < model.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }

                return true;

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error exporting to CSV: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean exportTimelineToCSV(JTable table, String title,
            LocalDate startDate, LocalDate endDate) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Timeline to CSV");

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String defaultName = String.format("Timeline_%s_to_%s_%s.csv",
                startDate, endDate, timestamp);
        fileChooser.setSelectedFile(new java.io.File(defaultName));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {

                // Add metadata
                csvPrinter.printComment("Payment Timeline Export");
                csvPrinter.printComment("Generated: " + LocalDateTime.now().toString());
                csvPrinter.printComment("Period: " + startDate + " to " + endDate);
                csvPrinter.println();

                // Write headers
                TableModel model = table.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    csvPrinter.print(model.getColumnName(i));
                }
                csvPrinter.println();

                // Write data
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        csvPrinter.print(value != null ? value.toString() : "");
                    }
                    csvPrinter.println();
                }

                return true;

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error exporting to CSV: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}