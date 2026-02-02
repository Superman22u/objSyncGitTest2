package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import data.PaymentManager;

public class SummaryPanel extends JPanel {
    private PaymentManager paymentManager;

    public SummaryPanel() {
        this.paymentManager = PaymentManager.getInstance();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createSummaryView();
    }

    private void createSummaryView() {
        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Financial Summary"));

        // Current month summary
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);

        BigDecimal currentMonthDebits = paymentManager.getAllPayments().stream()
                .filter(p -> p.getPaymentDate().getMonth() == now.getMonth() &&
                        p.getPaymentDate().getYear() == now.getYear() &&
                        "Debit".equals(p.getDebitCredit()))
                .map(p -> p.getAmountCHF())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentMonthCredits = paymentManager.getAllPayments().stream()
                .filter(p -> p.getPaymentDate().getMonth() == now.getMonth() &&
                        p.getPaymentDate().getYear() == now.getYear() &&
                        "Credit".equals(p.getDebitCredit()))
                .map(p -> p.getAmountCHF())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentMonthNet = currentMonthCredits.subtract(currentMonthDebits);

        summaryPanel.add(new JLabel("Current Month (" + currentMonth + "):"));
        summaryPanel.add(new JLabel(String.format("CHF %,.2f", currentMonthNet.doubleValue())));

        // Total balance
        BigDecimal totalBalance = paymentManager.getTotalBalance();
        summaryPanel.add(new JLabel("Total Balance:"));
        summaryPanel.add(new JLabel(String.format("CHF %,.2f", totalBalance.doubleValue())));

        // Number of payments
        int totalPayments = paymentManager.getAllPayments().size();
        summaryPanel.add(new JLabel("Total Payments:"));
        summaryPanel.add(new JLabel(String.valueOf(totalPayments)));

        // Monthly averages
        if (!paymentManager.getAllPayments().isEmpty()) {
            LocalDate firstPayment = paymentManager.getAllPayments().stream()
                    .map(p -> p.getPaymentDate())
                    .min(LocalDate::compareTo)
                    .orElse(now);

            long monthsBetween = currentMonth.getYear() * 12 + currentMonth.getMonthValue() -
                    (firstPayment.getYear() * 12 + firstPayment.getMonthValue()) + 1;

            if (monthsBetween > 0) {
                BigDecimal averageMonthly = totalBalance.divide(
                        BigDecimal.valueOf(monthsBetween), 2, BigDecimal.ROUND_HALF_UP);

                summaryPanel.add(new JLabel("Average Monthly:"));
                summaryPanel.add(new JLabel(String.format("CHF %,.2f", averageMonthly.doubleValue())));
            }
        }

        add(summaryPanel, BorderLayout.CENTER);
    }
}