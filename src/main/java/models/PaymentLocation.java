package models;

public class PaymentLocation {
    private int id;
    private String name;
    private String description;
    private String address;

    // Constructors
    public PaymentLocation() {
    }

    public PaymentLocation(String name, String description, String address) {
        this.name = name;
        this.description = description;
        this.address = address;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return name;
    }
}