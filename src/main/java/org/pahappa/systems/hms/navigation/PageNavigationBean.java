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
    private String selectedMenu;
    @PostConstruct
    public void init() {
        // Your existing init logic to set a default page and menu
        if (userAccountBean.isLoggedIn()) {
            if (userAccountBean.isAdministrator()) {
                navigateToDashboard();
            } else if (userAccountBean.isDoctor()) {
                navigateToDoctorDashboard();
            } else if (userAccountBean.isPatient()) {
                navigateToPatientAppointments();
            } else if (userAccountBean.isReceptionist()) {
                navigateToViewPatients();
            } else if (userAccountBean.isAccountant()) {
                navigateToDashboard();
            } else {
                // A sensible default if no specific role matches
                navigateToDashboard();
            }
        }
        // If not logged in, it will be handled by your checkLogin listener
    }

    // --- Navigation Methods ---
    public String navigateToDashboard() {
        this.selectedMenu = "dashboard";
        this.currentPage = "/WEB-INF/includes/admin/dashboard_content.xhtml";
        return null;
    }
    public String navigateToDoctorDashboard() {
        this.selectedMenu = "dashboard";
        this.currentPage = "/WEB-INF/includes/doctor/dashboard.xhtml";
        return null;
    }


    public String navigateToUnpaidPrescriptions() {
        this.selectedMenu =  "unpaidPrescriptions";
        this.currentPage = "/WEB-INF/includes/unpaid_prescriptions.xhtml";
        return null;
    }


    public String navigateToViewStaff() {
        this.selectedMenu = "staff_list";
        this.currentPage = "/WEB-INF/includes/admin/staff_list.xhtml";
        return null;
    }

    public String navigateToManageMedicationCatalog() {
        this.selectedMenu = "medication_catalog";
        this.currentPage = "/WEB-INF/includes/admin/manage_medication_catalog_content.xhtml";
        return null;
    }

    public String navigateToViewPatients() {
        this.selectedMenu = "patient_list";
        this.currentPage = "/WEB-INF/includes/admin/patient_list.xhtml";
        return null;
    }

    public String navigateToBookAppointment() {
        this.selectedMenu = "book_appointment";
        this.currentPage = "/WEB-INF/includes/patient/book_appointment.xhtml";
        return null;
    }

    public String navigateToManageDoctorSchedule() {
        this.selectedMenu = "manage_schedule";
        this.currentPage = "/WEB-INF/includes/doctor/manage_schedule.xhtml";
        return null;
    }

    public String navigateToPatientAppointments() {
        this.selectedMenu = "patient_appointments";
        this.currentPage = "/WEB-INF/includes/patient/patient_appointment_list.xhtml";
        return null;
    }

    public String navigateToMyPaymentHistory() {
        this.selectedMenu = "payment_history"; // For active menu highlighting
        this.currentPage = "/WEB-INF/includes/patient/my_payment_history_content.xhtml";
        return null;
    }

    public String navigateToDoctorAppointments() {
        this.selectedMenu = "doctor_appointment_list";
        this.currentPage = "/WEB-INF/includes/doctor/doctor_appointment_list.xhtml";
        return null;
    }

    public String navigateToUnpaidBills() {
        this.selectedMenu = "unpaid_bills";
        this.currentPage = "/WEB-INF/includes/receptionist/unpaid_bills.xhtml";
        return null;
    }

    public String navigateToCompletedAppointments() {
        this.selectedMenu ="completed_appointments";
        this.currentPage = "/WEB-INF/includes/receptionist/completed_appointment_list.xhtml";
        return null;
    }

    public String navigateToRescheduleAppointment() {
        this.selectedMenu = "reschedule_appointment";
        this.currentPage = "/WEB-INF/includes/shared/reschedule_appointment.xhtml";
        return null;
    }
    public String navigateToMyPrescriptions() {
        this.selectedMenu = "my_prescriptions";
        this.currentPage = "/WEB-INF/includes/patient/my_prescriptions.xhtml";
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
        this.selectedMenu = "profile";
        this.currentPage = "/WEB-INF/includes/patient/patient_profile.xhtml";
        return null;
    }

    public String navigateToStaffProfile() {
        this.selectedMenu = "profile";
        this.currentPage = "/WEB-INF/includes/shared/staff_profile.xhtml";
        return null;
    }

    public String navigateToManageServiceCatalog() {
        this.selectedMenu = "service_catalog";
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
    public String getSelectedMenu() {
        return selectedMenu;
    }

    public void setSelectedMenu(String selectedMenu) {
        this.selectedMenu = selectedMenu;
    }
}
