package backingbeans.patient;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;

import jakarta.inject.Named;
import models.Patient;

import services.PatientService;
import services.impl.PatientServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("patientListBean")
@ViewScoped
public class PatientListBean implements Serializable {


    private List<Patient> patientList;
private final PatientService patientService;
public PatientListBean() {
    this.patientService = new PatientServiceImpl();
}
    @PostConstruct
    public void init() {
        loadPatients();
    }

    private void loadPatients() {
        patientList = patientService.getAllPatients();
        if (patientList == null) {
            patientList = new ArrayList<>(); // Avoid NullPointerException in DataTable
            System.err.println("StaffListBean: hospitalService.getAllStaffs() returned null.");
        } else {
            System.out.println("StaffListBean: Loaded " + patientList.size() + " staff members.");
        }
    }

    // Getter for the JSF page
    public List<Patient> getPatientList() {
        return patientList;
    }

    // Optional: Action method to refresh the list
    public void refreshList() {
        loadPatients();
    }

    // Optional: Action method for deleting a staff member (example)
    public void deletePatient(Patient patientToDelete) {
        if (patientService != null && patientToDelete != null) {
            System.out.println("Attempting to delete staff: " + patientToDelete.getName());
            patientService.deletePatient(patientToDelete); // Assuming deleteStaff takes ID or object
            loadPatients(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }
}
