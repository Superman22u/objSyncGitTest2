package models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    private int id;
    private LocalDate paymentDate;
    private String description;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountCHF;
    private BigDecimal exchangeRate;
    private String recurrence;
    private String debitCredit; // "Debit" or "Credit"
    private int locationId;
    private int typeId;
    private LocalDate createdAt;

    // Constructors
    public Payment() {
        this.createdAt = LocalDate.now();
    }

    public Payment(LocalDate paymentDate, String description, BigDecimal amount,
            String currency, BigDecimal amountCHF, BigDecimal exchangeRate,
            String recurrence, String debitCredit, int locationId, int typeId) {
        this();
        this.paymentDate = paymentDate;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.amountCHF = amountCHF;
        this.exchangeRate = exchangeRate;
        this.recurrence = recurrence;
        this.debitCredit = debitCredit;
        this.locationId = locationId;
        this.typeId = typeId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmountCHF() {
        return amountCHF;
    }

    public void setAmountCHF(BigDecimal amountCHF) {
        this.amountCHF = amountCHF;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    public String getDebitCredit() {
        return debitCredit;
    }

    public void setDebitCredit(String debitCredit) {
        this.debitCredit = debitCredit;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Payment{id=%d, date=%s, desc='%s', amount=%s %s}",
                id, paymentDate, description, amount, currency);
    }
}