package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.AppointmentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    // --- Relationship with Patient ---
    @ManyToOne(fetch = FetchType.LAZY) // LAZY is generally preferred for performance
    @JoinColumn(name = "patient_id_fk", nullable = false) // Name of the FK column in 'appointments' table
    // referencing Patient's PK.
    // nullable = false means an appointment MUST have a patient.
    private Patient patient; // Reference to the Patient entity

    // --- Relationship with Doctor ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id_fk", nullable = false) // Name of the FK column in 'appointments' table
    // referencing Doctor's PK.
    // nullable = false means an appointment MUST have a doctor.
    private Staff doctor;   // Reference to the Doctor entity
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    private String reason;
    private AppointmentStatus status;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Bill bill; // Assuming a Bill has a OneToOne back to Appointment or AppointmentId

    public void setDoctor(Staff doctor) {
        this.doctor = doctor;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    private String diagnosisNotes;

    public Appointment(Patient patientId, Staff doctorId, LocalDateTime dateTime, String reason) {
        this.patient = patientId;
        this.doctor = doctorId;
        this.dateTime = dateTime;
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Appointment() {

    }

    // Getters and setters... (same as before)

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public long getAppointmentId() { return appointmentId; }
    public Patient getPatient() { return patient; }
    public Staff getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }


    // Helper method for the rendered condition
    public boolean isBillFullyPaid() {
        return this.bill != null && this.bill.getPaymentStatus() == org.pahappa.systems.hms.constants.PaymentStatus.PAID;
    }
    public boolean hasBill() {
        return this.bill != null;
    }

}