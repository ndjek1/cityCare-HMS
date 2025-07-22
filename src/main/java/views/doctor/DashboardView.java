package views.doctor;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.services.AppointmentService;
import org.pahappa.systems.hms.services.PrescriptionService;
import org.pahappa.systems.hms.services.impl.AppointmentServiceImpl;
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;
import views.UserAccountBean;

import java.io.Serializable;

@Named("doctorDashboardBean")
@ViewScoped
public class DashboardView implements Serializable {

    private int completedAppointmentsCount;
    private long todayAppointmentsCount;
    private long totalPrescriptionsCount;
    @Inject
    private UserAccountBean  userAccountBean;

    private static AppointmentServiceImpl appointmentService;
    private static PrescriptionServiceImpl prescriptionService;

    public DashboardView() {
        appointmentService = new AppointmentServiceImpl();
        prescriptionService = new PrescriptionServiceImpl();
    }
    @PostConstruct
    public void init() {
        getAppointments();
    }

    public void getAppointments(){
        this.todayAppointmentsCount = appointmentService.getTodaysScheduledAppointmentCount();
        this.completedAppointmentsCount = appointmentService.getAppointmentsForDoctor(userAccountBean.getCurrentUser().getEntityId()).size();
        this.totalPrescriptionsCount = prescriptionService.findByDoctorId(userAccountBean.getCurrentUser().getEntityId());

    }

    public int getCompletedAppointmentsCount() {
        return completedAppointmentsCount;
    }

    public void setCompletedAppointmentsCount(int completedAppointmentsCount) {
        this.completedAppointmentsCount = completedAppointmentsCount;
    }

    public long getTodayAppointmentsCount() {
        return todayAppointmentsCount;
    }

    public void setTodayAppointmentsCount(long todayAppointmentsCount) {
        this.todayAppointmentsCount = todayAppointmentsCount;
    }

    public long getTotalPrescriptionsCount() {
        return totalPrescriptionsCount;
    }

    public void setTotalPrescriptionsCount(long totalPrescriptionsCount) {
        this.totalPrescriptionsCount = totalPrescriptionsCount;
    }
}
