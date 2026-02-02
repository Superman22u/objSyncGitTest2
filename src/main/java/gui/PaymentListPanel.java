package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import data.PaymentManager;
import models.Payment;

public class PaymentListPanel extends JPanel {
    private MainFrame mainFrame;
    private PaymentManager paymentManager;
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton copyButton;
    private JButton backButton;
    private JButton refreshButton;
    private JButton filterButton;
    private JComboBox<String> filterCombo;
    JButton exportButton = new JButton("Export to Excel");

    public PaymentListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.paymentManager = PaymentManager.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create components
        createTable();
        createButtonPanel();
        createFilterPanel();

        // Load data
        refreshData();
    }

    private void createTable() {
        // Column names
        String[] columns = { "ID", "Date", "Description", "Amount", "Currency",
                "Amount CHF", "Type", "Location", "Recurrence", "D/C" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 5) { // Amount and Amount CHF columns
                    return BigDecimal.class;
                }
                return String.class;
            }
        };

        paymentTable = new JTable(tableModel);

        // Customize table appearance
        paymentTable.setRowHeight(25);
        paymentTable.setAutoCreateRowSorter(true);
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        TableColumnModel columnModel = paymentTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // ID
        columnModel.getColumn(1).setPreferredWidth(100); // Date
        columnModel.getColumn(2).setPreferredWidth(200); // Description
        columnModel.getColumn(3).setPreferredWidth(80); // Amount
        columnModel.getColumn(4).setPreferredWidth(60); // Currency
        columnModel.getColumn(5).setPreferredWidth(100); // Amount CHF
        columnModel.getColumn(6).setPreferredWidth(100); // Type
        columnModel.getColumn(7).setPreferredWidth(100); // Location
        columnModel.getColumn(8).setPreferredWidth(100); // Recurrence
        columnModel.getColumn(9).setPreferredWidth(60); // D/C

        // Center align ID and D/C columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        paymentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        paymentTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        // Right align amount columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        paymentTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        paymentTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        // Add mouse listener for double-click to edit
        paymentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedPayment();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Payment List"));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));

        filterPanel.add(new JLabel("Show:"));

        filterCombo = new JComboBox<>(new String[] {
                "All Payments",
                "This Month",
                "Last Month",
                "This Year",
                "Debits Only",
                "Credits Only"
        });

        filterCombo.addActionListener(e -> applyFilter());

        filterPanel.add(filterCombo);

        filterButton = new JButton("Custom Date Range");
        filterButton.addActionListener(e -> showDateFilterDialog());
        filterPanel.add(filterButton);

        add(filterPanel, BorderLayout.NORTH);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedPayment());

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedPayment());

        copyButton = new JButton("Duplicate");
        copyButton.addActionListener(e -> duplicateSelectedPayment());

        backButton = new JButton("Back to Entry");
        backButton.addActionListener(e -> mainFrame.showEntryPanel());

        buttonPanel.add(refreshButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(copyButton);

        buttonPanel.add(exportButton);
        exportButton.addActionListener(e -> exportToExcel());

        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);

        List<Payment> payments = paymentManager.getAllPayments();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Payment payment : payments) {
            Object[] row = {
                    payment.getId(),
                    payment.getPaymentDate().format(dateFormatter),
                    payment.getDescription(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getAmountCHF(),
                    paymentManager.getTypeName(payment.getTypeId()),
                    paymentManager.getLocationName(payment.getLocationId()),
                    payment.getRecurrence(),
                    payment.getDebitCredit()
            };
            tableModel.addRow(row);
        }

        updateSummary();
    }

    private void updateSummary() {
        int rowCount = tableModel.getRowCount();
        BigDecimal totalCHF = BigDecimal.ZERO;

        for (int i = 0; i < rowCount; i++) {
            BigDecimal amountCHF = (BigDecimal) tableModel.getValueAt(i, 5);
            String debitCredit = (String) tableModel.getValueAt(i, 9);

            if ("Debit".equals(debitCredit)) {
                totalCHF = totalCHF.add(amountCHF);
            } else if ("Credit".equals(debitCredit)) {
                totalCHF = totalCHF.subtract(amountCHF);
            }
        }

        // Update status bar or display
        String summary = String.format("Total Payments: %d | Net Amount CHF: %.2f",
                rowCount, totalCHF.doubleValue());

        // You can add this to a status bar or display it somewhere
        System.out.println(summary);
    }

    private void exportToExcel() {
        boolean success = ExcelExporter.exportTableToCSV(paymentTable, "Payment_List");
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Payment list exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        List<Payment> filteredPayments;

        LocalDate now = LocalDate.now();

        switch (filter) {
            case "This Month":
                filteredPayments = paymentManager.getPaymentsByMonth(now.getYear(), now.getMonthValue());
                break;

            case "Last Month":
                LocalDate lastMonth = now.minusMonths(1);
                filteredPayments = paymentManager.getPaymentsByMonth(lastMonth.getYear(), lastMonth.getMonthValue());
                break;

            case "This Year":
                filteredPayments = paymentManager.getPaymentsByDateRange(
                        LocalDate.of(now.getYear(), 1, 1),
                        LocalDate.of(now.getYear(), 12, 31));
                break;

            case "Debits Only":
                filteredPayments = paymentManager.getAllPayments().stream()
                        .filter(p -> "Debit".equals(p.getDebitCredit()))
                        .collect(Collectors.toList());
                break;

            case "Credits Only":
                filteredPayments = paymentManager.getAllPayments().stream()
                        .filter(p -> "Credit".equals(p.getDebitCredit()))
                        .collect(Collectors.toList());
                break;

            default: // "All Payments"
                filteredPayments = paymentManager.getAllPayments();
        }

        displayFilteredPayments(filteredPayments);
    }

    private void displayFilteredPayments(List<Payment> payments) {
        tableModel.setRowCount(0);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Payment payment : payments) {
            Object[] row = {
                    payment.getId(),
                    payment.getPaymentDate().format(dateFormatter),
                    payment.getDescription(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getAmountCHF(),
                    paymentManager.getTypeName(payment.getTypeId()),
                    paymentManager.getLocationName(payment.getLocationId()),
                    payment.getRecurrence(),
                    payment.getDebitCredit()
            };
            tableModel.addRow(row);
        }
    }

    private void showDateFilterDialog() {
        JDialog filterDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Custom Date Range", true);
        filterDialog.setSize(300, 200);
        filterDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField startDateField = new JTextField(LocalDate.now().withDayOfMonth(1).toString());
        JTextField endDateField = new JTextField(LocalDate.now().toString());

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
        panel.add(endDateField);

        JButton applyButton = new JButton("Apply Filter");
        applyButton.addActionListener(e -> {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(filterDialog,
                            "Start date must be before end date.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<Payment> filtered = paymentManager.getPaymentsByDateRange(startDate, endDate);
                displayFilteredPayments(filtered);
                filterDialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(filterDialog,
                        "Invalid date format. Use YYYY-MM-DD.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> filterDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        filterDialog.add(panel, BorderLayout.CENTER);
        filterDialog.add(buttonPanel, BorderLayout.SOUTH);
        filterDialog.setVisible(true);
    }

    private void editSelectedPayment() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = paymentTable.convertRowIndexToModel(selectedRow);
            int paymentId = (Integer) tableModel.getValueAt(modelRow, 0);

            // TODO: Implement edit dialog
            JOptionPane.showMessageDialog(this,
                    "Edit functionality for payment ID: " + paymentId + " would open here.",
                    "Edit Payment",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedPayment() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = paymentTable.convertRowIndexToModel(selectedRow);
            int paymentId = (Integer) tableModel.getValueAt(modelRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete payment ID: " + paymentId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (paymentManager.deletePayment(paymentId)) {
                    refreshData();
                    JOptionPane.showMessageDialog(this,
                            "Payment deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete payment.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void duplicateSelectedPayment() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = paymentTable.convertRowIndexToModel(selectedRow);
            int paymentId = (Integer) tableModel.getValueAt(modelRow, 0);

            Payment original = paymentManager.getPaymentById(paymentId);
            if (original != null) {
                // Create a copy with today's date
                Payment copy = new Payment(
                        LocalDate.now(),
                        original.getDescription() + " (Copy)",
                        original.getAmount(),
                        original.getCurrency(),
                        original.getAmountCHF(),
                        original.getExchangeRate(),
                        original.getRecurrence(),
                        original.getDebitCredit(),
                        original.getLocationId(),
                        original.getTypeId());

                int newId = paymentManager.addPayment(copy);
                refreshData();

                JOptionPane.showMessageDialog(this,
                        String.format("Payment duplicated! New ID: %d", newId),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a payment to duplicate.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}