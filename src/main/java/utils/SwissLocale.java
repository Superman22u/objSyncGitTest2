package utils;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Currency;
import javax.swing.*;
import java.awt.*;

public class SwissLocale {
    // Swiss German locale
    public static final Locale SWISS_LOCALE = new Locale("de", "CH");
    
    // Date formatters
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(SWISS_LOCALE);
    
    public static final DateTimeFormatter DATE_FORMATTER_SHORT = 
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(SWISS_LOCALE);
    
    public static final DateTimeFormatter DATE_FORMATTER_LONG = 
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(SWISS_LOCALE);
    
    // Number formatters for CHF
    private static NumberFormat chfNumberFormat;
    private static NumberFormat chfCurrencyFormat;
    
    static {
        // Initialize CHF number format
        chfNumberFormat = NumberFormat.getNumberInstance(SWISS_LOCALE);
        chfNumberFormat.setMinimumFractionDigits(2);
        chfNumberFormat.setMaximumFractionDigits(2);
        
        // Initialize CHF currency format
        chfCurrencyFormat = NumberFormat.getCurrencyInstance(SWISS_LOCALE);
        chfCurrencyFormat.setCurrency(Currency.getInstance("CHF"));
    }
    
    // Swiss number format symbols
    public static DecimalFormatSymbols getSwissNumberSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(SWISS_LOCALE);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator('\'');
        symbols.setCurrencySymbol("CHF");
        return symbols;
    }
    
    // Format number as CHF
    public static String formatCHF(Number amount) {
        if (amount == null) return "CHF 0.00";
        return chfCurrencyFormat.format(amount);
    }
    
    // Format number with Swiss separators
    public static String formatNumber(Number number) {
        if (number == null) return "0.00";
        return chfNumberFormat.format(number);
    }
    
    // Parse Swiss formatted number
    public static Double parseSwissNumber(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        
        // Remove Swiss grouping apostrophes and replace commas with dots
        String cleaned = text.trim()
            .replace("'", "")  // Remove thousands separator
            .replace(",", "."); // Decimal separator
        
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    // Set Swiss locale for all Swing components
    public static void setupSwissLocale() {
        Locale.setDefault(SWISS_LOCALE);
        UIManager.getDefaults().setDefaultLocale(SWISS_LOCALE);
        JComponent.setDefaultLocale(SWISS_LOCALE);
    }
    
    // Create a formatted text field for Swiss numbers
    public static JTextField createNumberField() {
        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.RIGHT);
        return field;
    }
    
    // Create a formatted text field for CHF amounts
    public static JTextField createCHFField() {
        JTextField field = new JTextField();
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setForeground(new Color(0, 100, 0)); // Green for CHF
        return field;
    }
    
    // Swiss date patterns
    public static String getDatePattern() {
        return "dd.MM.yyyy";
    }
    
    public static String getDateTimePattern() {
        return "dd.MM.yyyy HH:mm";
    }
    
    // Validate Swiss date format
    public static boolean isValidSwissDate(String dateStr) {
        try {
            java.time.LocalDate.parse(dateStr, DATE_FORMATTER_SHORT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}