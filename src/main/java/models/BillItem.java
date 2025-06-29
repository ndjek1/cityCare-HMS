package models;

import jakarta.persistence.Embeddable;

@Embeddable // This item is part of a Bill
public class BillItem {

    // @Column(name="service_name") // Optional: if DB column name is different
    private String serviceName; // Name of the service at the time of billing

    // @Column(name="service_cost") // Optional
    private double costAtTimeOfBilling; // Cost at the time of billing (prices can change)

    private int quantity; // Optional: if services can have quantities (e.g., 2x Paracetamol)

    // Optional: Reference to the original catalog item, if you want to trace it back
    // This would make BillItem an @Entity with a @ManyToOne to ServiceCatalogItem
    // private Long serviceCatalogId; // Store ID, not the full object if embeddable

    public BillItem() {
    }

    public BillItem(String serviceName, double costAtTimeOfBilling, int quantity) {
        this.serviceName = serviceName;
        this.costAtTimeOfBilling = costAtTimeOfBilling;
        this.quantity = (quantity <= 0) ? 1 : quantity; // Default quantity to 1
    }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getCostAtTimeOfBilling() { return costAtTimeOfBilling; }
    public void setCostAtTimeOfBilling(double costAtTimeOfBilling) { this.costAtTimeOfBilling = costAtTimeOfBilling; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity > 0 ? quantity : 1; }

    public double getTotalCost() {
        return this.costAtTimeOfBilling * this.quantity;
    }
}