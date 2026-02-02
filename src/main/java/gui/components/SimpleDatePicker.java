package gui.components;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXDatePicker;

import utils.SwissLocale;

public class SimpleDatePicker extends JPanel {
    private JXDatePicker datePicker;

    public SimpleDatePicker() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Create date picker with Swiss locale
        datePicker = new JXDatePicker();
        datePicker.setFormats(SwissLocale.getDatePattern());
        datePicker.setLocale(SwissLocale.SWISS_LOCALE);

        // Set today's date
        datePicker.setDate(Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));

        add(datePicker, BorderLayout.CENTER);
    }

    public LocalDate getDate() {
        Date date = datePicker.getDate();
        if (date == null)
            return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            datePicker.setDate(null);
        } else {
            datePicker.setDate(Date.from(date
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }

    public void setDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            setDate((LocalDate) null);
        } else {
            try {
                LocalDate date = LocalDate.parse(dateString,
                        SwissLocale.DATE_FORMATTER_SHORT);
                setDate(date);
            } catch (Exception e) {
                setDate((LocalDate) null);
            }
        }
    }

    public void clear() {
        setDate((LocalDate) null);
    }

    public boolean hasDate() {
        return getDate() != null;
    }

    public JXDatePicker getDatePicker() {
        return datePicker;
    }
}