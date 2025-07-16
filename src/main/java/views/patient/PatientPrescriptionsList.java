package views.patient;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.models.Prescription;
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;
import views.UserAccountBean;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class PatientPrescriptionsList implements Serializable {

    @Inject
    private UserAccountBean userAccountBean;

    // We'll inject both services
    private final PrescriptionServiceImpl prescriptionService;

    private List<Prescription> patientPrescriptions =  new ArrayList<>();
    private List<Prescription> filteredPrescriptions = new ArrayList<>();
    private Prescription selectedPrescription;


    public PatientPrescriptionsList() {
        this.prescriptionService = new PrescriptionServiceImpl();
    }


    @PostConstruct
    public void init() {
        loadPrescriptions();
    }

    public void loadPrescriptions() {
        if (userAccountBean.isLoggedIn() && userAccountBean.isPatient()) {
            Patient currentPatient = (Patient) userAccountBean.getCurrentUserDetails();
            if (currentPatient != null && currentPatient.getPatientId() != null) {
                // You will need a method in your service layer like this:
                this.patientPrescriptions = prescriptionService.findByPatientId(currentPatient.getPatientId());
            }
        }
        if (this.patientPrescriptions == null) {
            this.patientPrescriptions = new ArrayList<>();
        }
        this.filteredPrescriptions = new ArrayList<>(patientPrescriptions);
    }

    public boolean globalFilterFunction(Object value, Object filter, Locale locale) {
        if (value == null || filter == null) return true;

        Prescription prescription = (Prescription) value;

        // Case 1: Text filter from search box (String)
        if (filter instanceof String filterText) {
            String lowerFilter = filterText.toLowerCase().trim();
            if (lowerFilter.isBlank()) return true;

            return (prescription.getPaymentStatus() != null && prescription.getPaymentStatus().toString().toLowerCase().contains(lowerFilter)) ||
                    (prescription.getPrescriptionDate() != null && prescription.getPrescriptionDate().toString().contains(lowerFilter)) ||
                    (prescription.getTotalCost() > 0 && String.valueOf(prescription.getTotalCost()).toLowerCase().contains(lowerFilter));
        }


        return true; // fallback
    }

    // Getters and Setters

    public List<Prescription> getFilteredPrescriptions() {
        return filteredPrescriptions;
    }

    public void setFilteredPrescriptions(List<Prescription> filteredPrescriptions) {
        this.filteredPrescriptions = filteredPrescriptions;
    }

    public List<Prescription> getPatientPrescriptions() {
        return patientPrescriptions;
    }

    public Prescription getSelectedPrescription() {
        return selectedPrescription;
    }

    public void setSelectedPrescription(Prescription selectedPrescription) {
        // This is called by the actionListener on the "View Details" button
        this.selectedPrescription = selectedPrescription;
    }
}
