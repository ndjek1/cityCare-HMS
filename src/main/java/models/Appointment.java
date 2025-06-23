package models;

import jakarta.persistence.*;
import org.example.constants.AppointmentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
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
    private LocalDateTime dateTime;
    private String reason;
    private AppointmentStatus status;
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
    public Long getAppointmentId() { return appointmentId; }
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

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String lineSeparator = System.lineSeparator();
        int labelWidth = 18; // Width for labels like "Appointment ID:", "Patient:"
        String indent = "  "; // Indentation for details

        StringBuilder sb = new StringBuilder();

        sb.append("------------------------------------------").append(lineSeparator);

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Appointment ID", appointmentId != null ? appointmentId : "N/A"));

        if (patient != null) {
            sb.append(String.format("%-" + labelWidth + "s: %s (ID: %s)%n", "Patient",
                    patient.getName() != null ? patient.getName() : "N/A",
                    patient.getPatientId() != null ? patient.getPatientId() : "N/A"));
        } else {
            sb.append(String.format("%-" + labelWidth + "s: %s%n", "Patient", "N/A (No patient data)"));
        }

        if (doctor != null) {
            sb.append(String.format("%-" + labelWidth + "s: Dr. %s (ID: %s)%n", "Doctor",
                    doctor.getName() != null ? doctor.getName() : "N/A",
                    doctor.getStaffId() != null ? doctor.getStaffId() : "N/A"));
        } else {
            sb.append(String.format("%-" + labelWidth + "s: %s%n", "Doctor", "N/A (No doctor data)"));
        }

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Date & Time",
                dateTime != null ? dateTime.format(dtf) : "Not Set"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Status",
                status != null ? status : "Not Set"));

        sb.append(lineSeparator); // Add a blank line before multi-line fields

        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(String.format("%-" + labelWidth + "s:%n", "Reason for Visit"));
            sb.append(indent).append(wrapText(reason, 70 - indent.length())).append(lineSeparator); // Wrap text
        } else {
            sb.append(String.format("%-" + labelWidth + "s: %s%n", "Reason for Visit", "Not specified"));
        }

        if (diagnosisNotes != null && !diagnosisNotes.trim().isEmpty()) {
            sb.append(String.format("%-" + labelWidth + "s:%n", "Diagnosis Notes"));
            sb.append(indent).append(wrapText(diagnosisNotes, 70 - indent.length())).append(lineSeparator); // Wrap text
        } else if (status == AppointmentStatus.COMPLETED) { // Assuming AppointmentStatus.COMPLETED exists
            sb.append(String.format("%-" + labelWidth + "s: %s%n", "Diagnosis Notes", "Awaiting notes or N/A"));
        }
        sb.append("------------------------------------------").append(lineSeparator);

        return sb.toString();
    }

    private String wrapText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        String lineSeparator = System.lineSeparator();
        String indentForNextLines = "  "; // Indent for wrapped lines under the same field

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth && currentLine.length() > 0) {
                wrapped.append(currentLine.toString().trim()).append(lineSeparator).append(indentForNextLines);
                currentLine.setLength(0); // Reset current line
            }
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        if (currentLine.length() > 0) {
            wrapped.append(currentLine.toString().trim());
        }
        return wrapped.toString();
    }

}