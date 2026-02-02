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

import data.TypeManager;
import models.PaymentType;

public class TypeDialog extends JDialog {
    private boolean saved = false;
    private PaymentType paymentType;

    private JTextField nameField;
    private JTextArea descriptionArea;

    public TypeDialog(JFrame parent) {
        super(parent, "Add New Payment Type", true);
        setSize(400, 250);
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
        descriptionArea = new JTextArea(4, 20);
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
        saveButton.addActionListener(e -> saveType());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, gbc);

        add(panel);

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    private void saveType() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a type name.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // Check if type with this name already exists
        TypeManager typeManager = TypeManager.getInstance();
        PaymentType existing = typeManager.getTypeByName(nameField.getText().trim());
        if (existing != null) {
            JOptionPane.showMessageDialog(this,
                    "A type with this name already exists.",
                    "Duplicate Name",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        paymentType = new PaymentType(
                nameField.getText().trim(),
                descriptionArea.getText().trim());

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }
}