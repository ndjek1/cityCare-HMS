package views.doctor; // Adjust package

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped; // Could also be RequestScoped if dialog state is always fresh
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;

import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;

import java.io.Serializable;
import java.time.LocalDateTime; // For displaying appointment date/time

@Named("diagnosisEntryBean")
@ViewScoped // ViewScoped is good. If the dialog is simple, RequestScoped might even work
// but ViewScoped allows it to hold state if there are AJAX interactions within the dialog itself.
public class DiagnosisEntryBean implements Serializable {

    private final AppointmentServiceImpl appointmentService;

    // To refresh the main list after saving diagnosis
    @Inject
    private DoctorAppointmentsBean doctorAppointmentsBean;

    private Long appointmentIdToDiagnose;
    private String patientNameForDisplay;
    private String doctorNameForDisplay;
    private LocalDateTime appointmentDateTimeForDisplay;
    private String diagnosisNotes;
    private boolean dialogReady = false; // To control rendering of dialog content

    public DiagnosisEntryBean(){
        this.appointmentService = new AppointmentServiceImpl();
        System.out.println("DiagnosisEntryBean CONSTRUCTOR - HashCode: " + System.identityHashCode(this));
    }

    @PostConstruct
    public void init() {
        System.out.println("DiagnosisEntryBean @PostConstruct - HashCode: " + System.identityHashCode(this));
        resetDialog();
    }

    public void resetDialog() {
        this.appointmentIdToDiagnose = null;
        this.patientNameForDisplay = null;
        this.doctorNameForDisplay = null;
        this.appointmentDateTimeForDisplay = null;
        this.diagnosisNotes = null;
        this.dialogReady = false;
        System.out.println("DiagnosisEntryBean: Dialog fields reset.");
    }

    // This method is called by the "Record Diagnosis" button from the appointments list.
    // It receives the full Appointment object to extract necessary info.
    public void prepareForDiagnosis(Appointment appointment) {
        if (appointment != null) {
            this.appointmentIdToDiagnose = appointment.getAppointmentId();
            this.patientNameForDisplay = appointment.getPatient() != null ? appointment.getPatient().getName() : "N/A";
            this.doctorNameForDisplay = appointment.getDoctor() != null ? appointment.getDoctor().getName() : "N/A";
            this.appointmentDateTimeForDisplay = appointment.getDateTime();
            this.diagnosisNotes = null; // Clear any previous notes
            this.dialogReady = true;
            System.out.println("DiagnosisEntryBean: Prepared for Appt ID: " + this.appointmentIdToDiagnose +
                    ", Patient: " + this.patientNameForDisplay);
        } else {
            resetDialog(); // Ensure clean state if null appointment passed
            System.err.println("DiagnosisEntryBean: prepareForDiagnosis called with null appointment.");
        }
    }

    public void saveDiagnosis() {
        System.out.println("DiagnosisEntryBean saveDiagnosis() - Appt ID: " + this.appointmentIdToDiagnose +
                ", Notes: '" + this.diagnosisNotes + "'");
        FacesContext context = FacesContext.getCurrentInstance();

        if (appointmentIdToDiagnose == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No appointment selected. Please close and retry."));
            return;
        }
        if (diagnosisNotes == null || diagnosisNotes.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Diagnosis notes cannot be empty."));
            return;
        }

        boolean success = appointmentService.recordDiagnosis(appointmentIdToDiagnose, diagnosisNotes);

        if (success) {
            if (doctorAppointmentsBean != null) {
                doctorAppointmentsBean.refreshAppointments(); // Refresh the lists on the main page
            }
            resetDialog();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Diagnosis recorded successfully for appointment ID: " + appointmentIdToDiagnose));
            doctorAppointmentsBean.loadAndCategorizeAppointments(); // Tell the other bean to refresh its list
            resetDialog(); // Clear this bean's state for the next use
            // The oncomplete of the save button in the dialog will handle hiding the dialog
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Could not record diagnosis. Appointment might not be in correct state or an error occurred."));
        }
    }

    // Getters and Setters
    public Long getAppointmentIdToDiagnose() { return appointmentIdToDiagnose; }
    // No setter for appointmentIdToDiagnose, it's set by prepareForDiagnosis
    public String getPatientNameForDisplay() { return patientNameForDisplay; }
    public String getDoctorNameForDisplay() { return doctorNameForDisplay; }
    public LocalDateTime getAppointmentDateTimeForDisplay() { return appointmentDateTimeForDisplay; }
    public String getDiagnosisNotes() { return diagnosisNotes; }
    public void setDiagnosisNotes(String diagnosisNotes) { this.diagnosisNotes = diagnosisNotes; }
    public boolean isDialogReady() { return dialogReady; }
}