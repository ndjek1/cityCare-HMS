package views.doctor;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.constants.PrescriptionStatus;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Staff;
import org.pahappa.systems.hms.models.Prescription;
import org.pahappa.systems.hms.services.PrescriptionService; // You'll need a PrescriptionService
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;
import views.UserAccountBean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named("prescriptionBean")
@SessionScoped
public class PrescriptionBean implements Serializable {

    // You'll need to create this service layer for Prescription CRUD operations
    private final PrescriptionServiceImpl prescriptionService;

    @Inject
    private UserAccountBean userAccountBean; // Assumes this bean holds the logged-in user

    private Appointment selectedAppointment; // The appointment we are prescribing for
    private List<Prescription> existingPrescriptions; // Prescriptions already on record for this appointment
    private Prescription newPrescription; // The prescription object bound to the "add new" form

    public PrescriptionBean() {
        this.prescriptionService = new PrescriptionServiceImpl(); // Instantiate your service
    }

    @PostConstruct
    public void init() {
        // This is called when the bean is created for the view.
        // We will populate data when prepareDialog is called.
    }

    // This method is called by the "Manage Prescriptions" button to open and populate the dialog.
    public void prepareDialog(Appointment appointment) {
        if (appointment == null) {
            System.err.println("prepareDialog called with null appointment.");
            return;
        }
        this.selectedAppointment = appointment;
        // Fetch prescriptions already associated with this appointment
        this.existingPrescriptions = prescriptionService.findByAppointmentId(appointment.getAppointmentId());
        // Prepare a blank prescription for the "Add New" form
        resetNewPrescriptionForm();
        System.out.println("PrescriptionBean prepared for appointment ID: " + appointment.getAppointmentId());
    }

    public void resetNewPrescriptionForm() {
        this.newPrescription = new Prescription();
        this.newPrescription.setStartDate(LocalDate.now()); // Sensible default
    }

    public void saveNewPrescription() {
        if (selectedAppointment == null || newPrescription == null || userAccountBean.getCurrentUserDetails() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "System error: Required data is missing.");
            return;
        }
        if (newPrescription.getMedicationName() == null || newPrescription.getMedicationName().trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Medication name cannot be empty.");
            return;
        }

        try {
            // Set the relationships on the new prescription object
            newPrescription.setAppointment(selectedAppointment);
            newPrescription.setPatient(selectedAppointment.getPatient());
            newPrescription.setDoctor((Staff) userAccountBean.getCurrentUserDetails());
            newPrescription.setStatus(PrescriptionStatus.ACTIVE);

            prescriptionService.registerPrescription(newPrescription);
            addMessage(FacesMessage.SEVERITY_INFO, "Success", "Prescription for '" + newPrescription.getMedicationName() + "' saved.");

            // Refresh the list of existing prescriptions and reset the form for another entry
            this.existingPrescriptions.add(newPrescription);
            resetNewPrescriptionForm();

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Save Failed", "An error occurred while saving the prescription.");
            e.printStackTrace();
        }
    }

//    public void deletePrescription(Prescription prescription) {
//        try {
//            prescriptionService.deletePrescription(prescription);
//            existingPrescriptions.remove(prescription); // Remove from the view
//            addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Prescription has been deleted.");
//        } catch(Exception e) {
//            addMessage(FacesMessage.SEVERITY_ERROR, "Delete Failed", "Could not delete the prescription.");
//            e.printStackTrace();
//        }
//    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public Appointment getSelectedAppointment() { return selectedAppointment; }
    public List<Prescription> getExistingPrescriptions() { return existingPrescriptions; }
    public Prescription getNewPrescription() { return newPrescription; }
    public void setNewPrescription(Prescription newPrescription) { this.newPrescription = newPrescription; }
}
