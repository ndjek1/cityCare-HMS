package backingbeans;

import constants.UserRole;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import jakarta.inject.Named;
import models.Patient;
import models.Staff;
import models.UserAccount;

import services.LoginResult;
import services.impl.AuthServiceImpl;

import java.io.Serializable;
import java.util.Optional;

@Named("userAccountBean")
@SessionScoped
public class UserAccountBean implements Serializable {
    private String username;
    private String password;
    private UserAccount currentUser;
    private Object currentUserDetails;
    private final AuthServiceImpl authService;


    public UserAccountBean() {
        this.authService = new AuthServiceImpl();
    }


    // In UserAccountBean.java
    public String login() {
        Optional<LoginResult> resultOpt = authService.login(username, password);

        if (resultOpt.isPresent()) {
            LoginResult result = resultOpt.get();

            // --- DEFENSIVE CHECK ---
            if (result.getUserAccount() == null) {
                // This is the error condition we are trying to catch.
                System.err.println("CRITICAL ERROR: LoginResult was present, but contained a NULL UserAccount!");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_FATAL, "System Error", "User account could not be loaded after login. Please contact support."));
                return null; // Abort and stay on the login page.
            }

            // If we pass the check, we can safely set the state.
            this.currentUser = result.getUserAccount();
            this.currentUserDetails = result.getUserDetails();

            // Now this line is safe to execute.
            System.out.println("Login successful for user: " + this.currentUser.getUsername() +
                    ". Details object type: " + (this.currentUserDetails != null ? this.currentUserDetails.getClass().getSimpleName() : "null"));

            return "/template.xhtml?faces-redirect=true";
        } else {
            // ... (your standard login failure logic) ...
            return null;
        }
    }

    public void logout() {
        this.currentUser = null;
        this.currentUserDetails = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        // Redirect to login page after logout
        // return "/login.xhtml?faces-redirect=true"; // from a method that returns String
    }

    public boolean isLoggedIn() { return currentUser != null; }
    // ... other helper methods like isStaff(), isPatient(), etc.


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

    public boolean isStaff() {
        return currentUser != null && currentUser.getRole() != UserRole.PATIENT;
    }

    // Getters and setters for username, password, currentUser, currentUserDetails
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserAccount getCurrentUser() { return currentUser; }
    public Object getCurrentUserDetails() { return currentUserDetails; }

    // In UserAccountBean.java
    public String getCurrentUserName() {
        if (currentUserDetails instanceof Staff) {
            return ((Staff) currentUserDetails).getName();
        }
        if (currentUserDetails instanceof Patient) {
            return ((Patient) currentUserDetails).getName();
        }
        if (currentUser != null) {
            return currentUser.getUsername(); // Fallback to username
        }
        return "User"; // Default
    }
    public String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        StringBuilder initials = new StringBuilder();
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(parts[parts.length - 1].charAt(0));
        }
        return initials.toString().toUpperCase();
    }
    public void refreshLoggedInUserDetails() {
        if (this.currentUser != null) {
            // Re-fetch UserAccount to get updated username/password (though password hash won't be used for display)
            Optional<UserAccount> updatedUserOpt = authService.findUserAccountById(this.currentUser.getAccountId()); // You'll need this service method
            if (updatedUserOpt.isPresent()) {
                this.currentUser = updatedUserOpt.get();
                // Re-fetch the specific entity details (Patient/Staff)
                // This assumes your HospitalService has a method to get entity details by UserAccount
                Optional<Object> detailsOpt = Optional.ofNullable(authService.getSpecificEntityDetails(this.currentUser));
                this.currentUserDetails = detailsOpt.orElse(null);
                System.out.println("UserAccountBean: Logged in user details refreshed.");
            }
        }
    }




}
