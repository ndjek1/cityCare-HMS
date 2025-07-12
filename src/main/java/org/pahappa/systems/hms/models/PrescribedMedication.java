package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.PrescriptionStatus;

import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
public class PrescribedMedication implements Serializable {

    @Column(nullable = false, name = "medication_id")
    private Long medicationId; // Reference to the Medication entity

    @Column(nullable = false, name = "medication_name")
    private String medicationName; // Denormalized for easy display

    @Column(nullable = false)
    private String dosage; // e.g., "500mg"

    @Column(nullable = false)
    private String frequency; // e.g., "Twice a day"

    private String duration; // e.g., "For 7 days"
    private int quantity; // e.g., 14 tablets

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.PENDING; // PENDING, DISPENSED, CANCELLED
    @Column(precision = 19, scale = 2, name = "cost_at_the_time_of_prescription")
    private BigDecimal costAtTimeOfPrescription; // Price when prescribed
    @Transient
    public BigDecimal getLineItemTotal() {
        if (this.costAtTimeOfPrescription == null) {
            return BigDecimal.ZERO;
        }
        return this.costAtTimeOfPrescription.multiply(new BigDecimal(this.quantity));
    }

    // Constructors, getters, setters...
    public PrescribedMedication() {}
    public PrescribedMedication(Long medicationId, String medicationName,
                                String dosage, String frequency, String duration, int quantity, BigDecimal costAtTimeOfPrescription) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
        this.costAtTimeOfPrescription = costAtTimeOfPrescription;

    }

    // ... all getters and setters ...
    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public BigDecimal getCostAtTimeOfPrescription() {
        return costAtTimeOfPrescription;
    }

    public void setCostAtTimeOfPrescription(BigDecimal costAtTimeOfPrescription) {
        this.costAtTimeOfPrescription = costAtTimeOfPrescription;
    }
}
