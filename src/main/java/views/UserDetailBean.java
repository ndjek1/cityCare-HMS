package views;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Staff;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;
import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.io.Serializable;

@SessionScoped
@Named("userDetailBean")
public class UserDetailBean implements Serializable {

    private Patient selectedPatient;
    private Staff selectedStaff;
    private final PatientServiceImpl patientService;
    private final StaffServiceImpl staffService;

    public UserDetailBean() {
        this.patientService = new PatientServiceImpl();
        this.staffService = new StaffServiceImpl();
    }

    public void loadPatient(Long patientId) {
        this.selectedPatient = patientService.findPatientById(patientId).orElse(null);
        System.err.println("Patient with id: " + patientId + " is found");
        this.selectedStaff = null;
    }

    public void loadStaff(Long staffId) {
        this.selectedStaff = staffService.findStaffById(staffId).orElse(null);
        this.selectedPatient = null;
    }

    public boolean isPatient() {
        return selectedPatient != null;
    }

    public boolean isStaff() {
        return selectedStaff != null;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public Staff getSelectedStaff() {
        return selectedStaff;
    }

    public boolean isDialogReady() {
        return selectedPatient != null || selectedStaff != null;
    }
}
