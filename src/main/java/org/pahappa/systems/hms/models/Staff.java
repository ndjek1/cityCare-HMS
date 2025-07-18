package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.HospitalDepartment;
import org.pahappa.systems.hms.constants.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.Date; // For Date of Birth
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "staffs")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Enumerated(EnumType.STRING) // Good practice for enums
    private HospitalDepartment department;

    @Enumerated(EnumType.STRING) // Good practice for enums
    private UserRole role;

    private String name;
    private String address;
    @NotEmpty(message = "Email is required.") // Also ensures the field is not empty
    @Email(message = "Invalid email format. Please provide a valid address.")
    private String email;
    @Column(name = "phone_number")
    private String phone; // Consider renaming to phoneNumber for clarity
    @Column(name = "is_deleted")
    private boolean deleted =  false;
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Temporal(TemporalType.DATE) // Maps to SQL DATE
    @Column(name = "date_of_birth")
    private Date dateOfBirth;     // Changed from String dob

    private String gender;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "doctor_available_slots",
            joinColumns = @JoinColumn(name = "staff_id") // Changed to staff_id to match common FK naming
    )
    @Column(name = "available_date_time")
    @OrderBy("available_date_time ASC") // Keep them sorted
    private Set<LocalDateTime> availableSlots = new HashSet<>(); // Use Set for uniqueness
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    // Constructors
    public Staff() {
        // Initialize collections if not done at declaration (already done here)
        // this.availableSlots = new HashSet<>();
    }

    public Staff(String name, UserRole role, HospitalDepartment department, String address, String email, String phone, Date dateOfBirth, String gender) {
        this.name = name;
        this.role = role;
        this.department = department;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth; // Changed to Date
        this.gender = gender;
        this.deleted = false;
        this.registrationDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public HospitalDepartment getDepartment() { return department; }
    public void setDepartment(HospitalDepartment department) { this.department = department; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getDateOfBirth() { return dateOfBirth; } // Changed getter name for consistency
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; } // Changed setter

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Set<LocalDateTime> getAvailableSlots() {
        if (this.availableSlots == null) {
            this.availableSlots = new HashSet<>();
        }
        return availableSlots;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setAvailableSlots(Set<LocalDateTime> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public boolean addAvailability(LocalDateTime slot) {
        if (this.availableSlots == null) {
            this.availableSlots = new HashSet<>();
        }
        return this.availableSlots.add(slot);
    }

    public boolean removeAvailability(LocalDateTime slot) {
        if (this.availableSlots == null) {
            return false; // Or throw an exception if null is not expected here
        }
        return this.availableSlots.remove(slot);
    }

}