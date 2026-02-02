package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import data.LocationManager;
import data.PaymentManager;
import data.TypeManager;
import gui.components.SimpleDatePicker;
import models.Payment;
import models.PaymentLocation;
import models.PaymentType;
import utils.SwissLocale;

public class PaymentEntryPanel extends JPanel {
    private MainFrame mainFrame;
    private PaymentManager paymentManager;
    private LocationManager locationManager;
    private TypeManager typeManager;

    // Form components with Swiss locale
    private SimpleDatePicker datePicker; // Changed from JTextField
    private JTextField descriptionField;
    private JFormattedTextField amountField;
    private JComboBox<String> currencyCombo;
    private JFormattedTextField amountCHFField;
    private JFormattedTextField exchangeRateField;
    private JComboBox<String> recurrenceCombo;
    private JComboBox<String> debitCreditCombo;
    private JComboBox<String> locationCombo;
    private JComboBox<String> typeCombo;

    // Number formatters for Swiss locale
    private DecimalFormat amountFormatter;
    private DecimalFormat chfFormatter;

    public PaymentEntryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.paymentManager = PaymentManager.getInstance();
        this.locationManager = LocationManager.getInstance();
        this.typeManager = TypeManager.getInstance();

        // Setup Swiss number formatters
        setupNumberFormatters();

        // Set Swiss locale for this panel
        SwissLocale.setupSwissLocale();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize with today's date
        datePicker.setDate(LocalDate.now());

        // Load locations and types
        refreshLocationCombo();
        refreshTypeCombo();

        // Set currency change listener
        currencyCombo.addActionListener(e -> updateCurrencyFields());

