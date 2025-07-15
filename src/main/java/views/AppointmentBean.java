package views; // Adjust package

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Staff; // Assuming your Doctor is a type of Staff
import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;
import org.pahappa.systems.hms.services.impl.StaffServiceImpl;
import views.staff.AppointmentsListBean;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date; // For p:datePicker
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named("appointmentBookingBean")
@ViewScoped
public class AppointmentBean implements Serializable {

    private final AppointmentServiceImpl  appointmentService;
    private final PatientServiceImpl patientService;
    private final StaffServiceImpl staffService;

    @Inject
    private UserAccountBean userAccountBean; // To get logged-in user details

    private Long selectedPatientId;    // For receptionist/doctor booking for a patient
    private Long selectedDoctorId;
    private Date appointmentDate;      // Bound to p:datePicker (java.util.Date)
    private LocalTime appointmentTime; // Bound to p:calendar with timeOnly=true or custom input
    private String reason;

    private List<Patient> availablePatients;   // For receptionist to select from
    private List<Staff> availableDoctors;    // For patient/receptionist to select from
    private List<LocalTime> availableTimeSlots; // To populate after selecting a doctor and date
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a"); // e.g., 09:00 AM
    private static final DateTimeFormatter VALUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm"); // e.g., 09:00 (24-hour)
    @Inject
    AppointmentsListBean appointmentsListBean;

    public AppointmentBean() {
        this.appointmentService = new AppointmentServiceImpl();
        this.patientService = new PatientServiceImpl();
        this.staffService = new StaffServiceImpl();
    }

    @PostConstruct
    public void init() {
        // Initialize lists
        availablePatients = new ArrayList<>();
        availableDoctors = new ArrayList<>();
        availableTimeSlots = new ArrayList<>();

        // If user is a patient, pre-select them
        if (userAccountBean.isLoggedIn() && userAccountBean.isPatient()) {
            Object patientDetails = userAccountBean.getCurrentUserDetails();
            if (patientDetails instanceof Patient) {
                this.selectedPatientId = ((Patient) patientDetails).getPatientId();
            }
        } else {
            // If receptionist or admin, load all patients for selection
            loadAvailablePatients();
        }
        loadAvailableDoctors(); // Load all doctors initially
        resetFormFields();
    }

    private void resetFormFields() {
        // Don't reset selectedPatientId if pre-filled for a patient
        // this.selectedPatientId = null;
        this.selectedDoctorId = null;
        this.appointmentDate = null;
        this.appointmentTime = null; // Or a default time
        this.reason = null;
        this.availableTimeSlots.clear();
    }

    public void loadAvailablePatients() {
        availablePatients = patientService.getAllPatients();
    }

    public void loadAvailableDoctors() {
        // In a real app, you might filter doctors by specialty if selected
        // Assuming getAllStaffs() returns all staff, filter for DOCTOR role
        List<Staff> allStaff = staffService.getAllStaffs();
        if (allStaff != null) {
            availableDoctors = allStaff.stream()
                    .filter(staff -> staff.getRole() == org.pahappa.systems.hms.constants.UserRole.DOCTOR)
                    .collect(Collectors.toList());
        }
    }

    public void onDoctorOrDateChange() {
        if (selectedDoctorId != null && appointmentDate != null) {
            // Clear previous results only if valid selection
            availableTimeSlots.clear();

            LocalDate selectedDate = appointmentDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            List<LocalDateTime> doctorSlots = staffService.getDoctorAvailableSlotsForDate(selectedDoctorId, selectedDate);
            if (doctorSlots != null) {
                availableTimeSlots = doctorSlots.stream()
                        .map(LocalDateTime::toLocalTime)
                        .sorted()
                        .collect(Collectors.toList());
            }

            if (availableTimeSlots.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "No slots available",
                                "Selected doctor has no available slots on this date."));
            }
        }
    }


    // --- Formatting methods for the XHTML ---
    public String formatLocalTimeForDisplay(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DISPLAY_TIME_FORMATTER);
    }

    public String formatLocalTimeForValue(LocalTime time) {
        if (time == null) {
            return ""; // Or null, depending on how noSelectionOption is handled
        }
        return time.format(VALUE_TIME_FORMATTER);
    }

    public String formatLocalDateTimeForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
    }
    public void bookAppointment() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (selectedPatientId == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Patient must be selected."));
            return;
        }
        if (selectedDoctorId == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Doctor must be selected."));
            return;
        }
        if (appointmentDate == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Appointment date must be selected."));
            return;
        }
        if (appointmentTime == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Appointment time must be selected."));
            return;
        }
        if (reason == null || reason.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Reason for appointment is required."));
            return;
        }

        // Convert java.util.Date and LocalTime to LocalDateTime
        LocalDate localDate = appointmentDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDateTime appointmentDateTime = LocalDateTime.of(localDate, appointmentTime);

        System.out.println("Booking attempt: PatientID=" + selectedPatientId + ", DoctorID=" + selectedDoctorId +
                ", DateTime=" + appointmentDateTime + ", Reason=" + reason);

        Optional<Appointment> bookedAppointment = appointmentService.bookAppointment(
                selectedPatientId,
                selectedDoctorId,
                appointmentDateTime,
                reason
        );

        if (bookedAppointment.isPresent()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Appointment booked successfully for " + bookedAppointment.get().getPatient().getName() +
                            " with Dr. " + bookedAppointment.get().getDoctor().getName() +
                            " on " + appointmentDateTime.toLocalDate() + " at " + appointmentDateTime.toLocalTime()));
            resetFormFields(); // Clear form for next booking
            // Potentially refresh doctor's available slots if they changed
            onDoctorOrDateChange();
            appointmentsListBean.refreshList();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Booking Failed",
                    "Could not book appointment. The slot might have just been taken or an error occurred."));
        }
    }

    // Getters and Setters
    public Long getSelectedPatientId() { return selectedPatientId; }
    public void setSelectedPatientId(Long selectedPatientId) { this.selectedPatientId = selectedPatientId; }

    public Long getSelectedDoctorId() { return selectedDoctorId; }
    public void setSelectedDoctorId(Long selectedDoctorId) { this.selectedDoctorId = selectedDoctorId; }

    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public List<Patient> getAvailablePatients() { return availablePatients; }
    public List<Staff> getAvailableDoctors() { return availableDoctors; }
    public List<LocalTime> getAvailableTimeSlots() { return availableTimeSlots; }

    // Helper to check if current user is a patient for conditional rendering
    public boolean isCurrentUserPatient() {
        return userAccountBean.isLoggedIn() && userAccountBean.isPatient();
    }
}