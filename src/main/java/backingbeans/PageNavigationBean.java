package backingbeans;

import jakarta.enterprise.context.SessionScoped; // Or ViewScoped depending on desired behavior
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped // SessionScoped is common for holding current page state across requests
public class PageNavigationBean implements Serializable {

    private String currentPage = "/WEB-INF/includes/dashboard_content.xhtml"; // Default content to show

    // --- Actions called by menu items ---
    public String navigateToDashboard() {
        this.currentPage = "/WEB-INF/includes/dashboard_content.xhtml";
        return null; // Stay on the same JSF view, but AJAX will update mainContentPanel
    }

    public String navigateAddNewStaff() {
        this.currentPage = "/WEB-INF/includes/staffRegistration.xhtml";
        return null;
    }
    // In PageNavigationBean.java
    public String navigateToViewStaff() {
        this.currentPage = "/WEB-INF/includes/staff_list.xhtml";
        System.out.println("Navigating to: " + this.currentPage);
        return null; // For AJAX update
    }


    // --- Getter for the current page ---
    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}