        // Set amount change listener for auto CHF calculation
        setupDocumentListeners();
    }

    private void setupNumberFormatters() {
        DecimalFormatSymbols symbols = SwissLocale.getSwissNumberSymbols();

        // Amount formatter (general numbers)
        amountFormatter = new DecimalFormat("#,##0.00", symbols);
        amountFormatter.setParseBigDecimal(true);

        // CHF formatter (with CHF symbol)
        chfFormatter = new DecimalFormat("CHF #,##0.00", symbols);
        chfFormatter.setParseBigDecimal(true);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Zahlung erfassen"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Payment Date with Date Picker
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Datum*:"), gbc);
        datePicker = new SimpleDatePicker();
        gbc.gridx = 1;
        formPanel.add(datePicker, gbc);

        // Description
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Beschreibung*:"), gbc);
        descriptionField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);

        // Amount and Currency
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Betrag*:"), gbc);

        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        amountField = new JFormattedTextField(amountFormatter);
        amountField.setColumns(10);
        amountField.setValue(BigDecimal.ZERO);
        amountPanel.add(amountField);

        currencyCombo = new JComboBox<>(new String[] { "CHF", "EUR", "USD", "GBP", "JPY" });
        currencyCombo.setPreferredSize(new Dimension(80, 25));
        amountPanel.add(currencyCombo);

        gbc.gridx = 1;
        formPanel.add(amountPanel, gbc);

        // Amount CHF
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Betrag CHF*:"), gbc);
        amountCHFField = new JFormattedTextField(chfFormatter);
        amountCHFField.setColumns(10);
        amountCHFField.setValue(BigDecimal.ZERO);
        amountCHFField.setEditable(false);
        amountCHFField.setForeground(new Color(0, 100, 0)); // Green for CHF
        gbc.gridx = 1;
        formPanel.add(amountCHFField, gbc);

        // Exchange Rate
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Wechselkurs:"), gbc);
        exchangeRateField = new JFormattedTextField(amountFormatter);
        exchangeRateField.setColumns(10);
        exchangeRateField.setValue(new BigDecimal("1.00"));
        gbc.gridx = 1;
        formPanel.add(exchangeRateField, gbc);

        // Recurrence (in German)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Wiederholung:"), gbc);
        recurrenceCombo = new JComboBox<>(new String[] {
                "Einmalig", "Täglich", "Wöchentlich", "Alle 2 Wochen", "Monatlich",
                "Alle 3 Monate", "Jährlich", "Vor Feiertag", "Nach Feiertag"
        });
        gbc.gridx = 1;
        formPanel.add(recurrenceCombo, gbc);

        // Debit/Credit (in German)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Lastschrift/Gutschrift*:"), gbc);
        debitCreditCombo = new JComboBox<>(new String[] { "Lastschrift", "Gutschrift" });
        gbc.gridx = 1;
        formPanel.add(debitCreditCombo, gbc);

        // Location with Add button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Ort:"), gbc);

        JPanel locationPanel = new JPanel(new BorderLayout(5, 0));
        locationCombo = new JComboBox<>();
        locationPanel.add(locationCombo, BorderLayout.CENTER);

        JButton addLocationBtn = new JButton("+");
        addLocationBtn.setPreferredSize(new Dimension(30, 25));
        addLocationBtn.setToolTipText("Neuen Ort hinzufügen");
        addLocationBtn.addActionListener(e -> addNewLocation());
        locationPanel.add(addLocationBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(locationPanel, gbc);

        // Type with Add button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Kategorie:"), gbc);

        JPanel typePanel = new JPanel(new BorderLayout(5, 0));
        typeCombo = new JComboBox<>();
        typePanel.add(typeCombo, BorderLayout.CENTER);

        JButton addTypeBtn = new JButton("+");
        addTypeBtn.setPreferredSize(new Dimension(30, 25));
        addTypeBtn.setToolTipText("Neue Kategorie hinzufügen");
        addTypeBtn.addActionListener(e -> addNewType());
        typePanel.add(addTypeBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(typePanel, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton saveButton = new JButton("Zahlung speichern");
        saveButton.setPreferredSize(new Dimension(140, 30));
        saveButton.addActionListener(e -> savePayment());

        JButton clearButton = new JButton("Formular leeren");
        clearButton.setPreferredSize(new Dimension(140, 30));
        clearButton.addActionListener(e -> clearForm());

        JButton viewListButton = new JButton("Alle Zahlungen anzeigen");
        viewListButton.setPreferredSize(new Dimension(180, 30));
        viewListButton.addActionListener(e -> mainFrame.showListPanel());

        JButton timelineButton = new JButton("Zeitverlauf anzeigen");
        timelineButton.setPreferredSize(new Dimension(160, 30));
        timelineButton.addActionListener(e -> mainFrame.showTimelinePanel());

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(viewListButton);
        buttonPanel.add(timelineButton);

        return buttonPanel;
    }

    private void setupDocumentListeners() {
        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCHFAmount();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCHFAmount();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCHFAmount();
            }
        };

        amountField.getDocument().addDocumentListener(listener);
        exchangeRateField.getDocument().addDocumentListener(listener);
    }

    private void updateCurrencyFields() {
        String currency = (String) currencyCombo.getSelectedItem();
        if ("CHF".equals(currency)) {
            exchangeRateField.setValue(new BigDecimal("1.00"));
            exchangeRateField.setEnabled(false);
            updateCHFAmount();
        } else {
            exchangeRateField.setEnabled(true);
            // Set default exchange rates for common currencies
            switch (currency) {
                case "EUR":
                    exchangeRateField.setValue(new BigDecimal("0.95"));
                    break;
                case "USD":
                    exchangeRateField.setValue(new BigDecimal("1.10"));
                    break;
                case "GBP":
                    exchangeRateField.setValue(new BigDecimal("0.85"));
                    break;
                case "JPY":
                    exchangeRateField.setValue(new BigDecimal("160.00"));
                    break;
                default:
                    exchangeRateField.setValue(new BigDecimal("1.00"));
            }
            updateCHFAmount();
        }
    }

    private void updateCHFAmount() {
        try {
            BigDecimal amount = (BigDecimal) amountField.getValue();
            BigDecimal rate = (BigDecimal) exchangeRateField.getValue();

            if (amount == null || rate == null) {
                amountCHFField.setValue(BigDecimal.ZERO);
                return;
            }

            BigDecimal chfAmount = amount.multiply(rate);
            chfAmount = chfAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
            amountCHFField.setValue(chfAmount);

        } catch (Exception e) {
            amountCHFField.setValue(BigDecimal.ZERO);
        }
    }

    private void refreshLocationCombo() {
        locationCombo.removeAllItems();
        locationCombo.addItem("-- Ort auswählen --");
        for (PaymentLocation location : locationManager.getAllLocations()) {
            locationCombo.addItem(location.getName());
        }
        if (locationCombo.getItemCount() > 1) {
            locationCombo.setSelectedIndex(1);
        }
    }

    private void refreshTypeCombo() {
        typeCombo.removeAllItems();
        typeCombo.addItem("-- Kategorie auswählen --");
        for (PaymentType type : typeManager.getAllTypes()) {
            typeCombo.addItem(type.getName());
        }
        if (typeCombo.getItemCount() > 1) {
            typeCombo.setSelectedIndex(1);
        }
    }

    private void addNewLocation() {
        LocationDialog dialog = new LocationDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshLocationCombo();
            // Select the newly added location
            for (int i = 0; i < locationCombo.getItemCount(); i++) {
                if (locationCombo.getItemAt(i).equals(dialog.getPaymentLocation().getName())) {
                    locationCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void addNewType() {
        TypeDialog dialog = new TypeDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTypeCombo();
            // Select the newly added type
            for (int i = 0; i < typeCombo.getItemCount(); i++) {
                if (typeCombo.getItemAt(i).equals(dialog.getPaymentType().getName())) {
                    typeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void savePayment() {
        try {
            // Validation
            if (datePicker.getDate() == null) {
                JOptionPane.showMessageDialog(this,
                        "Bitte geben Sie ein Datum ein.",
                        "Validierungsfehler",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Bitte geben Sie eine Beschreibung ein.",
                        "Validierungsfehler",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal amount = (BigDecimal) amountField.getValue();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                JOptionPane.showMessageDialog(this,
                        "Bitte geben Sie einen Betrag ein.",
                        "Validierungsfehler",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get data
            LocalDate paymentDate = datePicker.getDate();
            BigDecimal amountCHF = (BigDecimal) amountCHFField.getValue();
            BigDecimal exchangeRate = (BigDecimal) exchangeRateField.getValue();

            // Convert German recurrence to English for storage
            String recurrenceEN = convertRecurrenceToEN(
                    (String) recurrenceCombo.getSelectedItem());

            // Convert German debit/credit to English for storage
            String debitCreditEN = convertDebitCreditToEN(
                    (String) debitCreditCombo.getSelectedItem());

            // Get location ID
            int locationId = 0;
            if (locationCombo.getSelectedIndex() > 0) {
                String locationName = (String) locationCombo.getSelectedItem();
                PaymentLocation location = locationManager.getLocationByName(locationName);
                if (location != null) {
                    locationId = location.getId();
                }
            }

            // Get type ID
            int typeId = 0;
            if (typeCombo.getSelectedIndex() > 0) {
                String typeName = (String) typeCombo.getSelectedItem();
                PaymentType type = typeManager.getTypeByName(typeName);
                if (type != null) {
                    typeId = type.getId();
                }
            }

            // Create and save payment
            Payment payment = new Payment(
                    paymentDate,
                    descriptionField.getText(),
                    amount,
                    (String) currencyCombo.getSelectedItem(),
                    amountCHF,
                    exchangeRate,
                    recurrenceEN,
                    debitCreditEN,
                    locationId,
                    typeId);

            int paymentId = paymentManager.addPayment(payment);

            JOptionPane.showMessageDialog(this,
                    String.format("Zahlung erfolgreich gespeichert! (ID: %d)", paymentId),
                    "Erfolg",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Fehler beim Speichern der Zahlung: " + e.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String convertRecurrenceToEN(String recurrenceDE) {
        switch (recurrenceDE) {
            case "Einmalig":
                return "One-time";
            case "Täglich":
                return "Daily";
            case "Wöchentlich":
                return "Weekly";
            case "Alle 2 Wochen":
                return "Every 2 weeks";
            case "Monatlich":
                return "Monthly";
            case "Alle 3 Monate":
                return "Every 3 months";
            case "Jährlich":
                return "Yearly";
            case "Vor Feiertag":
                return "Before holiday";
            case "Nach Feiertag":
                return "After holiday";
            default:
                return "One-time";
        }
    }

    private String convertDebitCreditToEN(String debitCreditDE) {
        switch (debitCreditDE) {
            case "Lastschrift":
                return "Debit";
            case "Gutschrift":
                return "Credit";
            default:
                return "Debit";
        }
    }

    private void clearForm() {
        datePicker.setDate(LocalDate.now());
        descriptionField.setText("");
        amountField.setValue(BigDecimal.ZERO);
        currencyCombo.setSelectedItem("CHF");
        amountCHFField.setValue(BigDecimal.ZERO);
        exchangeRateField.setValue(new BigDecimal("1.00"));
        recurrenceCombo.setSelectedItem("Einmalig");
        debitCreditCombo.setSelectedItem("Lastschrift");

        if (locationCombo.getItemCount() > 0) {
            locationCombo.setSelectedIndex(locationCombo.getItemCount() > 1 ? 1 : 0);
        }

        if (typeCombo.getItemCount() > 0) {
            typeCombo.setSelectedIndex(typeCombo.getItemCount() > 1 ? 1 : 0);
        }

        descriptionField.requestFocus();
    }
}