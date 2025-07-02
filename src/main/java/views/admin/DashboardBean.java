package views.admin;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.constants.PaymentStatus;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.services.DashboardService;
import org.pahappa.systems.hms.services.impl.DashboardServiceImpl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    /* simple stats */
    private long patientCount;
    private long staffCount;
    private long todaysAppointmentCount;
    private long openBillsCount;
    private long paidBillsCount;
    private double unpaidAmount;
    private double paidAmount;

    private final DashboardService dashboardService = new DashboardServiceImpl();

    @PostConstruct
    public void init() {
        loadDashboardStats();

    }

    private void loadDashboardStats() {
        patientCount            = dashboardService.getPatientCount();
        staffCount              = dashboardService.getStaffCount();
        todaysAppointmentCount  = dashboardService.getTodaysScheduledAppointmentCount();
        openBillsCount          = dashboardService.getOpenBillsCount();
        List<Bill> allBills = dashboardService.getAllBills();
        for (Bill bill : allBills) {
            if(bill.getPaymentStatus() == PaymentStatus.PAID){
                paidAmount +=bill.getTotalAmount();
                paidBillsCount++;
            }else if(bill.getPaymentStatus() == PaymentStatus.UNPAID){
                unpaidAmount +=bill.getTotalAmount();
            }
        }

    }

    /* ---------- getters ---------- */
    public long getPatientCount()            { return patientCount; }
    public long getStaffCount()              { return staffCount; }
    public long getTodaysAppointmentCount()  { return todaysAppointmentCount; }
    public long getOpenBillsCount()          { return openBillsCount; }
   public long getPaidBillsCount()          { return paidBillsCount; }
    public double getUnpaidAmount()          { return unpaidAmount; }
    public double getPaidAmount()              { return paidAmount; }
}
