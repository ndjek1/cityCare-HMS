package views.staff;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Appointment;

import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class AppointmentsListBean implements Serializable {

    private final AppointmentServiceImpl appointmentService;

    private List<Appointment> appointmentList;

    public AppointmentsListBean() {
        this.appointmentService = new  AppointmentServiceImpl();
    }

    @PostConstruct
    public void init() {
        loadAppointments();
    }

    private void loadAppointments() {
        appointmentList = appointmentService.findAllAppointments();
        if (appointmentList == null) {
            appointmentList = new ArrayList<>(); // Avoid NullPointerException in DataTable
            System.err.println("AppointmentListBean: hospitalService.getAllStaffs() returned null.");
        } else {
            System.out.println("AppointmentListBean: Loaded " + appointmentList.size() + " staff members.");
        }
    }

    // Getter for the JSF page
    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    // Optional: Action method to refresh the list
    public void refreshList() {
        loadAppointments();
    }


}
