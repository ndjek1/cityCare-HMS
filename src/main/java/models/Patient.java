package models;

import jakarta.persistence.*;
import constants.UserRole;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId; // Primary Key for Patients table
    private String name;
    private final UserRole role = UserRole.PATIENT; // Fixed role

    private String dateOfBirth;
    private String address;
    private String phoneNumber;
    private String email;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "patient_medical_record", // More descriptive table name
            joinColumns = @JoinColumn(name = "patien_id") // FK in 'doctor_available_slots' referencing Doctor's PK
    )
    @Column(name = "notes") // Name for the column storing the LocalDateTime itself
    private List<String> medicalHistory;
    private String insuranceDetails;

    public Patient(String name, String dateOfBirth, String address, String phoneNumber, String email) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.medicalHistory = new ArrayList<>();
    }

    public Patient() {

    }

    // Getters
    public Long getPatientId() { return patientId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public List<String> getMedicalHistory() { return medicalHistory; }
    public String getInsuranceDetails() { return insuranceDetails; }

    // Setters for mutable fields
    public void setName(String name) { this.name = name; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void addMedicalHistory(String entry) { this.medicalHistory.add(entry); }
    public void setInsuranceDetails(String insuranceDetails) { this.insuranceDetails = insuranceDetails; }

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