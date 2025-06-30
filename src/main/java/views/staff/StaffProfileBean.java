package views.staff;

import views.UserAccountBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Staff;

import org.pahappa.systems.hms.services.impl.AuthServiceImpl;
import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.io.Serializable;
import java.util.Optional;


@Named("staffProfileBean")
@ViewScoped
public class StaffProfileBean implements Serializable {

    private final StaffServiceImpl staffService;
    private final AuthServiceImpl authService;

    @Inject
    private UserAccountBean userAccountBean; // To get the logged-in patient

    private Staff currentStaff; // Holds the patient data for the form
    private boolean editMode = false; // To toggle between display and edit views

    // Optional: For updating UserAccount credentials
    private String newUsername;
    private String newPassword;
    private String confirmNewPassword;
    private String currentUsername;

    public StaffProfileBean() {
        this.staffService = new StaffServiceImpl();
        this.authService = new AuthServiceImpl();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    @PostConstruct
    public void init() {
        loadStaffProfile();
        // Don't reset newUsername/newPassword here unless you always want them blank on view load
    }

    public void loadStaffProfile() {
        // Point 1: Check UserAccountBean state
        if (userAccountBean.isLoggedIn() && userAccountBean.isStaff()) {
            // Point 2: Get staff details from UserAccountBean
            Object staffDetails = userAccountBean.getCurrentUserDetails(); // Renamed from getLoggedInEntityDetails for clarity

            // Point 3: Check if staffDetails is actually a Staff instance
            if (staffDetails instanceof Staff) {
                Staff loggedInStaff = (Staff) staffDetails; // Cast for clarity

                // Point 4: Reload from service for fresh data
                // Ensure loggedInStaff.getStaffId() is not null here
                if (loggedInStaff.getStaffId() != null) {
                    Optional<Staff> freshStaffOpt = staffService.findStaffById(loggedInStaff.getStaffId());

                    // Point 5: Check if staff was found by service
                    if (freshStaffOpt.isPresent()) {
                        this.currentStaff = freshStaffOpt.get(); // <<<< SUCCESSFUL POPULATION
                        // Optionally set currentUsername from the fresh staff's associated UserAccount if needed for display consistency
                        if (userAccountBean.getCurrentUser()!= null) { // Assuming Staff has a UserAccount reference
                            this.currentUsername = userAccountBean.getCurrentUser().getUsername();
                        } else {
                            // Or get from userAccountBean if staff entity doesn't directly hold it or it's not populated
                            this.currentUsername = userAccountBean.getCurrentUser().getUsername();
                        }
                    } else {
                        // Staff not found by ID in DB - this is a problem
                        this.currentStaff = null;
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not load staff profile details from database. Staff ID: " + loggedInStaff.getStaffId()));
                        System.err.println("StaffProfileBean: Staff with ID " + loggedInStaff.getStaffId() + " not found by hospitalService.findStaffById.");
                    }
                } else {
                    this.currentStaff = null;
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Logged-in staff has no ID. Profile cannot be loaded."));
                    System.err.println("StaffProfileBean: Logged-in staffDetails (Staff object) has a null staffId.");
                }
            } else {
                // staffDetails from userAccountBean is not a Staff instance
                this.currentStaff = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Logged in user is not recognized as staff or details missing. Details type: " + (staffDetails != null ? staffDetails.getClass().getName() : "null")));
                System.err.println("StaffProfileBean: userAccountBean.getCurrentUserDetails() did not return a Staff instance. Actual type: " + (staffDetails != null ? staffDetails.getClass().getName() : "null"));
            }
        } else {
            // User is not logged in or not identified as staff by UserAccountBean
            this.currentStaff = null;
            String reason = !userAccountBean.isLoggedIn() ? "User not logged in." : "User is not staff.";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Profile Unavailable", reason + " Please log in as staff."));
            System.err.println("StaffProfileBean: Profile load condition failed. LoggedIn: " + userAccountBean.isLoggedIn() + ", IsStaff: " + userAccountBean.isStaff());
        }

        // These should be reset regardless, or only if currentStaff is null
        if (this.currentStaff == null) {
            this.editMode = false;
            this.newUsername = null;
            this.newPassword = null;
            this.confirmNewPassword = null;
            // this.currentUsername = null; // Only null out if staff is null
        } else {
            this.editMode = false; // Always start in display mode
        }
    }

    public void toggleEditMode() {
        this.editMode = !this.editMode;
        if (this.editMode && this.currentStaff != null && userAccountBean.getCurrentUser() != null) {
            // Pre-fill newUsername if user wants to change it, from existing username
            this.newUsername = userAccountBean.getCurrentUser().getUsername();
        } else {
            this.newUsername = null;
        }
        this.newPassword = null;
        this.confirmNewPassword = null;
    }

    public void saveProfileChanges() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (currentStaff == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No patient data to save."));
            return;
        }

        // --- 1. Update Staff Demographics ---
        boolean staffToUpdate = staffService.updateStaffRecord(
               currentStaff.getStaffId(),
               currentStaff.getAddress(),
               currentStaff.getPhone(),
               currentStaff.getEmail()
        );

        if (staffToUpdate) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Personal information updated."));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update personal information."));
        }

        // --- 2. Update Login Credentials (Optional) ---
        boolean credentialsUpdated = false;
        if ((newPassword != null && !newPassword.isEmpty()) ||
                (newUsername != null && !newUsername.equals(userAccountBean.getCurrentUser().getUsername()))) {

            if (newPassword != null && !newPassword.isEmpty() && (confirmNewPassword == null || !newPassword.equals(confirmNewPassword))) {
                context.addMessage("passwordFields", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Mismatch", "New passwords do not match."));
                // Do not proceed with credential update if passwords mismatch
            } else {
                // If newUsername is empty but password is set, use current username
                String usernameToUpdate = (newUsername != null && !newUsername.trim().isEmpty()) ? newUsername.trim() : userAccountBean.getCurrentUser().getUsername();
                String passwordToUpdate = (newPassword != null && !newPassword.isEmpty()) ? newPassword : null; // Pass null if not changing

                // Call service method to update credentials.
                // This method needs to handle hashing the new password.
                // It should also check if the newUsername is already taken (if different from current).
                credentialsUpdated = authService.updateLoginCredentialsForUser(
                        userAccountBean.getCurrentUser().getAccountId(), // Or however you identify the UserAccount
                        usernameToUpdate,
                        passwordToUpdate // Service method should HASH this
                );

                if (credentialsUpdated) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Login credentials updated."));
                    // Important: If username changed, the UserAccountBean needs to be updated
                    // or the user might need to log out and log back in.
                    // For simplicity, we'll assume UserAccountBean reloads user details if needed.
                    userAccountBean.refreshLoggedInUserDetails(); // You'd need such a method in UserAccountBean
                } else {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credentials Error", "Could not update login credentials. Username might be taken or an error occurred."));
                }
            }
        }


        if (staffToUpdate || credentialsUpdated) {
            this.editMode = false; // Switch back to display mode after saving
            loadStaffProfile(); // Reload to show fresh data (especially if username changed)
        }
    }

    // Getters and Setters
    public Staff getCurrentStaff() { return currentStaff; }
    public void setCurrentStaff(Staff currentStaff) { this.currentStaff = currentStaff; }
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode = editMode; }

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }

}
