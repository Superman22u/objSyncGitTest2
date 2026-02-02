import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import gui.MainFrame;
import utils.SwissLocale;

public class Main {
    public static void main(String[] args) {
        // Set Swiss German locale for entire application
        SwissLocale.setupSwissLocale();

        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Schedule a job for the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);

                // Show welcome message in German
                JOptionPane.showMessageDialog(frame,
                        "Willkommen bei Payment Tracker!\n\n" +
                                "Ihre Daten werden in CSV-Dateien im 'data' Ordner gespeichert.\n" +
                                "Keine Datenbankinstallation erforderlich!\n\n" +
                                "Features:\n" +
                                "- Zahlungserfassung mit Datumsauswahl\n" +
                                "- Schweizer Zahlenformat (CHF)\n" +
                                "- Zeitverlauf mit Saldoberechnung\n" +
                                "- Excel Export",
                        "Willkommen",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Fehler beim Starten der Anwendung: " + e.getMessage(),
                        "Schwerwiegender Fehler",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}