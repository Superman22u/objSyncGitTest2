package gui;

import java.awt.BorderLayout;
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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import data.LocationManager;
import data.PaymentManager;
import data.TypeManager;
import models.Payment;
import models.PaymentLocation;
import models.PaymentType;
import utils.SwissLocale;

public class PaymentEntryPanel2 extends JPanel {
    private MainFrame mainFrame;
    private PaymentManager paymentManager;
    private LocationManager locationManager;
    private TypeManager typeManager;

    // Form components
    private JTextField dateField;
    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<String> currencyCombo;
    private JTextField amountCHFField;
    private JTextField exchangeRateField;
    private JComboBox<String> recurrenceCombo;
    private JComboBox<String> debitCreditCombo;
    private JComboBox<String> locationCombo;
    private JComboBox<String> typeCombo;

    private DecimalFormat amountFormatter;
    private DecimalFormat chfFormatter;

    // Buttons
    private JButton saveButton;
    private JButton clearButton;
    private JButton viewListButton;
    private JButton addLocationButton;
    private JButton addTypeButton;

    public PaymentEntryPanel2(MainFrame mainFrame) {
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
        dateField.setText(LocalDate.now().toString());

        // Load locations and types
        refreshLocationCombo();
        refreshTypeCombo();

        // Set currency change listener
        currencyCombo.addActionListener(e -> updateCurrencyFields());

        // Set amount change listener for auto CHF calculation
        javax.swing.event.DocumentListener documentListener = new javax.swing.event.DocumentListener() {
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

        amountField.getDocument().addDocumentListener(documentListener);
        exchangeRateField.getDocument().addDocumentListener(documentListener);
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Enter Payment Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Payment Date
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Payment Date*:"), gbc);
        dateField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);

        // Description
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Description*:"), gbc);
        descriptionField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);

        // Amount and Currency
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Amount*:"), gbc);

        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        amountField = new JTextField(10);
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
        formPanel.add(new JLabel("Amount CHF*:"), gbc);
        amountCHFField = new JTextField(10);
        amountCHFField.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(amountCHFField, gbc);

        // Exchange Rate
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Exchange Rate:"), gbc);
        exchangeRateField = new JTextField(10);
        exchangeRateField.setText("1.0000");
        gbc.gridx = 1;
        formPanel.add(exchangeRateField, gbc);

        // Recurrence
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Recurrence:"), gbc);
        recurrenceCombo = new JComboBox<>(new String[] {
                "One-time", "Daily", "Weekly", "Every 2 weeks", "Monthly",
                "Every 3 months", "Yearly", "Before holiday", "After holiday"
        });
        gbc.gridx = 1;
        formPanel.add(recurrenceCombo, gbc);

        // Debit/Credit
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Debit/Credit*:"), gbc);
        debitCreditCombo = new JComboBox<>(new String[] { "Debit", "Credit" });
        gbc.gridx = 1;
        formPanel.add(debitCreditCombo, gbc);

        // Location with Add button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Location:"), gbc);

        JPanel locationPanel = new JPanel(new BorderLayout(5, 0));
        locationCombo = new JComboBox<>();
        locationPanel.add(locationCombo, BorderLayout.CENTER);

