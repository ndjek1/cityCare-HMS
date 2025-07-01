package views.admin;


import org.pahappa.systems.hms.constants.HospitalDepartment;
import org.pahappa.systems.hms.constants.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped; // Good scope for displaying lists
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Staff; // Your Staff model

import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("staffListBean")
@ViewScoped // ViewScoped is good for pages that display data; data is fetched once per view.
public class StaffListBean implements Serializable {


    private List<Staff> staffList;
    private List<HospitalDepartment>  availableDepartments;
    private List<UserRole>  availableRoles;
    private final StaffServiceImpl staffService;

    public List<UserRole> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(List<UserRole> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public StaffListBean() {
        this.staffService = new StaffServiceImpl();
    }
    @PostConstruct
    public void init() {
        loadStaff();
        this.availableDepartments = new ArrayList<>(List.of(HospitalDepartment.values()));
        this.availableRoles = new ArrayList<>(List.of(UserRole.values()));
    }

    private void loadStaff() {
        staffList = staffService.getAllStaffs();
        if (staffList == null) {
            staffList = new ArrayList<>(); // Avoid NullPointerException in DataTable
            System.err.println("StaffListBean: hospitalService.getAllStaffs() returned null.");
        } else {
            System.out.println("StaffListBean: Loaded " + staffList.size() + " staff members.");
        }
    }

    // Getter for the JSF page
    public List<Staff> getStaffList() {
        return staffList;
    }

    // Optional: Action method to refresh the list
    public void refreshList() {
        loadStaff();
    }

    // Optional: Action method for deleting a staff member (example)
    public void deleteStaff(Staff staffToDelete) {
        if (staffService != null && staffToDelete != null) {
            System.out.println("Attempting to delete staff: " + staffToDelete.getName());
           staffService.deleteStaff(staffToDelete); // Assuming deleteStaff takes ID or object
            loadStaff(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }

    public List<HospitalDepartment> getAvailableDepartments() {
        return availableDepartments;
    }

    public void setAvailableDepartments(List<HospitalDepartment> availableDepartments) {
        this.availableDepartments = availableDepartments;
    }

    public String getRoleStyleClass(String role) {
        if (role == null) {
            return "";
        }
        // Simple way to create a CSS-friendly class name, e.g., "Administrator" -> "role-Administrator"
        return "role-" + role.replaceAll("\\s+", "");
    }


    public String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        // ... (copy the same logic from Option 1's getInitials() method) ...
        StringBuilder initials = new StringBuilder();
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(parts[parts.length - 1].charAt(0));
        }
        return initials.toString().toUpperCase();
    }
}