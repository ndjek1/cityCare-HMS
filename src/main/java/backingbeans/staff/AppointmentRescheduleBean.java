package backingbeans.staff; // Adjust package

import backingbeans.PageNavigationBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Appointment;
import models.Staff;

import services.impl.AppointmentServiceImpl;
import services.impl.StaffServiceImpl;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date; // For p:datePicker
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named("appointmentRescheduleBean")
@ViewScoped
public class AppointmentRescheduleBean implements Serializable {

    private final StaffServiceImpl staffService;
    private final AppointmentServiceImpl appointmentService;

    @Inject
    private PageNavigationBean pageNavigationBean; // To navigate to the reschedule form

    // Fields to hold the appointment being rescheduled
    private Appointment appointmentToReschedule;
    private Long appointmentIdToReschedule; // Passed as parameter

    // Fields for selecting new date/time
    private Long newSelectedDoctorId; // Could be same as original or different if allowed
    private Date newAppointmentDate;  // java.util.Date for p:datePicker
    private LocalTime newAppointmentTime;
    private List<Staff> availableDoctors;
    private List<LocalTime> newAvailableTimeSlots;

    public AppointmentRescheduleBean() {
        this.staffService = new  StaffServiceImpl();
        this.appointmentService = new  AppointmentServiceImpl();
    }

    @PostConstruct
    public void init() {
        availableDoctors = new ArrayList<>();
        newAvailableTimeSlots = new ArrayList<>();
        // Load available doctors (could be filtered by specialty later)
        List<Staff> allStaff = staffService.getAllStaffs();
        if (allStaff != null) {
            availableDoctors = allStaff.stream()
                    .filter(staff -> staff.getRole() == constants.UserRole.DOCTOR)
                    .collect(Collectors.toList());
        }
    }

