package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import data.PaymentManager;
import models.Payment;

public class TimelinePanel2 extends JPanel {
    private MainFrame mainFrame;
    private PaymentManager paymentManager;
    private JTable timelineTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton exportButton;
    private JButton backButton;
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> viewTypeCombo;
    private JLabel balanceLabel;

    public TimelinePanel2(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.paymentManager = PaymentManager.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create components
        createFilterPanel();
        createTable();
        createButtonPanel();

        // Load data with default date range (current month)
        loadDataWithDefaultRange();
    }

    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Timeline Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Start Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Start Date:"), gbc);
        startDateField = new JTextField(12);
        gbc.gridx = 1;
        filterPanel.add(startDateField, gbc);

        // End Date
        gbc.gridx = 2;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("End Date:"), gbc);
        endDateField = new JTextField(12);
        gbc.gridx = 3;
        filterPanel.add(endDateField, gbc);

        // View Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("View Type:"), gbc);
        viewTypeCombo = new JComboBox<>(new String[] {
                "All Entries", "Monthly Summary", "Weekly Summary", "Daily Details"
        });
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(viewTypeCombo, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Apply Filter Button
        gbc.gridx = 4;
        gbc.gridy = 0;
        JButton applyFilterButton = new JButton("Apply");
        applyFilterButton.addActionListener(e -> refreshData());
        filterPanel.add(applyFilterButton, gbc);

        // Current Balance Label
        gbc.gridx = 4;
        gbc.gridy = 1;
        balanceLabel = new JLabel("Balance: CHF 0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        filterPanel.add(balanceLabel, gbc);

        add(filterPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        // Column names for timeline view
        String[] columns = { "Date", "Type", "Description", "Amount", "Currency",
                "Amount CHF", "Recurrence", "Location", "Running Balance" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 5 || columnIndex == 8) {
                    return BigDecimal.class;
                }
                return String.class;
            }
        };

        timelineTable = new JTable(tableModel);

        // Customize table appearance
        timelineTable.setRowHeight(25);
        timelineTable.setAutoCreateRowSorter(false); // No sorting for timeline
        timelineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        TableColumnModel columnModel = timelineTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Date
        columnModel.getColumn(1).setPreferredWidth(80); // Type
        columnModel.getColumn(2).setPreferredWidth(200); // Description
        columnModel.getColumn(3).setPreferredWidth(80); // Amount
        columnModel.getColumn(4).setPreferredWidth(60); // Currency
        columnModel.getColumn(5).setPreferredWidth(100); // Amount CHF
        columnModel.getColumn(6).setPreferredWidth(100); // Recurrence
        columnModel.getColumn(7).setPreferredWidth(100); // Location
        columnModel.getColumn(8).setPreferredWidth(120); // Running Balance

        // Right align amount and balance columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        timelineTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        timelineTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        timelineTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Color coding for debit/credit
        timelineTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Get the type (debit/credit) from the Type column
                String type = (String) table.getValueAt(row, 1);
                if ("Debit".equals(type)) {
                    c.setForeground(new Color(200, 0, 0)); // Red for debits
                } else if ("Credit".equals(type)) {
                    c.setForeground(new Color(0, 150, 0)); // Green for credits
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(timelineTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Payment Timeline with Balance"));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        exportButton = new JButton("Export to Excel");
        // exportButton.addActionListener(e -> exportToExcel());
        exportButton.addActionListener(e -> exportToExcel2());

        backButton = new JButton("Back to Main");
        backButton.addActionListener(e -> mainFrame.showEntryPanel());

        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDataWithDefaultRange() {
        // Set default date range to current month
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        startDateField.setText(firstDayOfMonth.toString());
        endDateField.setText(lastDayOfMonth.toString());

        refreshData();
    }

    public void refreshData() {
        try {
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate endDate = LocalDate.parse(endDateField.getText());

            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this,
                        "Start date must be before end date.",
                        "Invalid Date Range",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Payment> allPayments = paymentManager.getAllPayments();
            List<TimelineEntry> timelineEntries = generateTimelineEntries(
                    allPayments, startDate, endDate);

            displayTimeline(timelineEntries);
            updateBalanceLabel(timelineEntries);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Date Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<TimelineEntry> generateTimelineEntries(List<Payment> payments,
            LocalDate startDate, LocalDate endDate) {
        List<TimelineEntry> entries = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        // First, add all one-time payments within date range
        for (Payment payment : payments) {
            if (payment.getPaymentDate().isBefore(startDate) ||
                    payment.getPaymentDate().isAfter(endDate)) {
                continue;
            }

            if ("One-time".equals(payment.getRecurrence())) {
                runningBalance = updateBalance(runningBalance, payment);
                entries.add(new TimelineEntry(payment, runningBalance));
            }
        }

        // Then add recurring payments
        for (Payment payment : payments) {
            if ("One-time".equals(payment.getRecurrence())) {
                continue; // Already processed
            }

            List<LocalDate> occurrenceDates = calculateOccurrences(
                    payment, startDate, endDate);

            for (LocalDate occurrenceDate : occurrenceDates) {
                // Create a copy of the payment for this occurrence
                Payment occurrence = createOccurrencePayment(payment, occurrenceDate);
                runningBalance = updateBalance(runningBalance, occurrence);
                entries.add(new TimelineEntry(occurrence, runningBalance));
            }
        }

        // Sort by date
        entries.sort((e1, e2) -> e1.payment.getPaymentDate().compareTo(e2.payment.getPaymentDate()));

        return entries;
    }

    private List<LocalDate> calculateOccurrences(Payment payment,
            LocalDate startDate, LocalDate endDate) {
        List<LocalDate> occurrences = new ArrayList<>();
        LocalDate paymentDate = payment.getPaymentDate();
        String recurrence = payment.getRecurrence();

        if (paymentDate.isAfter(endDate)) {
            return occurrences;
        }

        LocalDate currentDate = paymentDate;

        while (!currentDate.isAfter(endDate)) {
            if (!currentDate.isBefore(startDate)) {
                occurrences.add(currentDate);
            }

            // Calculate next occurrence based on recurrence
            currentDate = calculateNextOccurrence(currentDate, recurrence);
        }

        return occurrences;
    }

    private LocalDate calculateNextOccurrence(LocalDate currentDate, String recurrence) {
        switch (recurrence) {
            case "Daily":
                return currentDate.plusDays(1);

            case "Weekly":
                return currentDate.plusWeeks(1);

            case "Every 2 weeks":
                return currentDate.plusWeeks(2);

            case "Monthly":
                return currentDate.plusMonths(1);

            case "Every 3 months":
                return currentDate.plusMonths(3);

            case "Yearly":
                return currentDate.plusYears(1);

            case "Before holiday":
                // For simplicity, assume before major holidays
                return currentDate.plusYears(1);

            case "After holiday":
                // For simplicity, assume after major holidays
                return currentDate.plusYears(1);

            default:
                return currentDate.plusMonths(1);
        }
    }

    private Payment createOccurrencePayment(Payment original, LocalDate occurrenceDate) {
        Payment occurrence = new Payment(
                occurrenceDate,
                original.getDescription() + " (Recurring)",
                original.getAmount(),
                original.getCurrency(),
                original.getAmountCHF(),
                original.getExchangeRate(),
                original.getRecurrence(),
                original.getDebitCredit(),
                original.getLocationId(),
                original.getTypeId());
        occurrence.setId(original.getId()); // Keep original ID for reference
        return occurrence;
    }

    private BigDecimal updateBalance(BigDecimal balance, Payment payment) {
        if ("Debit".equals(payment.getDebitCredit())) {
            return balance.subtract(payment.getAmountCHF());
        } else if ("Credit".equals(payment.getDebitCredit())) {
            return balance.add(payment.getAmountCHF());
        }
        return balance;
    }

    private void displayTimeline(List<TimelineEntry> entries) {
        tableModel.setRowCount(0);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TimelineEntry entry : entries) {
            Payment payment = entry.payment;

            Object[] row = {
                    payment.getPaymentDate().format(dateFormatter),
                    payment.getDebitCredit(),
                    payment.getDescription(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getAmountCHF(),
                    payment.getRecurrence(),
                    paymentManager.getLocationName(payment.getLocationId()),
                    entry.runningBalance.setScale(2, BigDecimal.ROUND_HALF_UP)
            };
            tableModel.addRow(row);
        }
    }

    private void updateBalanceLabel(List<TimelineEntry> entries) {
        if (entries.isEmpty()) {
            balanceLabel.setText("Balance: CHF 0.00");
            return;
        }

        BigDecimal finalBalance = entries.get(entries.size() - 1).runningBalance;
        balanceLabel.setText(String.format("Final Balance: CHF %,.2f",
                finalBalance.doubleValue()));
    }

    private void exportToExcel2() {
        try {
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate endDate = LocalDate.parse(endDateField.getText());

            boolean success = ExcelExporter.exportTimelineToCSV(
                    timelineTable, "Payment_Timeline", startDate, endDate);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Timeline exported successfully!\n" +
                                "Period: " + startDate + " to " + endDate,
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Timeline to CSV (Excel)");
        fileChooser.setSelectedFile(new java.io.File("payment_timeline.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                            .withHeader("Date", "Type", "Description", "Amount", "Currency",
                                    "Amount CHF", "Recurrence", "Location", "Running Balance"))) {

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    csvPrinter.printRecord(
                            tableModel.getValueAt(i, 0), // Date
                            tableModel.getValueAt(i, 1), // Type
                            tableModel.getValueAt(i, 2), // Description
                            tableModel.getValueAt(i, 3), // Amount
                            tableModel.getValueAt(i, 4), // Currency
                            tableModel.getValueAt(i, 5), // Amount CHF
                            tableModel.getValueAt(i, 6), // Recurrence
                            tableModel.getValueAt(i, 7), // Location
                            tableModel.getValueAt(i, 8) // Running Balance
                    );
                }

                JOptionPane.showMessageDialog(this,
                        "Timeline exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting to CSV: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Helper class for timeline entries
    private static class TimelineEntry {
        Payment payment;
        BigDecimal runningBalance;

        TimelineEntry(Payment payment, BigDecimal runningBalance) {
            this.payment = payment;
            this.runningBalance = runningBalance;
        }
    }
}