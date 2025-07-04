package views.admin;


import annotations.MaxAge;
import jakarta.enterprise.context.SessionScoped;
import org.pahappa.systems.hms.constants.HospitalDepartment;
import org.pahappa.systems.hms.constants.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
// Import the standard validation annotations
import jakarta.validation.constraints.*;
        import org.pahappa.systems.hms.models.Staff;

import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

@Named("staffBean")
@SessionScoped
public class StaffBean implements Serializable {

    // --- Validation directly on the fields ---

    @NotEmpty(message = "Username is required.")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters.")
    private String username;

    @NotEmpty(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, with one uppercase, one lowercase, one number, and one special character.")
    private String password;

    @NotEmpty(message = "Full name is required.")
    private String fullName;

    @NotEmpty(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;

    @NotEmpty(message = "Phone number is required.")
    // We will add a custom validator for the Ugandan phone format in the XHTML
    private String phone;

    @NotEmpty(message = "Address is required.")
    private String address;

    @NotEmpty(message = "Gender is required.")
    private String gender;

    @NotNull(message = "Birthday is required.")
    @Past(message = "Birthday must be a date in the past.")
    @MaxAge(value = 110, message = "Age cannot exceed 100 years. Please enter a valid birth date.")
    private Date birthday;

    @NotNull(message = "Role is required.")
    private UserRole role;

    @NotNull(message = "Department is required.")
    private HospitalDepartment department;

    private StaffServiceImpl staffService;

    @PostConstruct
    public void init() {
        // Reset individual fields if this bean is reused (especially if SessionScoped)
        this.staffService = new StaffServiceImpl();
        this.username = null;
        this.password = null;
        this.fullName = null;
        this.email = null;
        this.phone = null;
        this.address = null;
        this.gender = null;
        this.birthday = null;
        this.role = null;
        this.department = null;
    }


    public void register() {
        // Step 1: Check if JSF's validation phase failed.
        // The framework runs all @NotEmpty, @Email, etc. checks automatically.
        System.out.println(this.phone);
        if (FacesContext.getCurrentInstance().isValidationFailed()) {
            // If any validation failed, stop immediately.
            // Error messages are already added to the context by the framework.
            return;
        }

        // If we reach here, all basic validations have passed.
        System.out.println("All validations passed. Proceeding with registration for: " + this.fullName);

        // Your existing registration logic is perfect here.
        Optional<Staff> registeredStaffOpt = staffService.registerStaff(this.username,this.password,this.role,this.fullName,this.department,this.address,this.email,this.phone,this.birthday,this.gender);

        if (registeredStaffOpt.isPresent()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member " + registeredStaffOpt.get().getName() + " registered successfully."));
            init(); // Reset the form fields for the next entry
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", "Could not register staff. The username or email might already be in use."));
        }
    }

    // You no longer need the static validateEmail, validatePassword, etc. methods here.
    // They are replaced by the annotations.

    // Getters for dropdowns remain the same...
    public List<UserRole> getAvailableRoles() {
        return Arrays.asList((UserRole.values()));
    }
    public List<HospitalDepartment> getAvailableDepartments() {
        return Arrays.asList((HospitalDepartment.values()));
    }
    // Getters and Setters...
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthday() { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public HospitalDepartment getDepartment() { return department; }
    public void setDepartment(HospitalDepartment department) { this.department = department; }
}