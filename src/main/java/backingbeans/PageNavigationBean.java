package backingbeans;

import jakarta.enterprise.context.SessionScoped; // Or ViewScoped depending on desired behavior
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped // SessionScoped is common for holding current page state across requests
public class PageNavigationBean implements Serializable {

    @Inject
    private UserAccountBean userAccountBean;
    private String currentPage = "/WEB-INF/includes/admin/dashboard_content.xhtml"; // Default content to show

    // --- Actions called by menu items ---
    public String navigateToDashboard() {
        this.currentPage = "/WEB-INF/includes/admin/dashboard_content.xhtml";
        return null; // Stay on the same JSF view, but AJAX will update mainContentPanel
    }

    public String navigateAddNewStaff() {
        this.currentPage = "/WEB-INF/includes/admin/staff_registration.xhtml";
        return null;
    }

    public String navigateAddNewPatient() {
         this.currentPage = "/WEB-INF/includes/receptionist/patient_registration.xhtml";
        return null;
    }
    // In PageNavigationBean.java
    public String navigateToViewStaff() {
        this.currentPage = "/WEB-INF/includes/admin/staff_list.xhtml";
        System.out.println("Navigating to: " + this.currentPage);
        return null; // For AJAX update
    }
    public String navigateToProfile() {
        if (userAccountBean.isPatient()) {
            return navigateToMyProfile();
        } else {
            return navigateToStaffProfile();
        }
    }

    // In PageNavigationBean.java
    public String navigateToViewPatients() {
        this.currentPage = "/WEB-INF/includes/admin/patient_list.xhtml";
        System.out.println("Navigating to: " + this.currentPage);
        return null; // For AJAX update
    }

    // In PageNavigationBean.java
    public String navigateToBookAppointment() {
        this.currentPage = "/WEB-INF/includes/patient/book_appointment.xhtml";
        System.out.println("Navigating to: " + this.currentPage);
        return null; // For AJAX update
    }

    // In PageNavigationBean.java
    public String navigateToManageDoctorSchedule() {
        this.currentPage = "/WEB-INF/includes/doctor/manage_schedule.xhtml";
        return null;
    }

    // In PageNavigationBean.java
    public String navigateToPatientAppointments() {
        this.currentPage = "/WEB-INF/includes/patient/patient_appointment_list.xhtml";
        return null;
    }

    public String navigateToDoctorAppointments() {
        this.currentPage = "/WEB-INF/includes/doctor/doctor_appointment_list.xhtml";
        return null;
    }
    public String navigateToUnpaidBills() {
        this.currentPage = "/WEB-INF/includes/receptionist/unpaid_bills.xhtml";
        return null;
    }


    // In PageNavigationBean.java
    public String navigateToCompletedAppointments() {
        this.currentPage = "/WEB-INF/includes/receptionist/completed_appointment_list.xhtml";
        return null;
    }

    // In PageNavigationBean.java
    public String navigateToRescheduleAppointment() {
        this.currentPage = "/WEB-INF/includes/shared/reschedule_appointment.xhtml";
        System.out.println("Navigating to Reschedule Appointment page: " + this.currentPage);
        return null; // For AJAX update of the main content panel
    }
    // In PageNavigationBean.java
    public String navigateToMyProfile() {
        this.currentPage = "/WEB-INF/includes/patient/patient_profile.xhtml";
        return null;
    }

    public String navigateToStaffProfile() {
        this.currentPage = "/WEB-INF/includes/shared/staff_profile.xhtml";
        return null;
    }

    // In PageNavigationBean.java
    public String navigateToManageServiceCatalog() {
        this.currentPage = "/WEB-INF/includes/admin/manage_service_catalog.xhtml";
        return null;
    }


    // --- Getter for the current page ---
    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}