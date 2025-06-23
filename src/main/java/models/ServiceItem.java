package models;

import jakarta.persistence.*;

@Embeddable
public class ServiceItem { // This would be like a row in BillItems table or a product/service catalog
    private String name;
    private double cost;
    public ServiceItem(String name, double cost) { this.name = name; this.cost = cost; }

    public ServiceItem() {

    }
    public String getName() { return name; }
    public double getCost() { return cost; }
}
