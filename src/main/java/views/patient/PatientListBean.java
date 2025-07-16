package views.patient;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;

import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;

import org.pahappa.systems.hms.models.Staff;
import org.pahappa.systems.hms.services.PatientService;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Named("patientListBean")
@ViewScoped
public class PatientListBean implements Serializable {


    private List<Patient> patientList;
    private List<Patient> filteredPatientsList;
private final PatientService patientService;
public PatientListBean() {
    this.patientService = new PatientServiceImpl();
}
    private String globalFilterValue;


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
            patientList.sort(Comparator.comparing(Patient::getRegistrationDate).reversed());
            for(Patient patient : patientList) {
                System.out.println(patient.getRegistrationDate().toString());
            }
        }
        this.filteredPatientsList = new ArrayList<>(this.patientList);
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

    // ✅ Global Filter Function
    public boolean globalFilterFunction(Object value, Object filter, java.util.Locale locale) {
        String filterText = (filter == null) ? "" : filter.toString().toLowerCase();
        if (filterText.isBlank()) return true;

        Patient patient = (Patient) value;

        return (patient.getName() != null && patient.getName().toLowerCase().contains(filterText)) ||
                (patient.getEmail() != null && patient.getEmail().toLowerCase().contains(filterText)) ||
                (patient.getAddress() != null && patient.getAddress().toLowerCase().contains(filterText)) ||
                (patient.getPhoneNumber() != null && patient.getPhoneNumber().toLowerCase().contains(filterText));
    }
    public List<Patient> getFilteredPatientsList() {
        return filteredPatientsList;
    }

    public void setFilteredPatientsList(List<Patient> filteredPatientsList) {
        this.filteredPatientsList = filteredPatientsList;
    }

    public String getGlobalFilterValue() {
        return globalFilterValue;
    }

    public void setGlobalFilterValue(String globalFilterValue) {
        this.globalFilterValue = globalFilterValue;
    }
}
