package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.PrescriptionStatus;

import java.time.LocalDate;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id_fk", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id_fk", nullable = false)
    private Staff doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id_fk", nullable = true) // A prescription might not always be tied to an appointment (e.g., a refill)
    private Appointment appointment;

    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;

    @Column(length = 100)
    private String dosage; // e.g., "500mg", "10ml"

    @Column(length = 255)
    private String frequency; // e.g., "Once a day", "Twice daily with meals"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date") // Can be null for ongoing prescriptions
    private LocalDate endDate;

    @Lob // For potentially long text
    @Column(name = "instructions", columnDefinition="TEXT")
    private String instructions; // e.g., "Take with a full glass of water. Avoid dairy."

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status; // e.g., ACTIVE, COMPLETED, CANCELLED

    // No-arg constructor
    public Prescription() {
    }

    // Constructor with essential fields
    public Prescription(Patient patient, Staff doctor, Appointment appointment, String medicationName, String dosage, String frequency, LocalDate startDate) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointment = appointment;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.status = PrescriptionStatus.ACTIVE; // Default to active
    }

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Staff getDoctor() { return doctor; }
    public void setDoctor(Staff doctor) { this.doctor = doctor; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
}