    // Called when navigating to the reschedule page/view
    // This method would be called by an action that sets appointmentIdToReschedule
    // For example, via <f:param> or by setting it directly before navigation.
    public void loadAppointmentForReschedule() {
        if (appointmentIdToReschedule != null) {
            Optional<Appointment> optApp = appointmentService.findAppointmentById(appointmentIdToReschedule);
            if (optApp.isPresent()) {
                this.appointmentToReschedule = optApp.get();
                // Pre-fill new selections with current appointment details
                this.newSelectedDoctorId = this.appointmentToReschedule.getDoctor().getStaffId();
                this.newAppointmentDate = Date.from(this.appointmentToReschedule.getDateTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
                this.newAppointmentTime = this.appointmentToReschedule.getDateTime().toLocalTime();
                onNewDoctorOrDateChange(); // Load initial slots for the current new date/doctor
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Appointment not found."));
                this.appointmentToReschedule = null; // Clear if not found
            }
        } else {
            this.appointmentToReschedule = null;
        }
    }

    // AJAX listener when new doctor or new date changes
    public void onNewDoctorOrDateChange() {
        if (newAvailableTimeSlots == null) newAvailableTimeSlots = new ArrayList<>();
        newAvailableTimeSlots.clear();

        if (newSelectedDoctorId != null && newAppointmentDate != null) {
            LocalDate selectedDate = newAppointmentDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            List<LocalDateTime> doctorSlots = staffService.getDoctorAvailableSlotsForDate(newSelectedDoctorId, selectedDate);

            // Also add the *original* slot of the appointment being rescheduled,
            // as it should become available if the date/time/doctor changes.
            if (appointmentToReschedule != null &&
                    newSelectedDoctorId.equals(appointmentToReschedule.getDoctor().getStaffId()) &&
                    selectedDate.equals(appointmentToReschedule.getDateTime().toLocalDate())) {
                // If we are looking at the same doctor and same original date,
                // the original slot should appear as available for re-selection.
                boolean alreadyPresent = doctorSlots.stream().anyMatch(s -> s.toLocalTime().equals(appointmentToReschedule.getDateTime().toLocalTime()));
                if (!alreadyPresent) {
                    doctorSlots.add(appointmentToReschedule.getDateTime());
                }
            }


            if (doctorSlots != null) {
                newAvailableTimeSlots = doctorSlots.stream()
                        .map(LocalDateTime::toLocalTime)
                        .sorted()
                        .collect(Collectors.toList());
            }

            if (newAvailableTimeSlots.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "No slots available", "Selected doctor has no available slots on this date."));
            }
        }
        // Reset time if its previous value is no longer valid
        if (newAppointmentTime != null && !newAvailableTimeSlots.contains(newAppointmentTime)) {
            newAppointmentTime = null;
        }
    }

    public void confirmReschedule() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (appointmentToReschedule == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No appointment selected for reschedule."));
            return;
        }
        if (newSelectedDoctorId == null || newAppointmentDate == null || newAppointmentTime == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "New doctor, date, and time must be selected."));
            return;
        }

        LocalDate localNewDate = newAppointmentDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDateTime newAppointmentDateTime = LocalDateTime.of(localNewDate, newAppointmentTime);

        // Check if anything actually changed
        if (newSelectedDoctorId.equals(appointmentToReschedule.getDoctor().getStaffId()) &&
                newAppointmentDateTime.equals(appointmentToReschedule.getDateTime())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Change", "New schedule is the same as the old one."));
            return;
        }

        System.out.println("Rescheduling Appointment ID: " + appointmentToReschedule.getAppointmentId() +
                " to new Doctor ID: " + newSelectedDoctorId + ", new DateTime: " + newAppointmentDateTime);

        boolean success = appointmentService.rescheduleAppointment(
                appointmentToReschedule.getAppointmentId(),
                newSelectedDoctorId, // Pass the ID of the new doctor
                newAppointmentDateTime
        );

        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Appointment rescheduled successfully."));
            // Navigate back to appointment list or dashboard
            // Option 1: Update a global message component and stay (if in a dialog)
            // Option 2: Use PageNavigationBean to go back to appointments list
            pageNavigationBean.navigateToPatientAppointments(); // Example - you'll need this method
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Reschedule Failed", "Could not reschedule appointment. Slot may be unavailable or an error occurred."));
        }
    }
    // In AppointmentRescheduleBean.java
    public String prepareReschedule(Long appointmentId) {
        this.appointmentIdToReschedule = appointmentId;
        loadAppointmentForReschedule(); // Load the details for the form
        // Now, tell PageNavigationBean to show the reschedule fragment
        return pageNavigationBean.navigateToRescheduleAppointment(); // Needs this method in PageNavigationBean
    }

    // --- Getters and Setters ---
    public Appointment getAppointmentToReschedule() { return appointmentToReschedule; }
    public Long getAppointmentIdToReschedule() { return appointmentIdToReschedule; }
    public void setAppointmentIdToReschedule(Long appointmentIdToReschedule) { this.appointmentIdToReschedule = appointmentIdToReschedule; }
    public Long getNewSelectedDoctorId() { return newSelectedDoctorId; }
    public void setNewSelectedDoctorId(Long newSelectedDoctorId) { this.newSelectedDoctorId = newSelectedDoctorId; }
    public Date getNewAppointmentDate() { return newAppointmentDate; }
    public void setNewAppointmentDate(Date newAppointmentDate) { this.newAppointmentDate = newAppointmentDate; }
    public LocalTime getNewAppointmentTime() { return newAppointmentTime; }
    public void setNewAppointmentTime(LocalTime newAppointmentTime) { this.newAppointmentTime = newAppointmentTime; }
    public List<Staff> getAvailableDoctors() { return availableDoctors; }
    public List<LocalTime> getNewAvailableTimeSlots() { return newAvailableTimeSlots; }

    // For display
    public String formatLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) return "";
        return ldt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
    }
    public String formatLocalTime(LocalTime lt) {
        if (lt == null) return "";
        return lt.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}