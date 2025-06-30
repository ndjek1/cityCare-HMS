package views.doctor;

import views.UserAccountBean;
import org.pahappa.systems.hms.constants.AppointmentStatus; // Make sure you have this enum
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;

import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("doctorAppointmentsBean")
@ViewScoped
public class DoctorAppointmentsBean implements Serializable {

    private final AppointmentServiceImpl appointmentService;

    @Inject
    private UserAccountBean userAccountBean;

    // Instead of one list, we'll have two (or more, depending on your needs)
    private List<Appointment> scheduledAppointments;
    private List<Appointment> completedAppointments;
    // You could also have a list for CANCELLED if you want to display those separately

    public DoctorAppointmentsBean() {
        this.appointmentService = new AppointmentServiceImpl();
        System.out.println("DoctorAppointmentsBean CONSTRUCTOR called. HashCode: " + System.identityHashCode(this));
    }

    @PostConstruct
    public void init() {
        System.out.println("DoctorAppointmentsBean @PostConstruct init() - HashCode: " + System.identityHashCode(this) + " - START");
        this.scheduledAppointments = new ArrayList<>(); // Initialize
        this.completedAppointments = new ArrayList<>(); // Initialize
        loadAndCategorizeAppointments();
        System.out.println("DoctorAppointmentsBean @PostConstruct init() - HashCode: " + System.identityHashCode(this) + " - END.");
    }

    public void loadAndCategorizeAppointments() {
        System.out.println("DoctorAppointmentsBean loadAndCategorizeAppointments() - HashCode: " + System.identityHashCode(this) + " - START");
        // Clear existing lists before loading
        if (this.scheduledAppointments == null) this.scheduledAppointments = new ArrayList<>();
        else this.scheduledAppointments.clear();

        if (this.completedAppointments == null) this.completedAppointments = new ArrayList<>();
        else this.completedAppointments.clear();


        if (userAccountBean == null || userAccountBean.getCurrentUser() == null || userAccountBean.getCurrentUser().getEntityId() == null) {
            System.err.println("DoctorAppointmentsBean: Cannot load appointments, UserAccountBean or current user/entity ID is null.");
            return;
        }
        if (appointmentService != null) {
            Long doctorId = userAccountBean.getCurrentUser().getEntityId();
            if (userAccountBean.isDoctor()) {
                List<Appointment> allAppointmentsForDoctor = appointmentService.getAppointmentsForDoctor(doctorId);

                if (allAppointmentsForDoctor != null) {
                    for (Appointment app : allAppointmentsForDoctor) {
                        if (app.getStatus() == AppointmentStatus.COMPLETED) {
                            this.completedAppointments.add(app);
                        } else{ // Or any other "active" statuses
                            this.scheduledAppointments.add(app);
                        }
                        // Add other categories if needed (e.g., CANCELLED)
                    }
                    System.out.println("DoctorAppointmentsBean: Loaded " + this.scheduledAppointments.size() + " scheduled and " +
                            this.completedAppointments.size() + " completed appointments for Dr. ID: " + doctorId);
                } else {
                    System.err.println("DoctorAppointmentsBean: hospitalService.getAppointmentsForDoctor() returned null for Dr. ID: " + doctorId);
                }
            } else {
                System.err.println("DoctorAppointmentsBean: Current user is not a doctor. Not loading doctor-specific appointments.");
            }
        } else {
            System.err.println("DoctorAppointmentsBean: HospitalService not injected, cannot load appointments.");
        }
        System.out.println("DoctorAppointmentsBean loadAndCategorizeAppointments() - HashCode: " + System.identityHashCode(this) + " - END.");
    }

    // Getters for the JSF page
    public List<Appointment> getScheduledAppointments() {
        System.out.println("getScheduledAppointments() - HashCode: " + System.identityHashCode(this) + " - Returning list of size: " + (this.scheduledAppointments != null ? this.scheduledAppointments.size() : "null"));
        return scheduledAppointments;
    }

    public List<Appointment> getCompletedAppointments() {
        System.out.println("getCompletedAppointments() - HashCode: " + System.identityHashCode(this) + " - Returning list of size: " + (this.completedAppointments != null ? this.completedAppointments.size() : "null"));
        return completedAppointments;
    }

    public void refreshAppointments() { // Renamed from refreshList for clarity
        System.out.println("refreshAppointments() called - HashCode: " + System.identityHashCode(this));
        loadAndCategorizeAppointments();
    }

    public void cancelAppointment(Appointment aptToCancel) {
        System.out.println("cancelAppointment() called - HashCode: " + System.identityHashCode(this));
        if (appointmentService != null && aptToCancel != null) {
            boolean success = appointmentService.cancelAppointment(aptToCancel.getAppointmentId());
            if (success) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Cancelled", "Appointment ID " + aptToCancel.getAppointmentId() + " cancelled."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not cancel appointment ID " + aptToCancel.getAppointmentId() + "."));
            }
        }
        loadAndCategorizeAppointments(); // Refresh all categorized lists
    }

}