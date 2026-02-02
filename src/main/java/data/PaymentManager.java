package data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import models.Payment;

public class PaymentManager {
    private static PaymentManager instance;
    private List<Payment> payments;
    private Map<Integer, String> locationNames;
    private Map<Integer, String> typeNames;

    private PaymentManager() {
        payments = new ArrayList<>();
        locationNames = new HashMap<>();
        typeNames = new HashMap<>();
        loadData();
    }

    public static synchronized PaymentManager getInstance() {
        if (instance == null) {
            instance = new PaymentManager();
        }
        return instance;
    }

    private void loadData() {
        try {
            // Load locations and types first for name mapping
            loadLocations();
            loadTypes();

            // Load payments
            List<String[]> paymentData = FileStorage.loadPayments();
            if (paymentData.size() <= 1)
                return; // Only header or empty

            for (int i = 1; i < paymentData.size(); i++) { // Skip header
                String[] row = paymentData.get(i);
                if (row.length >= 12) {
                    Payment payment = new Payment();
                    payment.setId(Integer.parseInt(row[0]));
                    payment.setPaymentDate(LocalDate.parse(row[1]));
                    payment.setDescription(row[2]);
                    payment.setAmount(new BigDecimal(row[3]));
                    payment.setCurrency(row[4]);
                    payment.setAmountCHF(new BigDecimal(row[5]));
                    if (!row[6].isEmpty()) {
                        payment.setExchangeRate(new BigDecimal(row[6]));
                    }
                    payment.setRecurrence(row[7]);
                    payment.setDebitCredit(row[8]);
                    payment.setLocationId(Integer.parseInt(row[9]));
                    payment.setTypeId(Integer.parseInt(row[10]));
                    payment.setCreatedAt(LocalDate.parse(row[11]));

                    payments.add(payment);
                }
            }

            // Sort by payment date descending
            payments.sort((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()));

        } catch (Exception e) {
            System.err.println("Error loading payment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLocations() {
        try {
            List<String[]> locationData = FileStorage.loadLocations();
            if (locationData.size() <= 1)
                return;

            for (int i = 1; i < locationData.size(); i++) {
                String[] row = locationData.get(i);
                if (row.length >= 4) {
                    int id = Integer.parseInt(row[0]);
                    String name = row[1];
                    locationNames.put(id, name);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading location data: " + e.getMessage());
        }
    }

    private void loadTypes() {
        try {
            List<String[]> typeData = FileStorage.loadTypes();
            if (typeData.size() <= 1)
                return;

            for (int i = 1; i < typeData.size(); i++) {
                String[] row = typeData.get(i);
                if (row.length >= 3) {
                    int id = Integer.parseInt(row[0]);
                    String name = row[1];
                    typeNames.put(id, name);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading type data: " + e.getMessage());
        }
    }

    private void savePayments() {
        try {
            List<String[]> data = new ArrayList<>();
            // Add header
            data.add(new String[] { "id", "paymentDate", "description", "amount", "currency",
                    "amountCHF", "exchangeRate", "recurrence", "debitCredit",
                    "locationId", "typeId", "createdAt" });

            // Add payment data
            for (Payment payment : payments) {
                data.add(new String[] {
                        String.valueOf(payment.getId()),
                        payment.getPaymentDate().toString(),
                        payment.getDescription(),
                        payment.getAmount().toString(),
                        payment.getCurrency(),
                        payment.getAmountCHF().toString(),
                        payment.getExchangeRate() != null ? payment.getExchangeRate().toString() : "",
                        payment.getRecurrence(),
                        payment.getDebitCredit(),
                        String.valueOf(payment.getLocationId()),
                        String.valueOf(payment.getTypeId()),
                        payment.getCreatedAt().toString()
                });
            }

            FileStorage.savePayments(data);

        } catch (Exception e) {
            System.err.println("Error saving payment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // CRUD Operations
    public int addPayment(Payment payment) {
        payment.setId(FileStorage.getNextPaymentId());
        payments.add(payment);
        savePayments();
        return payment.getId();
    }

    public boolean updatePayment(Payment updatedPayment) {
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId() == updatedPayment.getId()) {
                payments.set(i, updatedPayment);
                savePayments();
                return true;
            }
        }
        return false;
    }

    public boolean deletePayment(int id) {
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getId() == id) {
                payments.remove(i);
                savePayments();
                return true;
            }
        }
        return false;
    }

    public Payment getPaymentById(int id) {
        for (Payment payment : payments) {
            if (payment.getId() == id) {
                return payment;
            }
        }
        return null;
    }

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }

    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return payments.stream()
                .filter(p -> !p.getPaymentDate().isBefore(startDate) &&
                        !p.getPaymentDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public List<Payment> getPaymentsByMonth(int year, int month) {
        return payments.stream()
                .filter(p -> p.getPaymentDate().getYear() == year &&
                        p.getPaymentDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public String getLocationName(int locationId) {
        return locationNames.getOrDefault(locationId, "Unknown");
    }

    public String getTypeName(int typeId) {
        return typeNames.getOrDefault(typeId, "Unknown");
    }

    public Map<Integer, String> getLocationNames() {
        return new HashMap<>(locationNames);
    }

    public Map<Integer, String> getTypeNames() {
        return new HashMap<>(typeNames);
    }

    // Statistics
    public BigDecimal getTotalAmountByMonth(int year, int month) {
        return payments.stream()
                .filter(p -> p.getPaymentDate().getYear() == year &&
                        p.getPaymentDate().getMonthValue() == month &&
                        "Debit".equals(p.getDebitCredit()))
                .map(Payment::getAmountCHF)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getCategoryTotals(LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> totals = new HashMap<>();

        for (Payment payment : payments) {
            if (!payment.getPaymentDate().isBefore(startDate) &&
                    !payment.getPaymentDate().isAfter(endDate)) {
                String typeName = getTypeName(payment.getTypeId());
                BigDecimal current = totals.getOrDefault(typeName, BigDecimal.ZERO);
                totals.put(typeName, current.add(payment.getAmountCHF()));
            }
        }

        return totals;
    }

    public void refreshData() {
        payments.clear();
        locationNames.clear();
        typeNames.clear();
        loadData();
    }

    // Add these methods to your existing PaymentManager class

    public List<TimelineEntry> getTimelineEntries(LocalDate startDate, LocalDate endDate) {
        List<TimelineEntry> entries = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        // Get all payments within date range
        List<Payment> allPayments = getAllPayments().stream()
                .filter(p -> !p.getPaymentDate().isBefore(startDate) &&
                        !p.getPaymentDate().isAfter(endDate))
                .collect(java.util.stream.Collectors.toList());

        // Sort by date
        allPayments.sort((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()));

        // Calculate running balance
        for (Payment payment : allPayments) {
            if ("Debit".equals(payment.getDebitCredit())) {
                runningBalance = runningBalance.subtract(payment.getAmountCHF());
            } else if ("Credit".equals(payment.getDebitCredit())) {
                runningBalance = runningBalance.add(payment.getAmountCHF());
            }

            entries.add(new TimelineEntry(payment, runningBalance));
        }

        return entries;
    }

    public BigDecimal getTotalBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        for (Payment payment : payments) {
            if ("Debit".equals(payment.getDebitCredit())) {
                balance = balance.subtract(payment.getAmountCHF());
            } else if ("Credit".equals(payment.getDebitCredit())) {
                balance = balance.add(payment.getAmountCHF());
            }
        }

        return balance;
    }

    public Map<String, BigDecimal> getMonthlyBalances(int year) {
        Map<String, BigDecimal> monthlyBalances = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            List<Payment> monthlyPayments = getPaymentsByMonth(year, month);
            BigDecimal monthBalance = BigDecimal.ZERO;

            for (Payment payment : monthlyPayments) {
                if ("Debit".equals(payment.getDebitCredit())) {
                    monthBalance = monthBalance.subtract(payment.getAmountCHF());
                } else if ("Credit".equals(payment.getDebitCredit())) {
                    monthBalance = monthBalance.add(payment.getAmountCHF());
                }
            }

            String monthName = String.format("%04d-%02d", year, month);
            monthlyBalances.put(monthName, monthBalance);
        }

        return monthlyBalances;
    }

    // Helper class for timeline entries
    public static class TimelineEntry {
        private Payment payment;
        public BigDecimal runningBalance;

        public TimelineEntry(Payment payment, BigDecimal runningBalance) {
            this.payment = payment;
            this.runningBalance = runningBalance;
        }

        public Payment getPayment() {
            return payment;
        }

        public BigDecimal getRunningBalance() {
            return runningBalance;
        }
    }
}