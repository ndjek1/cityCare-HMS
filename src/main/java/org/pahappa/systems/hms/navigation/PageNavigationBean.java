package org.pahappa.systems.hms.navigation;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import views.UserAccountBean;

import java.io.Serializable;

@Named
@ViewScoped
public class PageNavigationBean implements Serializable {

    @Inject
    private UserAccountBean userAccountBean;

    private String currentPage;

    @PostConstruct
    public void init() {
        if (userAccountBean.isPatient()) {
            currentPage = "/WEB-INF/includes/patient/patient_appointment_list.xhtml";
        } else if (userAccountBean.isDoctor()) {
            currentPage = "/WEB-INF/includes/doctor/manage_schedule.xhtml";
        } else if (userAccountBean.isAdministrator()) {
            currentPage = "/WEB-INF/includes/admin/dashboard_content.xhtml";
        } else if (userAccountBean.isReceptionist()) {
            currentPage = "/WEB-INF/includes/receptionist/patient_registration.xhtml";
        }
    }

    // --- Navigation Methods ---
    public String navigateToDashboard() {
        this.currentPage = "/WEB-INF/includes/admin/dashboard_content.xhtml";
        return null;
    }

    public String navigateAddNewStaff() {
        this.currentPage = "/WEB-INF/includes/admin/staff_registration.xhtml";
        return null;
    }

    public String navigateAddNewPatient() {
        this.currentPage = "/WEB-INF/includes/receptionist/patient_registration.xhtml";
        return null;
    }

    public String navigateToViewStaff() {
        this.currentPage = "/WEB-INF/includes/admin/staff_list.xhtml";
        return null;
    }

    public String navigateToViewPatients() {
        this.currentPage = "/WEB-INF/includes/admin/patient_list.xhtml";
        return null;
    }

    public String navigateToBookAppointment() {
        this.currentPage = "/WEB-INF/includes/patient/book_appointment.xhtml";
        return null;
    }

    public String navigateToManageDoctorSchedule() {
        this.currentPage = "/WEB-INF/includes/doctor/manage_schedule.xhtml";
        return null;
    }

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

    public String navigateToCompletedAppointments() {
        this.currentPage = "/WEB-INF/includes/receptionist/completed_appointment_list.xhtml";
        return null;
    }

    public String navigateToRescheduleAppointment() {
        this.currentPage = "/WEB-INF/includes/shared/reschedule_appointment.xhtml";
        return null;
    }

    public String navigateToProfile() {
        if (userAccountBean.isPatient()) {
            return navigateToMyProfile();
        } else {
            return navigateToStaffProfile();
        }
    }

    public String navigateToMyProfile() {
        this.currentPage = "/WEB-INF/includes/patient/patient_profile.xhtml";
        return null;
    }

    public String navigateToStaffProfile() {
        this.currentPage = "/WEB-INF/includes/shared/staff_profile.xhtml";
        return null;
    }

    public String navigateToManageServiceCatalog() {
        this.currentPage = "/WEB-INF/includes/admin/manage_service_catalog.xhtml";
        return null;
    }

    // --- Getter & Setter ---
    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}
