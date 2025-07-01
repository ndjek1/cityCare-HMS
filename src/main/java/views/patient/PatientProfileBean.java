package views.patient; // Adjust package

import views.UserAccountBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;

import org.pahappa.systems.hms.services.impl.AuthServiceImpl;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;

import java.io.Serializable;
import java.util.Optional;

@Named("patientProfileBean")
@ViewScoped // Good for forms, data is loaded once per view and kept during AJAX
public class PatientProfileBean implements Serializable {


    private final PatientServiceImpl patientService;
    private final AuthServiceImpl authService;

    @Inject
    private UserAccountBean userAccountBean; // To get the logged-in patient

    private Patient currentPatient; // Holds the patient data for the form
    private boolean editMode = false; // To toggle between display and edit views

    // Optional: For updating UserAccount credentials
    private String newUsername;
    private String newPassword;
    private String confirmNewPassword;
    private String currentUsername;
    public PatientProfileBean() {
        this.patientService = new PatientServiceImpl();
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
        loadPatientProfile();
        // Don't reset newUsername/newPassword here unless you always want them blank on view load
    }

    public void loadPatientProfile() {
        if (userAccountBean.isLoggedIn() && userAccountBean.isPatient()) {
            Object patientDetails = userAccountBean.getCurrentUserDetails();
            if (patientDetails instanceof Patient) {
                // It's good to reload from service to ensure fresh data,
                // especially if other parts of the app could modify it.
                Optional<Patient> freshPatientOpt = patientService.findPatientById(((Patient) patientDetails).getPatientId());
                this.currentUsername  = userAccountBean.getCurrentUser().getUsername();
                if (freshPatientOpt.isPresent()) {
                    this.currentPatient = freshPatientOpt.get();
                } else {
                    // Handle case where patient might not be found (e.g., deleted)
                    this.currentPatient = null; // Or redirect with error
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not load patient profile."));
                }
            } else {
                this.currentPatient = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Logged in user is not a patient or details missing."));
            }
        } else {
            this.currentPatient = null; // Or redirect to login
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Not Logged In", "Please log in to view your profile."));
        }
        this.editMode = false; // Start in display mode
        this.newUsername = null; // Reset credential change fields
        this.newPassword = null;
        this.confirmNewPassword = null;
    }

    public void toggleEditMode() {
        this.editMode = !this.editMode;
        if (this.editMode && this.currentPatient != null && userAccountBean.getCurrentUser() != null) {
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
        if (currentPatient == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No patient data to save."));
            return;
        }

        // --- 1. Update Patient Demographics ---
        boolean patientInfoUpdated = patientService.updatePatientRecord(
                currentPatient.getPatientId(),
                currentPatient.getAddress(),
                currentPatient.getPhoneNumber(),
                currentPatient.getEmail(),
                currentPatient.getInsuranceDetails() // Assuming your service method takes these
                // Pass other editable fields like name, gender, dob if they are also updatable
        );

        if (patientInfoUpdated) {
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


        if (patientInfoUpdated || credentialsUpdated) {
            this.editMode = false; // Switch back to display mode after saving
            loadPatientProfile(); // Reload to show fresh data (especially if username changed)
        }
    }

    // Getters and Setters
    public Patient getCurrentPatient() { return currentPatient; }
    public void setCurrentPatient(Patient currentPatient) { this.currentPatient = currentPatient; }
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode = editMode; }

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }


}