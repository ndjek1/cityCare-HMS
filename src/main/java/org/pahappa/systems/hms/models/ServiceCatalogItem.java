package org.pahappa.systems.hms.models;

import org.pahappa.systems.hms.constants.ServiceCategory;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "services") // Name of the database table
public class ServiceCatalogItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(nullable = false, unique = true, name = "service_code")
    private String serviceCode; // Optional: A unique code for the service (e.g., "CONSULT01", "XRAYCHEST")

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,name = "default_cost")
    private double defaultCost;

    private String description; // Optional description

    @Enumerated(EnumType.STRING)
    private ServiceCategory category; // Optional: To categorize org.pahappa.systems.hms.navigation.services (e.g., Consultation, Lab Test, Pharmacy)

    private boolean active = true; // To enable/disable org.pahappa.systems.hms.navigation.services from being selected

    // Constructors
    public ServiceCatalogItem() {
    }

    public ServiceCatalogItem(String serviceCode, String name, double defaultCost, ServiceCategory category) {
        this.serviceCode = serviceCode;
        this.name = name;
        this.defaultCost = defaultCost;
        this.category = category;
    }

    // Getters and Setters
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getDefaultCost() { return defaultCost; }
    public void setDefaultCost(double defaultCost) { this.defaultCost = defaultCost; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // equals and hashCode are important if you use these in Sets or as keys in Maps
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceCatalogItem that = (ServiceCatalogItem) o;
        return Objects.equals(serviceId, that.serviceId) && Objects.equals(serviceCode, that.serviceCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, serviceCode);
    }

    @Override
    public String toString() {
        return name + " (UGX " + defaultCost + ")"; // For simple display in dropdowns
    }
}
