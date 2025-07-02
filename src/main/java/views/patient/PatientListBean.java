package views.patient;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;

import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;

import org.pahappa.systems.hms.services.PatientService;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named("patientListBean")
@ViewScoped
public class PatientListBean implements Serializable {


    private List<Patient> patientList;
private final PatientService patientService;
public PatientListBean() {
    this.patientService = new PatientServiceImpl();
}
    private List<Patient> filteredPatients;
    private String globalFilterValue;



    // This is your custom filtering function
    public boolean filterGlobal(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().toLowerCase();

        if (filterText == null || filterText.trim().isEmpty()) {
            return true;
        }

        Patient patient = (Patient) value;

        return (patient.getName() != null && patient.getName().toLowerCase().contains(filterText))
                || (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(filterText))
                || (patient.getPhoneNumber() != null && patient.getPhoneNumber().toLowerCase().contains(filterText));
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

    public List<Patient> getFilteredPatients() {
        return filteredPatients;
    }

    public void setFilteredPatients(List<Patient> filteredPatients) {
        this.filteredPatients = filteredPatients;
    }

    public String getGlobalFilterValue() {
        return globalFilterValue;
    }

    public void setGlobalFilterValue(String globalFilterValue) {
        this.globalFilterValue = globalFilterValue;
    }
}
