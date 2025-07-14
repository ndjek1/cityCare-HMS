package views.patient;


import annotations.MaxAge;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.*;
import org.pahappa.systems.hms.models.Patient;

import org.pahappa.systems.hms.services.impl.PatientServiceImpl;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

@Named("patientBean")
@ViewScoped
public class PatientBean implements Serializable {

    @NotEmpty(message = "Username is required.")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters.")
    private String username;
    @NotEmpty(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, with one uppercase, one lowercase, one number, and one special character.")
    private String password;
    @NotEmpty(message = "Full Name is required.")
    private String fullName;
    @NotEmpty(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    private String email;
    private String phone;
    @NotEmpty(message = "Address is required.")
    private String address;
    @NotEmpty(message = "Gender is required.")
    private String gender;
    @NotNull(message = "Birthday is required.")
    @Past(message = "Birthday must be a date in the past.")
    @MaxAge(value = 110, message = "Age cannot exceed 100 years. Please enter a valid birth date.")
    private Date birthday;

    private static PatientServiceImpl patientService;

    @PostConstruct
    public void init() {
        // Reset individual fields if this bean is reused (especially if SessionScoped)
        patientService = new PatientServiceImpl();
        this.username = null;
        this.password = null;
        this.fullName = null;
        this.email = null;
        this.phone = null;
        this.address = null;
        this.gender = null;
        this.birthday = null;
        System.out.println("StaffBean (ViewScoped) initialized/reset. HospitalService is: " + "INJECTED");
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public void register() {
        System.out.println("PatientBean.REGISTER() METHOD ENTERED. Current FullName: " + this.fullName + ", Username: " + this.username);
        if (this.fullName == null || this.fullName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Full name cannot be empty."));
            return;
        }



        System.out.println("Attempting to register staff: " + this.fullName);
        Optional<Patient> registeredPatientOpt = patientService.registerPatient(
                this.username,
                this.password,
                this.fullName,
                birthday,
                this.address,
                this.phone,
                this.email
        );

        if (registeredPatientOpt.isPresent()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Patient " + registeredPatientOpt.get().getName() + " registered successfully."));
            init(); // Reset the form fields
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration Failed", "Could not register staff. Username might be taken or other error. Check logs."));
        }
    }

}
