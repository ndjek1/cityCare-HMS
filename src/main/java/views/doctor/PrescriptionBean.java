package views.doctor;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Medication;
import org.pahappa.systems.hms.models.PrescribedMedication;
import org.pahappa.systems.hms.services.impl.PharmacyServiceImpl;
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("prescriptionBean")
@ViewScoped
public class PrescriptionBean implements Serializable {

    @Inject private PrescriptionServiceImpl prescriptionService;
    @Inject private PharmacyServiceImpl pharmacyService; // To get list of available medications
    @Inject private DoctorAppointmentsBean doctorAppointmentsBean; // To refresh appointment list

    private Long appointmentIdForPrescription;
    private Appointment appointmentDetails;
    private List<PrescribedMedication> itemsToAdd; // List of medications being added in the dialog

    private List<Medication> availableMedications; // From the catalog
    private Long selectedMedicationId;
    private String dosage;
    private String frequency;
    private String duration;
    private int quantity;

    @PostConstruct
    public void init() {
        itemsToAdd = new ArrayList<>();
        availableMedications = pharmacyService.getActiveMedications();
        if (availableMedications == null) availableMedications = new ArrayList<>();
    }

    public void prepareNewPrescription(Appointment appointment) {
        this.appointmentIdForPrescription = appointment.getAppointmentId();
        this.appointmentDetails = appointment;
        this.itemsToAdd.clear();
        clearMedicationForm();
    }

    private void clearMedicationForm() {
        this.selectedMedicationId = null;
        this.dosage = null;
        this.frequency = null;
        this.duration = null;
        this.quantity = 1;
    }

    public void addMedicationItem() {
        // Find selected medication from catalog
        Medication selectedMed = pharmacyService.findMedicationById(selectedMedicationId).orElse(null);
        if (selectedMed == null) { /* add error message */ return; }

        // Add to the list
        itemsToAdd.add(new PrescribedMedication(selectedMed.getMedicationId(), selectedMed.getName(), dosage, frequency, duration, quantity));
        clearMedicationForm();
    }

    public void removeMedicationItem(PrescribedMedication item) {
        itemsToAdd.remove(item);
    }

    public void savePrescription() {
        if (appointmentIdForPrescription == null || itemsToAdd.isEmpty()) { /* add error */ return; }

        boolean success = prescriptionService.createPrescription(appointmentIdForPrescription, itemsToAdd);
        System.out.println("Trying to save prescription");

        if (success) {
            // ... add success message ...
            System.out.println("Prescription created successfully.");
            doctorAppointmentsBean.refreshAppointments(); // Refresh appointment list
            // PrimeFaces.current().executeScript("PF('managePrescriptionDialogWidget').hide();");
        } else {
            System.out.println("Failed to save prescription");

            // ... add error message ...
        }
    }

    public Appointment getAppointmentDetails() {
        return appointmentDetails;
    }

    public void setAppointmentDetails(Appointment appointmentDetails) {
        this.appointmentDetails = appointmentDetails;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public Long getSelectedMedicationId() {
        return selectedMedicationId;
    }

    public void setSelectedMedicationId(Long selectedMedicationId) {
        this.selectedMedicationId = selectedMedicationId;
    }

    public List<Medication> getAvailableMedications() {
        return availableMedications;
    }

    public void setAvailableMedications(List<Medication> availableMedications) {
        this.availableMedications = availableMedications;
    }

    public List<PrescribedMedication> getItemsToAdd() {
        return itemsToAdd;
    }

    public void setItemsToAdd(List<PrescribedMedication> itemsToAdd) {
        this.itemsToAdd = itemsToAdd;
    }

    public Long getAppointmentIdForPrescription() {
        return appointmentIdForPrescription;
    }

    public void setAppointmentIdForPrescription(Long appointmentIdForPrescription) {
        this.appointmentIdForPrescription = appointmentIdForPrescription;
    }
}
