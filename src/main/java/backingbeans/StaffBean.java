package backingbeans;

import constants.HospitalDepartment;
import constants.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped; // Consider ViewScoped for forms
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Staff;
import models.UserAccount; // Assuming you have this for user credentials
import services.HospitalService;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Named("staffBean")
@ViewScoped // CHANGED TO SESSION SCOPED - IF THIS IS FOR A SINGLE REGISTRATION, ViewScoped is better.
// If you use SessionScoped, you MUST explicitly clear fields after successful registration.
public class StaffBean implements Serializable {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private Date birthday; // In Java, this is java.util.Date for p:datePicker
    private UserRole role;
    private HospitalDepartment department;


    @Inject
    private HospitalService hospitalService;

    @PostConstruct
    public void init() {
        // Reset individual fields if this bean is reused (especially if SessionScoped)
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
        System.out.println("StaffBean (ViewScoped) initialized/reset. HospitalService is: " + (hospitalService != null ? "INJECTED" : "NULL - PROBLEM!"));
    }

    // Getters and Setters for all individual fields (username, password, fullName, etc.)
    // ... (Your existing getters and setters are fine) ...
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



    public void register() {
        System.out.println("STAFFBEAN.REGISTER() METHOD ENTERED. Current FullName: " + this.fullName + ", Username: " + this.username);
//        if (this.fullName == null || this.fullName.trim().isEmpty()) {
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Full name cannot be empty."));
//            return;
//        }
//        // Add more validation for other required fields (username, password, role, department)
//
//        // You need to decide how your HospitalService.registerStaff method works.
//        // Does it take individual parameters or a Staff object?
//        // Let's assume it takes individual parameters as per your old service method signature:
//        // hospitalService.registerStaff(username, password, role, name, department, address, email, phone, dob_string, gender)
//
        String dobString = null;
        if (this.birthday != null) {
            // Format java.util.Date to String if your service or entity expects String for DoB
            // Be careful with date formats and timezones.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Or "MM/dd/yyyy" based on your model
            dobString = sdf.format(this.birthday);
        }

        System.out.println("Attempting to register staff: " + this.fullName);
        Optional<Staff> registeredStaffOpt = hospitalService.registerStaff(
                this.username,
                this.password, // SEND HASHED PASSWORD TO SERVICE
                this.role,
                this.fullName,
                this.department,
                this.address,
                this.email,
                this.phone,
                dobString, // Or pass Date object if service handles it
                this.gender
        );

        if (registeredStaffOpt.isPresent()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Staff member " + registeredStaffOpt.get().getName() + " registered successfully."));
            init(); // Reset the form fields
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", "Could not register staff. Username might be taken or other error. Check logs."));
        }
    }

    // For dropdowns
    public List<UserRole> getAvailableRoles() {
        return Arrays.asList(UserRole.DOCTOR, UserRole.RECEPTIONIST, UserRole.ADMINISTRATOR);
    }






    public List<HospitalDepartment> getAvailableDepartments() {
        return Arrays.asList(HospitalDepartment.values());
    }
}