package data;

import java.util.ArrayList;
import java.util.List;

import models.Payment;
import models.PaymentLocation;

public class LocationManager {
    private static LocationManager instance;
    private List<PaymentLocation> locations;

    private LocationManager() {
        locations = new ArrayList<>();
        loadLocations();
    }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    private void loadLocations() {
        try {
            List<String[]> locationData = FileStorage.loadLocations();
            if (locationData.size() <= 1)
                return; // Only header or empty

            for (int i = 1; i < locationData.size(); i++) {
                String[] row = locationData.get(i);
                if (row.length >= 4) {
                    PaymentLocation location = new PaymentLocation();
                    location.setId(Integer.parseInt(row[0]));
                    location.setName(row[1]);
                    location.setDescription(row[2]);
                    location.setAddress(row[3]);
                    locations.add(location);
                }
            }

            // Sort by name
            locations.sort((l1, l2) -> l1.getName().compareToIgnoreCase(l2.getName()));

        } catch (Exception e) {
            System.err.println("Error loading location data: " + e.getMessage());
        }
    }

    private void saveLocations() {
        try {
            List<String[]> data = new ArrayList<>();
            // Add header
            data.add(new String[] { "id", "name", "description", "address" });

            // Add location data
            for (PaymentLocation location : locations) {
                data.add(new String[] {
                        String.valueOf(location.getId()),
                        location.getName(),
                        location.getDescription(),
                        location.getAddress()
                });
            }

            FileStorage.saveLocations(data);

        } catch (Exception e) {
            System.err.println("Error saving location data: " + e.getMessage());
        }
    }

    // CRUD Operations
    public int addLocation(PaymentLocation location) {
        location.setId(FileStorage.getNextLocationId());
        locations.add(location);
        saveLocations();
        PaymentManager.getInstance().refreshData(); // Refresh payment manager cache
        return location.getId();
    }

    public List<PaymentLocation> getAllLocations() {
        return new ArrayList<>(locations);
    }

    public PaymentLocation getLocationById(int id) {
        for (PaymentLocation location : locations) {
            if (location.getId() == id) {
                return location;
            }
        }
        return null;
    }

    public PaymentLocation getLocationByName(String name) {
        for (PaymentLocation location : locations) {
            if (location.getName().equalsIgnoreCase(name)) {
                return location;
            }
        }
        return null;
    }

    public boolean deleteLocation(int id) {
        // Check if any payment uses this location
        PaymentManager paymentManager = PaymentManager.getInstance();
        List<Payment> payments = paymentManager.getAllPayments();
        for (Payment payment : payments) {
            if (payment.getLocationId() == id) {
                return false; // Cannot delete, location is in use
            }
        }

        // Remove location
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getId() == id) {
                locations.remove(i);
                saveLocations();
                paymentManager.refreshData(); // Refresh payment manager cache
                return true;
            }
        }
        return false;
    }

    public void refreshData() {
        locations.clear();
        loadLocations();
    }
}