package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.UserRole;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long patientId; // Primary Key for Patients table
    private String name;
    private final UserRole role = UserRole.PATIENT; // Fixed role

    @Column(name = "date_of_birth")
    private Date dateOfBirth;
    private String address;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String email;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "patient_medical_record", // More descriptive table name
            joinColumns = @JoinColumn(name = "patient_id") // FK in 'doctor_available_slots' referencing Doctor's PK
    )
    @Column(name = "notes") // Name for the column storing the LocalDateTime itself
    private List<String> medicalHistory;
    @Column(name = "is_deleted")
    private boolean deleted =  false;
    @Column(name = "insurance_details")
    private String insuranceDetails;
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    public Patient(String name, Date dateOfBirth, String address, String phoneNumber, String email) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.medicalHistory = new ArrayList<>();
        this.deleted = false;
        this.registrationDate = LocalDateTime.now();
    }

    public Patient() {

    }


    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Getters
    public Long getPatientId() { return patientId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public List<String> getMedicalHistory() { return medicalHistory; }
    public String getInsuranceDetails() { return insuranceDetails; }
    public  LocalDateTime getRegistrationDate() { return registrationDate; }

    // Setters for mutable fields
    public void setName(String name) { this.name = name; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void addMedicalHistory(String entry) { this.medicalHistory.add(entry); }
    public void setInsuranceDetails(String insuranceDetails) { this.insuranceDetails = insuranceDetails; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    @Override
    public String toString() {
        String lineSeparator = System.lineSeparator();
        int labelWidth = 20; // Consistent label width
        String sectionIndent = "  "; // Indentation for sections like Address, Medical History
        String itemIndent = "    - "; // Indentation for list items

        StringBuilder sb = new StringBuilder();

        sb.append("==============================================").append(lineSeparator);

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Patient ID", patientId != null ? patientId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Name", name != null ? name : "N/A"));

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Date of Birth", dateOfBirth != null ? dateOfBirth : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Phone Number", phoneNumber != null ? phoneNumber : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Email Address", email != null ? email : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Insurance Details", insuranceDetails != null && !insuranceDetails.isEmpty() ? insuranceDetails : "N/A"));

        sb.append(lineSeparator); // Blank line for separation

        // Address (could be multi-line if you parse it or have separate fields)
        sb.append(String.format("%-" + labelWidth + "s:%n", "Address"));
        if (address != null && !address.trim().isEmpty()) {
            // Simple split by comma for demonstration, or use the wrapText helper from Appointment
            for (String line : address.split(",")) { // Naive split, better if address is structured
                sb.append(sectionIndent).append(line.trim()).append(lineSeparator);
            }
        } else {
            sb.append(sectionIndent).append("N/A").append(lineSeparator);
        }

        sb.append(lineSeparator);

        // Medical History
        sb.append(String.format("%-" + labelWidth + "s:%n", "Medical History"));
        if (medicalHistory != null && !medicalHistory.isEmpty()) {
            for (String entry : medicalHistory) {
                // You might want a helper to wrap long medical history entries too
                sb.append(itemIndent).append(entry).append(lineSeparator);
            }
        } else {
            sb.append(sectionIndent).append("No medical history recorded.").append(lineSeparator);
        }

        sb.append("==============================================").append(lineSeparator);

        return sb.toString();
    }
}