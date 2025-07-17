package views.patient;

import org.pahappa.systems.hms.models.Patient;
import views.UserAccountBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;

import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("appointmentListBean")
@ViewScoped
public class AppointmentListBean implements Serializable {

    private final AppointmentServiceImpl appointmentService;

    @Inject
    private UserAccountBean userAccountBean; // To get current doctor's ID

    private List<Appointment> appointmentsList;
    private List<Appointment> filteredAppointmentsList;

    public AppointmentListBean() {
        this.appointmentService = new AppointmentServiceImpl();
    }

    @PostConstruct
    public void init() {
        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsList = appointmentService.getAppointmentsForPatient(userAccountBean.getCurrentUser().getEntityId());
        if (appointmentsList == null) {
            appointmentsList = new ArrayList<>(); // Avoid NullPointerException in DataTable
            System.err.println("StaffListBean: hospitalService.getAllStaffs() returned null.");
        } else {
            System.out.println("StaffListBean: Loaded " + appointmentsList.size() + " staff members.");
        }
        this.filteredAppointmentsList = new ArrayList<>(appointmentsList);
    }

    // ✅ Global Filter Function
    public boolean globalFilterFunction(Object value, Object filter, java.util.Locale locale) {
        if (value == null || filter == null) return true;

        Appointment appointment = (Appointment) value;
        if (filter instanceof String filterText) {
            String lowerFilter = filterText.toLowerCase().trim();
            if (lowerFilter.isBlank()) return true;
            return (appointment.getDoctor().getName() != null && appointment.getDoctor().getName().toLowerCase().contains(filterText)) ||
                    (appointment.getStatus().toString() != null && appointment.getStatus().toString().toLowerCase().contains(filterText)) ||
                    (appointment.getDateTime() != null && appointment.getDateTime().toLocalDate().toString().contains(lowerFilter));
        }
        return  true;
    }

    // Getter for the JSF page

    public List<Appointment> getFilteredAppointmentsList() {
        return filteredAppointmentsList;
    }

    public void setFilteredAppointmentsList(List<Appointment> filteredAppointmentsList) {
        this.filteredAppointmentsList = filteredAppointmentsList;
    }

    public List<Appointment> getAppointmentsList() {
        return appointmentsList;
    }

    // Optional: Action method to refresh the list
    public void refreshList() {
        loadAppointments();
    }


    // Optional: Action method for deleting a staff member (example)
    public void cancelAppointment(Appointment aptToDelete) {
        if (aptToDelete != null) {
            System.out.println("Attempting to cancel appointment: " + aptToDelete.getAppointmentId());
            appointmentService.cancelAppointment(aptToDelete.getAppointmentId()); // Assuming deleteStaff takes ID or object
            loadAppointments(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }

}
