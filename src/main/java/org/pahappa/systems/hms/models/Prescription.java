package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.PrescriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "prescriptions")
public class Prescription implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Staff doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    private LocalDate prescriptionDate;

    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch items with the prescription
    @CollectionTable(name = "prescribed_medications", joinColumns = @JoinColumn(name = "prescription_id"))
    @OrderBy("medicationName ASC")
    private List<PrescribedMedication> prescribedMedications = new ArrayList<>();

    // Constructors, getters, setters
    public Prescription() {
        this.prescriptionDate = LocalDate.now();
    }
    @Transient // This annotation tells Hibernate not to map this field to a DB column
    public BigDecimal getTotalCost() {
        if (prescribedMedications == null || prescribedMedications.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return prescribedMedications.stream()
                .map(PrescribedMedication::getLineItemTotal) // Get the BigDecimal cost of each medication
                .filter(Objects::nonNull) // Ensure we don't try to add a null value
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum them up
    }
    // ... other getters and setters ...
    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Staff getDoctor() { return doctor; }
    public void setDoctor(Staff doctor) { this.doctor = doctor; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }
    public List<PrescribedMedication> getPrescribedMedications() { return prescribedMedications; }
    public void setPrescribedMedications(List<PrescribedMedication> prescribedMedications) { this.prescribedMedications = prescribedMedications; }
}