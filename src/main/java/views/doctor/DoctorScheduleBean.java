package views.doctor;


import views.UserAccountBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Staff;
import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date; // For p:datePicker (selectedDateForView)
import java.util.List;
import java.util.stream.Collectors;

@Named("doctorScheduleBean")
@ViewScoped
public class DoctorScheduleBean implements Serializable {


    private final StaffServiceImpl staffService;

    @Inject
    private UserAccountBean userAccountBean; // To get current doctor's ID

    private Staff currentDoctor;
    private Date selectedDateForView; // Bound to p:datePicker for viewing/managing slots
    private List<LocalDateTime> existingSlotsForSelectedDate;
    private List<LocalTime> timesToAdd; // For p:selectManyCheckbox or similar

    // Predefined time slots for selection (e.g., every 30 minutes)
    private List<LocalTime> availableTimeOptions;

    public DoctorScheduleBean() {
        this.staffService = new  StaffServiceImpl();
    }

    @PostConstruct
    public void init() {
        Object loggedInDetails = userAccountBean.getCurrentUserDetails();
        if (loggedInDetails instanceof Staff && ((Staff) loggedInDetails).getRole() == org.pahappa.systems.hms.constants.UserRole.DOCTOR) {
            currentDoctor = (Staff) loggedInDetails;
        } else {
            // Handle error: not a doctor or not logged in - redirect or show error
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Access Denied", "You must be a doctor to manage schedules."));

            return;
        }

        selectedDateForView = new Date(); // Default to today (java.util.Date)
        timesToAdd = new ArrayList<>();
        existingSlotsForSelectedDate = new ArrayList<>();
        populateAvailableTimeOptions();
        loadExistingSlotsForDate(); // Load for default date
    }

    private void populateAvailableTimeOptions() {
        availableTimeOptions = new ArrayList<>();
        LocalTime startTime = LocalTime.of(8, 0);  // 8 AM
        LocalTime endTime = LocalTime.of(17, 0); // 5 PM
        int slotDurationMinutes = 30;

        LocalTime currentTime = startTime;
        while (!currentTime.isAfter(endTime.minusMinutes(slotDurationMinutes))) {
            availableTimeOptions.add(currentTime);
            currentTime = currentTime.plusMinutes(slotDurationMinutes);
        }
    }

    public void loadExistingSlotsForDate() {
        if (currentDoctor == null || selectedDateForView == null) {
            existingSlotsForSelectedDate = new ArrayList<>();
            return;
        }
        LocalDate localDate = selectedDateForView.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        existingSlotsForSelectedDate = staffService.getDoctorAvailableSlotsForDate(currentDoctor.getStaffId(), localDate);
        // Clear timesToAdd as they are specific to a new "add" operation
        timesToAdd = new ArrayList<>();
    }

    public void addSelectedSlots() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (currentDoctor == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Doctor not identified."));
            return;
        }
        if (selectedDateForView == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select a date."));
            return;
        }
        if (timesToAdd == null || timesToAdd.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No Selection", "Please select time slots to add."));
            return;
        }

        LocalDate localSelectedDate = selectedDateForView.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        List<LocalDateTime> slotsToPersist = timesToAdd.stream()
                .map(time -> LocalDateTime.of(localSelectedDate, time))
                .filter(newSlot -> existingSlotsForSelectedDate.stream().noneMatch(existing -> existing.equals(newSlot))) // Avoid duplicates
                .collect(Collectors.toList());

        if (slotsToPersist.isEmpty() && !timesToAdd.isEmpty()){
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Slots Exist", "Selected time slots are already marked as available."));
            timesToAdd.clear();
            return;
        }
        if (slotsToPersist.isEmpty()){
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No new slots", "No new unique time slots to add."));
            return;
        }


        boolean success = staffService.addDoctorAvailability(currentDoctor.getStaffId(), slotsToPersist);

        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Availability updated successfully."));
            loadExistingSlotsForDate(); // Refresh the displayed list
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not update availability."));
        }
        timesToAdd.clear(); // Clear selection
    }

    public void removeSlot(LocalDateTime slotToRemove) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (currentDoctor == null || slotToRemove == null) return;

        List<LocalDateTime> toRemoveList = new ArrayList<>();
        toRemoveList.add(slotToRemove);

        boolean success = staffService.removeDoctorAvailability(currentDoctor.getStaffId(), toRemoveList);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Slot removed successfully."));
            loadExistingSlotsForDate(); // Refresh
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not remove slot."));
        }
    }

    // --- Getters and Setters ---
    public Staff getCurrentDoctor() { return currentDoctor; }
    public Date getSelectedDateForView() { return selectedDateForView; }
    public void setSelectedDateForView(Date selectedDateForView) { this.selectedDateForView = selectedDateForView; }
    public List<LocalDateTime> getExistingSlotsForSelectedDate() { return existingSlotsForSelectedDate; }
    public List<LocalTime> getTimesToAdd() { return timesToAdd; }
    public void setTimesToAdd(List<LocalTime> timesToAdd) { this.timesToAdd = timesToAdd; }
    public List<LocalTime> getAvailableTimeOptions() { return availableTimeOptions; }

    // Formatter for display
    public String formatLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) return "";
        return ldt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
    }
    public String formatLocalTime(LocalTime lt) {
        if (lt == null) return "";
        return lt.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
}