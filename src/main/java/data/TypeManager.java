package data;

import java.util.ArrayList;
import java.util.List;

import models.Payment;
import models.PaymentType;

public class TypeManager {
    private static TypeManager instance;
    private List<PaymentType> types;

    private TypeManager() {
        types = new ArrayList<>();
        loadTypes();
    }

    public static synchronized TypeManager getInstance() {
        if (instance == null) {
            instance = new TypeManager();
        }
        return instance;
    }

    private void loadTypes() {
        try {
            List<String[]> typeData = FileStorage.loadTypes();
            if (typeData.size() <= 1)
                return; // Only header or empty

            for (int i = 1; i < typeData.size(); i++) {
                String[] row = typeData.get(i);
                if (row.length >= 3) {
                    PaymentType type = new PaymentType();
                    type.setId(Integer.parseInt(row[0]));
                    type.setName(row[1]);
                    type.setDescription(row[2]);
                    types.add(type);
                }
            }

            // Sort by name
            types.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

        } catch (Exception e) {
            System.err.println("Error loading type data: " + e.getMessage());
        }
    }

    private void saveTypes() {
        try {
            List<String[]> data = new ArrayList<>();
            // Add header
            data.add(new String[] { "id", "name", "description" });

            // Add type data
            for (PaymentType type : types) {
                data.add(new String[] {
                        String.valueOf(type.getId()),
                        type.getName(),
                        type.getDescription()
                });
            }

            FileStorage.saveTypes(data);

        } catch (Exception e) {
            System.err.println("Error saving type data: " + e.getMessage());
        }
    }

    // CRUD Operations
    public int addType(PaymentType type) {
        type.setId(FileStorage.getNextTypeId());
        types.add(type);
        saveTypes();
        PaymentManager.getInstance().refreshData(); // Refresh payment manager cache
        return type.getId();
    }

    public List<PaymentType> getAllTypes() {
        return new ArrayList<>(types);
    }

    public PaymentType getTypeById(int id) {
        for (PaymentType type : types) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }

    public PaymentType getTypeByName(String name) {
        for (PaymentType type : types) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public boolean deleteType(int id) {
        // Check if any payment uses this type
        PaymentManager paymentManager = PaymentManager.getInstance();
        List<Payment> payments = paymentManager.getAllPayments();
        for (Payment payment : payments) {
            if (payment.getTypeId() == id) {
                return false; // Cannot delete, type is in use
            }
        }

        // Remove type
        for (int i = 0; i < types.size(); i++) {
            if (types.get(i).getId() == id) {
                types.remove(i);
                saveTypes();
                paymentManager.refreshData(); // Refresh payment manager cache
                return true;
            }
        }
        return false;
    }

    public void refreshData() {
        types.clear();
        loadTypes();
    }
}