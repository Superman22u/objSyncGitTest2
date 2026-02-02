package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private PaymentEntryPanel entryPanel;
    private PaymentListPanel listPanel;
    private TimelinePanel timelinePanel; // Add this

    public MainFrame() {
        setTitle("Payment Tracker - Flat File Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800); // Increased size for timeline view
        setLocationRelativeTo(null);

        // Set application icon (optional)
        try {
            ImageIcon icon = new ImageIcon("icon.png");
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create panels
        entryPanel = new PaymentEntryPanel(this);
        listPanel = new PaymentListPanel(this);
        timelinePanel = new TimelinePanel(this); // Create timeline panel

        mainPanel.add(entryPanel, "entry");
        mainPanel.add(listPanel, "list");
        mainPanel.add(timelinePanel, "timeline"); // Add timeline panel

        // Create menu bar
        createMenuBar();

        add(mainPanel, BorderLayout.CENTER);

        // Show entry panel by default
        cardLayout.show(mainPanel, "entry");
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem backupMenuItem = new JMenuItem("Create Backup");
        backupMenuItem.addActionListener(e -> createBackup());

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));

        fileMenu.add(backupMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // Navigation menu
        JMenu navigationMenu = new JMenu("Navigation");

        JMenuItem entryMenuItem = new JMenuItem("Payment Entry");
        entryMenuItem.addActionListener(e -> showEntryPanel());

        JMenuItem listMenuItem = new JMenuItem("Payment List");
        listMenuItem.addActionListener(e -> {
            listPanel.refreshData();
            showListPanel();
        });

        JMenuItem timelineMenuItem = new JMenuItem("Timeline View");
        timelineMenuItem.addActionListener(e -> {
            timelinePanel.refreshData();
            showTimelinePanel();
        });

        navigationMenu.add(entryMenuItem);
        navigationMenu.add(listMenuItem);
        navigationMenu.add(timelineMenuItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(navigationMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createBackup() {
        try {
            data.FileStorage.createBackup();
            JOptionPane.showMessageDialog(this,
                    "Backup created successfully in data/backup folder!",
                    "Backup Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating backup: " + e.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Payment Tracker v1.0\n" +
                        "Flat File Edition\n" +
                        "Data stored in CSV files\n" +
                        "No database required!\n\n" +
                        "Features:\n" +
                        "- Payment Entry\n" +
                        "- Payment List with Filtering\n" +
                        "- Timeline View with Balance Tracking\n" +
                        "- Excel Export",
                "About Payment Tracker",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showListPanel() {
        listPanel.refreshData();
        cardLayout.show(mainPanel, "list");
    }

    public void showEntryPanel() {
        cardLayout.show(mainPanel, "entry");
    }

    public void showTimelinePanel() {
        cardLayout.show(mainPanel, "timeline");
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}