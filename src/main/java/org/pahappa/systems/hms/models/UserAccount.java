package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.UserRole;

@Entity
@Table(name = "user_accounts")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private Long accountId; // Primary key for a conceptual "UserAccounts" table
    private String username;  // Unique, for login
    private String password;  // In a real DB, this would be hashed
    private UserRole role;
    @Column(name = "entity_id")
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

}