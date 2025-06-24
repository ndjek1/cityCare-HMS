package backingbeans;


import constants.AppointmentStatus;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import models.Patient;
import models.Staff;

import java.io.Serializable;
import java.time.LocalDateTime;

@Named("appointmentBean")
@SessionScoped
public class AppointmentBean implements Serializable {
    private Patient patient;
    private Staff doctor;
    private LocalDateTime dateTime;
    private String reason;
    private AppointmentStatus  status;
    private String diagnosisNotes;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Staff getDoctor() {
        return doctor;
    }

    public void setDoctor(Staff doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }
}
