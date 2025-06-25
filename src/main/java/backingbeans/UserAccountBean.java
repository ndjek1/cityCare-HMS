package backingbeans;

import constants.UserRole;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.UserAccount;
import services.HospitalService;

import java.io.Serializable;
import java.util.Optional;

@Named("userAccountBean")
@SessionScoped
public class UserAccountBean implements Serializable {
    private String username;
    private String password;
    private UserAccount currentUser; // Stores the logged-in UserAccount object
    private Object currentUserDetails; // Stores Patient, Doctor, etc. object
    @Inject
    private HospitalService hospitalService;


    public String login() {
        Optional<UserAccount> accountOpt = hospitalService.login(username, password);
        if (accountOpt.isPresent()) {
            currentUser = accountOpt.get();
            currentUserDetails = hospitalService.getLoggedInEntityDetails(); // Fetch detailed entity
            // Redirect to dashboard
            return "/index.xhtml?faces-redirect=true";
        } else {
            currentUser = null;
            currentUserDetails = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login Failed", "Invalid username or password."));
            return null; // Stay on login page
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        currentUser = null;
        currentUserDetails = null;
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public UserRole getCurrentUserRole() {
        return (currentUser != null) ? currentUser.getRole() : null;
    }

    // --- Getters for checking specific roles in XHTML ---
    public boolean isPatient() {
        return currentUser != null && currentUser.getRole() == UserRole.PATIENT;
    }

    public boolean isDoctor() {
        return currentUser != null && currentUser.getRole() == UserRole.DOCTOR;
    }

    public boolean isReceptionist() {
        return currentUser != null && currentUser.getRole() == UserRole.RECEPTIONIST;
    }

    public boolean isAdministrator() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMINISTRATOR;
    }

    // Getters and setters for username, password, currentUser, currentUserDetails
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserAccount getCurrentUser() { return currentUser; }
    public Object getCurrentUserDetails() { return currentUserDetails; }

    public String getCurrentUserName() {
        if (currentUserDetails instanceof models.Patient) return ((models.Patient) currentUserDetails).getName();
        if (currentUserDetails instanceof models.Staff) return ((models.Staff) currentUserDetails).getName();
        // Add other types
        return (currentUser != null) ? currentUser.getUsername() : "Guest";
    }

}
