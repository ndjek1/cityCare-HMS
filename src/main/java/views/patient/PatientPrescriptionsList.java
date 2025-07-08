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
    }

    // Getters and Setters
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
