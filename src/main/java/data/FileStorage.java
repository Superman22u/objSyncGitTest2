package data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.csv.*;

public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String PAYMENTS_FILE = DATA_DIR + "/payments.csv";
    private static final String LOCATIONS_FILE = DATA_DIR + "/locations.csv";
    private static final String TYPES_FILE = DATA_DIR + "/types.csv";
    private static final String CONFIG_FILE = DATA_DIR + "/config.properties";

    // CSV Headers
    private static final String[] PAYMENT_HEADERS = {
            "id", "paymentDate", "description", "amount", "currency",
            "amountCHF", "exchangeRate", "recurrence", "debitCredit",
            "locationId", "typeId", "createdAt"
    };

    private static final String[] LOCATION_HEADERS = {
            "id", "name", "description", "address"
    };

    private static final String[] TYPE_HEADERS = {
            "id", "name", "description"
    };

    static {
        // Create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            createDefaultFilesIfNotExist();
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    private static void createDefaultFilesIfNotExist() throws IOException {
        // Create payments file
        if (!Files.exists(Paths.get(PAYMENTS_FILE))) {
            writeCSV(PAYMENTS_FILE, PAYMENT_HEADERS, Collections.emptyList());
        }

        // Create locations file with default locations
        if (!Files.exists(Paths.get(LOCATIONS_FILE))) {
            List<String[]> defaultLocations = Arrays.asList(
                    new String[] { "1", "Home", "Primary residence", "Home address" },
                    new String[] { "2", "Migros", "Supermarket chain", "Various locations" },
                    new String[] { "3", "Coop", "Supermarket chain", "Various locations" },
                    new String[] { "4", "Online", "Online purchase", "Internet" });
            writeCSV(LOCATIONS_FILE, LOCATION_HEADERS, defaultLocations);
        }

        // Create types file with default types
        if (!Files.exists(Paths.get(TYPES_FILE))) {
            List<String[]> defaultTypes = Arrays.asList(
                    new String[] { "1", "Groceries", "Food and household items" },
                    new String[] { "2", "Beer", "Alcoholic beverages" },
                    new String[] { "3", "Cosmetics", "Beauty and personal care products" },
                    new String[] { "4", "Transport", "Public transport and fuel" },
                    new String[] { "5", "Entertainment", "Movies, concerts, etc." },
                    new String[] { "6", "Rent", "Monthly rent payment" },
                    new String[] { "7", "Utilities", "Electricity, water, internet" },
                    new String[] { "8", "Healthcare", "Medical expenses" });
            writeCSV(TYPES_FILE, TYPE_HEADERS, defaultTypes);
        }

        // Create config file if it doesn't exist
        if (!Files.exists(Paths.get(CONFIG_FILE))) {
            try (PrintWriter writer = new PrintWriter(CONFIG_FILE)) {
                writer.println("# Payment Tracker Configuration");
                writer.println("default.currency=CHF");
                writer.println("last.id.payment=0");
                writer.println("last.id.location=4");
                writer.println("last.id.type=8");
                writer.println("backup.auto=true");
                writer.println("backup.max=10");
            }
        }
    }

    // Payment methods using Apache Commons CSV
    public static void savePayments(List<String[]> payments) throws IOException {
        writeCSV(PAYMENTS_FILE, PAYMENT_HEADERS, payments);
        updateConfig("last.id.payment", String.valueOf(getNextPaymentId() - 1));
    }

    public static List<String[]> loadPayments() throws IOException {
        return readCSV(PAYMENTS_FILE);
    }

    // Location methods
    public static void saveLocations(List<String[]> locations) throws IOException {
        writeCSV(LOCATIONS_FILE, LOCATION_HEADERS, locations);
        updateConfig("last.id.location", String.valueOf(getNextLocationId() - 1));
    }

    public static List<String[]> loadLocations() throws IOException {
        return readCSV(LOCATIONS_FILE);
    }

    // Type methods
    public static void saveTypes(List<String[]> types) throws IOException {
        writeCSV(TYPES_FILE, TYPE_HEADERS, types);
        updateConfig("last.id.type", String.valueOf(getNextTypeId() - 1));
    }

    public static List<String[]> loadTypes() throws IOException {
        return readCSV(TYPES_FILE);
    }

    // Helper methods using Apache Commons CSV
    private static void writeCSV(String filename, String[] headers, List<String[]> data) throws IOException {
        try (FileWriter writer = new FileWriter(filename);
                CSVPrinter csvPrinter = new CSVPrinter(writer,
                        CSVFormat.DEFAULT.withHeader(headers))) {

            for (String[] row : data) {
                csvPrinter.printRecord((Object[]) row);
            }
        }
    }

    private static List<String[]> readCSV(String filename) throws IOException {
        List<String[]> data = new ArrayList<>();

        if (!Files.exists(Paths.get(filename))) {
            return data;
        }

        try (FileReader reader = new FileReader(filename);
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim()
                        .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data.add(row);
            }
        }

        return data;
    }

    // ID management
    public static int getNextPaymentId() {
        return getNextId("last.id.payment");
    }

    public static int getNextLocationId() {
        return getNextId("last.id.location");
    }

    public static int getNextTypeId() {
        return getNextId("last.id.type");
    }

    private static int getNextId(String configKey) {
        try {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                props.load(reader);
            }
            String value = props.getProperty(configKey, "0");
            return Integer.parseInt(value) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private static void updateConfig(String key, String value) {
        try {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                props.load(reader);
            }
            props.setProperty(key, value);
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                props.store(writer, "Payment Tracker Configuration");
            }
        } catch (IOException e) {
            System.err.println("Error updating config: " + e.getMessage());
        }
    }

    // Backup method with auto-backup
    public static void createBackup() throws IOException {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupDir = Paths.get(DATA_DIR, "backup_" + timestamp);
        Files.createDirectories(backupDir);

        // Copy all CSV files
        Files.copy(Paths.get(PAYMENTS_FILE), backupDir.resolve("payments.csv"));
        Files.copy(Paths.get(LOCATIONS_FILE), backupDir.resolve("locations.csv"));
        Files.copy(Paths.get(TYPES_FILE), backupDir.resolve("types.csv"));
        Files.copy(Paths.get(CONFIG_FILE), backupDir.resolve("config.properties"));

        // Clean up old backups if auto-backup is enabled
        cleanOldBackups();

        System.out.println("Backup created: " + backupDir);
    }

    private static void cleanOldBackups() {
        try {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                props.load(reader);
            }

            String autoBackup = props.getProperty("backup.auto", "true");
            if (!"true".equalsIgnoreCase(autoBackup)) {
                return;
            }

            int maxBackups = Integer.parseInt(props.getProperty("backup.max", "10"));

            File dataDir = new File(DATA_DIR);
            File[] backupDirs = dataDir
                    .listFiles((dir, name) -> name.startsWith("backup_") && new File(dir, name).isDirectory());

            if (backupDirs != null && backupDirs.length > maxBackups) {
                // Sort by name (which includes timestamp)
                Arrays.sort(backupDirs);

                // Delete oldest backups
                for (int i = 0; i < backupDirs.length - maxBackups; i++) {
                    deleteDirectory(backupDirs[i]);
                    System.out.println("Deleted old backup: " + backupDirs[i].getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning old backups: " + e.getMessage());
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    // Export to JSON (optional feature)
    public static void exportToJson(String filename) throws IOException {
        // This would use Jackson to export data to JSON
        // Implementation depends on whether you want this feature
    }

    // Import from JSON (optional feature)
    public static void importFromJson(String filename) throws IOException {
        // This would use Jackson to import data from JSON
    }
}