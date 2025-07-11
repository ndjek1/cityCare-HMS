package views.admin;



import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Medication;
import org.pahappa.systems.hms.services.PharmacyService;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Named("medicationAdminBean")
@ViewScoped
public class MedicationAdminBean implements Serializable {

    @Inject
    private PharmacyService pharmacyService;

    private List<Medication> allMedications;
    private Medication selectedMedication; // For editing or adding
    private boolean is_New = true; // To distinguish between add and edit mode in the dialog

    @PostConstruct
    public void init() {
        System.out.println("MedicationAdminBean: Initializing.");
        loadAllMedications();
    }

    private void loadAllMedications() {
        if (pharmacyService != null) {
            allMedications = pharmacyService.getFullMedicationCatalog();
        } else {
            allMedications = new ArrayList<>();
            System.err.println("MedicationAdminBean: PharmacyService not injected.");
        }
    }

    // Prepares the dialog for adding a NEW medication
    public void openNew() {
        this.selectedMedication = new Medication(); // Create a new blank instance
        this.is_New = true;
        System.out.println("MedicationAdminBean: Preparing dialog for new medication.");
    }

    // Prepares the dialog for EDITING an EXISTING medication
    public void openForEdit(Medication medication) {
        // We might want to reload the medication from the service to ensure we have the latest state
        Optional<Medication> freshMedOpt = pharmacyService.findMedicationById(medication.getMedicationId());
        if (freshMedOpt.isPresent()) {
            this.selectedMedication = freshMedOpt.get();
            this.is_New = false;
            System.out.println("MedicationAdminBean: Preparing dialog to edit medication: " + this.selectedMedication.getName());
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Medication not found. It may have been deleted."));
            this.selectedMedication = null; // Clear if not found
        }
    }

    public void saveMedication() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (selectedMedication == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No medication data to save."));
            return;
        }

        try {
            if (is_New) {
                // Logic for adding a new medication
                Optional<Medication> addedMed = pharmacyService.addNewMedication(
                        selectedMedication.getName(),
                        selectedMedication.getDescription(),
                        selectedMedication.getUnitPrice() != null ? selectedMedication.getUnitPrice().doubleValue() : 0.0,
                        selectedMedication.getStockLevel()
                );
                if (addedMed.isPresent()) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "New medication added successfully."));
                } else {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not add medication. Name might already exist."));
                }
            } else {
                // Logic for updating an existing medication
                // We need an updateMedication method in our service
                pharmacyService.updateMedication(selectedMedication);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Medication updated successfully."));
            }

            loadAllMedications(); // Refresh the list in any case
            // PrimeFaces.current().executeScript("PF('medicationDialogWidget').hide();"); // Hide dialog on success
            // To make this work, button oncomplete needs to check for validation errors
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "System Error", "An unexpected error occurred while saving."));
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public List<Medication> getAllMedications() {
        return allMedications;
    }

    public Medication getSelectedMedication() {
        return selectedMedication;
    }

    public void setSelectedMedication(Medication selectedMedication) {
        this.selectedMedication = selectedMedication;
    }

    public boolean isIs_New() {
        return is_New;
    }

    public void setIs_New(boolean is_New) {
        this.is_New = is_New;
    }
}