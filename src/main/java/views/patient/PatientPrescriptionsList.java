package views.patient;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Prescription;
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;
import views.UserAccountBean;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class PatientPrescriptionsList implements Serializable {

    @Inject
    private UserAccountBean userAccountBean;

    // We'll inject both services
    private final PrescriptionServiceImpl prescriptionService;

    private List<Prescription> patientPrescriptions =  new ArrayList<>();
    private Prescription selectedPrescription;

    public PatientPrescriptionsList() {
        this.prescriptionService = new PrescriptionServiceImpl();
    }

    @PostConstruct
    public void init() {
        // Initialize lists to avoid NullPointerException in the view
        if (userAccountBean != null && userAccountBean.isPatient()) {
            Patient currentPatient = (Patient) userAccountBean.getCurrentUserDetails();
            if (currentPatient != null) {
                // Load data for the logged-in patient
                this.patientPrescriptions = prescriptionService.findByPatientId(currentPatient.getPatientId());
                System.err.println("Found prescriptions for patient " + currentPatient.getPatientId());
            } else {
                System.err.println("PatientHistoryBean: Logged in user is a patient, but detailed object is null.");
            }
        }
    }


    // Getters for the view to access the data
    public List<Prescription> getPatientPrescriptions() {
        return patientPrescriptions;
    }

    public Prescription getSelectedPrescription() {
        return selectedPrescription;
    }

    public void setSelectedPrescription(Prescription selectedPrescription) {
        System.err.println("Selected prescription: " + selectedPrescription.getPatient().getName());
        this.selectedPrescription = selectedPrescription;
    }
}