        addLocationButton = new JButton("+");
        addLocationButton.setPreferredSize(new Dimension(30, 25));
        addLocationButton.setToolTipText("Add new location");
        addLocationButton.addActionListener(e -> addNewLocation());
        locationPanel.add(addLocationButton, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(locationPanel, gbc);

        // Type with Add button
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Type:"), gbc);

        JPanel typePanel = new JPanel(new BorderLayout(5, 0));
        typeCombo = new JComboBox<>();
        typePanel.add(typeCombo, BorderLayout.CENTER);

        addTypeButton = new JButton("+");
        addTypeButton.setPreferredSize(new Dimension(30, 25));
        addTypeButton.setToolTipText("Add new type");
        addTypeButton.addActionListener(e -> addNewType());
        typePanel.add(addTypeButton, BorderLayout.EAST);

        gbc.gridx = 1;
        formPanel.add(typePanel, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        saveButton = new JButton("Save Payment");
        saveButton.setPreferredSize(new Dimension(120, 30));
        saveButton.addActionListener(e -> savePayment());

        clearButton = new JButton("Clear Form");
        clearButton.setPreferredSize(new Dimension(120, 30));
        clearButton.addActionListener(e -> clearForm());

        viewListButton = new JButton("View All Payments");
        viewListButton.setPreferredSize(new Dimension(150, 30));
        viewListButton.addActionListener(e -> mainFrame.showListPanel());

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(viewListButton);

        return buttonPanel;
    }

    private void updateCurrencyFields() {
        String currency = (String) currencyCombo.getSelectedItem();
        if ("CHF".equals(currency)) {
            exchangeRateField.setText("1.0000");
            exchangeRateField.setEditable(false);
            updateCHFAmount();
        } else {
            exchangeRateField.setEditable(true);
            // Set default exchange rates for common currencies
            switch (currency) {
                case "EUR":
                    exchangeRateField.setText("0.9500");
                    break;
                case "USD":
                    exchangeRateField.setText("1.1000");
                    break;
                case "GBP":
                    exchangeRateField.setText("0.8500");
                    break;
                default:
                    exchangeRateField.setText("1.0000");
            }
            updateCHFAmount();
        }
    }

    private void updateCHFAmount() {
        try {
            if (amountField.getText().isEmpty()) {
                amountCHFField.setText("");
                return;
            }

            BigDecimal amount = new BigDecimal(amountField.getText());
            BigDecimal rate = new BigDecimal(exchangeRateField.getText());
            BigDecimal chfAmount = amount.multiply(rate);

            // Round to 2 decimal places
            chfAmount = chfAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
            amountCHFField.setText(chfAmount.toString());

        } catch (Exception e) {
            amountCHFField.setText("");
        }
    }

    private void refreshLocationCombo() {
        locationCombo.removeAllItems();
        locationCombo.addItem("-- Select Location --");
        for (PaymentLocation location : locationManager.getAllLocations()) {
            locationCombo.addItem(location.getName());
        }
        if (locationCombo.getItemCount() > 1) {
            locationCombo.setSelectedIndex(1);
        }
    }

    private void refreshTypeCombo() {
        typeCombo.removeAllItems();
        typeCombo.addItem("-- Select Type --");
        for (PaymentType type : typeManager.getAllTypes()) {
            typeCombo.addItem(type.getName());
        }
        if (typeCombo.getItemCount() > 1) {
            typeCombo.setSelectedIndex(1);
        }
    }

    private void addNewLocation() {
        // Create a simple dialog for adding locations
        JDialog locationDialog = createLocationDialog();
        locationDialog.setVisible(true);
    }

    private JDialog createLocationDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Location", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name*:"), gbc);
        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);
        JTextField descriptionField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(descriptionField, gbc);

        // Address field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Address:"), gbc);
        JTextArea addressArea = new JTextArea(4, 20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(addressArea), gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a location name.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            // Check if location with this name already exists
            PaymentLocation existing = locationManager.getLocationByName(nameField.getText().trim());
            if (existing != null) {
                JOptionPane.showMessageDialog(dialog,
                        "A location with this name already exists.",
                        "Duplicate Name",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create and save new location
            PaymentLocation location = new PaymentLocation(
                    nameField.getText().trim(),
                    descriptionField.getText().trim(),
                    addressArea.getText().trim());

            int newLocationId = locationManager.addLocation(location);

            if (newLocationId > 0) {
                // Refresh the combo box
                refreshLocationCombo();

                // Select the newly added location
                for (int i = 0; i < locationCombo.getItemCount(); i++) {
                    if (locationCombo.getItemAt(i).equals(location.getName())) {
                        locationCombo.setSelectedIndex(i);
                        break;
                    }
                }

                JOptionPane.showMessageDialog(dialog,
                        "Location added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.getRootPane().setDefaultButton(saveButton);

        return dialog;
    }

    private void addNewType() {
        // Create a simple dialog for adding types
        JDialog typeDialog = createTypeDialog();
        typeDialog.setVisible(true);
    }

    private JDialog createTypeDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Payment Type", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name*:"), gbc);
        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);
        JTextArea descriptionArea = new JTextArea(4, 20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(descriptionArea), gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a type name.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }

            // Check if type with this name already exists
            PaymentType existing = typeManager.getTypeByName(nameField.getText().trim());
            if (existing != null) {
                JOptionPane.showMessageDialog(dialog,
                        "A type with this name already exists.",
                        "Duplicate Name",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create and save new type
            PaymentType type = new PaymentType(
                    nameField.getText().trim(),
                    descriptionArea.getText().trim());

            int newTypeId = typeManager.addType(type);

            if (newTypeId > 0) {
                // Refresh the combo box
                refreshTypeCombo();

                // Select the newly added type
                for (int i = 0; i < typeCombo.getItemCount(); i++) {
                    if (typeCombo.getItemAt(i).equals(type.getName())) {
                        typeCombo.setSelectedIndex(i);
                        break;
                    }
                }

                JOptionPane.showMessageDialog(dialog,
                        "Payment type added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.getRootPane().setDefaultButton(saveButton);

        return dialog;
    }

    private void savePayment() {
        try {
            // Validation
            if (dateField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter payment date.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (descriptionField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter description.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter amount.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse data
            LocalDate paymentDate;
            try {
                paymentDate = LocalDate.parse(dateField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal amount;
            BigDecimal amountCHF;
            BigDecimal exchangeRate = null;

            try {
                amount = new BigDecimal(amountField.getText());
                amountCHF = new BigDecimal(amountCHFField.getText());
                if (!exchangeRateField.getText().isEmpty()) {
                    exchangeRate = new BigDecimal(exchangeRateField.getText());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

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
                    (String) recurrenceCombo.getSelectedItem(),
                    (String) debitCreditCombo.getSelectedItem(),
                    locationId,
                    typeId);

            int paymentId = paymentManager.addPayment(payment);

            JOptionPane.showMessageDialog(this,
                    String.format("Payment saved successfully! (ID: %d)", paymentId),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving payment: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearForm() {
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
        amountField.setText("");
        currencyCombo.setSelectedItem("CHF");
        amountCHFField.setText("");
        exchangeRateField.setText("1.0000");
        recurrenceCombo.setSelectedItem("One-time");
        debitCreditCombo.setSelectedItem("Debit");

        if (locationCombo.getItemCount() > 0) {
            locationCombo.setSelectedIndex(locationCombo.getItemCount() > 1 ? 1 : 0);
        }

        if (typeCombo.getItemCount() > 0) {
            typeCombo.setSelectedIndex(typeCombo.getItemCount() > 1 ? 1 : 0);
        }

        descriptionField.requestFocus();
    }
}