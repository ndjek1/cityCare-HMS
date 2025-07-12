package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "medications")
public class Medication implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicationId;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Paracetamol 500mg"

    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;
    private boolean active = true;

    private int stockLevel;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Getters and setters...
    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(long medicationId) { this.medicationId = medicationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
}
