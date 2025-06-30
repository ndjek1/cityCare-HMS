package org.pahappa.systems.hms.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.pahappa.systems.hms.constants.UserRole;

@Entity
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accountId; // Primary key for a conceptual "UserAccounts" table
    private String username;  // Unique, for login
    private String password;  // In a real DB, this would be hashed
    private UserRole role;
    private Long entityId;  // Foreign key to PatientId, DoctorId, AdminId, etc.

public UserAccount(String username, String password, UserRole role, Long entityId) {
    this.username = username;
    this.password = password;
    this.role = role;
    this.entityId = entityId;
}

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public UserAccount() {

    }

    // Getters
public Long getAccountId() { return accountId; }
public String getUsername() { return username; }
public String getPassword() { return password; } // For authentication check
public UserRole getRole() { return role; }
public Long getEntityId() { return entityId; } // To fetch specific Patient/Doctor object

    @Override
    public String toString() {
        String lineSeparator = System.lineSeparator();
        int labelWidth = 22; // Adjusted label width for "Associated Entity ID"
        StringBuilder sb = new StringBuilder();

        sb.append("------------------------------------------").append(lineSeparator);

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Account ID", accountId != null ? accountId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Username", username != null ? username : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Role", role != null ? role.toString() : "N/A")); // Assuming UserRole enum has good toString()
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Associated Entity ID", entityId != null ? entityId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Password", "******** (Protected)")); // NEVER display actual password or hash

        sb.append("------------------------------------------").append(lineSeparator);

        return sb.toString();
    }
}