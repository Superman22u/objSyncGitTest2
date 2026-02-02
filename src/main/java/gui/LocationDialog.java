package gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import data.LocationManager;
import models.PaymentLocation;

public class LocationDialog extends JDialog {
    private boolean saved = false;
    private PaymentLocation paymentLocation;

    private JTextField nameField;
    private JTextField descriptionField;
    private JTextArea addressArea;

    public LocationDialog(JFrame parent) {
        super(parent, "Add New Location", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name*:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        // Description field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);
        descriptionField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(descriptionField, gbc);

        // Address field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Address:"), gbc);
        addressArea = new JTextArea(4, 20);
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
        saveButton.addActionListener(e -> saveLocation());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        add(panel);

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    private void saveLocation() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a location name.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // Check if location with this name already exists
        LocationManager locationManager = LocationManager.getInstance();
        PaymentLocation existing = locationManager.getLocationByName(nameField.getText().trim());
        if (existing != null) {
            JOptionPane.showMessageDialog(this,
                    "A location with this name already exists.",
                    "Duplicate Name",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        paymentLocation = new PaymentLocation(
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                addressArea.getText().trim());

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public PaymentLocation getPaymentLocation() {
        return this.paymentLocation;
    }
}