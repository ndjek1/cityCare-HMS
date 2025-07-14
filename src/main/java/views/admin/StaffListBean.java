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
import java.util.Comparator;
import java.util.List;

@Named("staffListBean")
@ViewScoped
public class StaffListBean implements Serializable {

    private List<Staff> staffList;
    private List<Staff> filteredStaffList;
    private List<HospitalDepartment> availableDepartments;
    private List<UserRole> availableRoles;
    private final StaffServiceImpl staffService;

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
            staffList = new ArrayList<>();
            System.err.println("StaffListBean: hospitalService.getAllStaffs() returned null.");
        } else {
            // Sort by registration date descending
            staffList.sort(Comparator.comparing(Staff::getRegistrationDate).reversed());
            System.out.println("StaffListBean: Loaded " + staffList.size() + " staff members.");
        }

        this.filteredStaffList = new ArrayList<>(this.staffList);
    }


    public List<Staff> getStaffList() {
        return staffList;
    }

    public List<Staff> getFilteredStaffList() {
        return filteredStaffList;
    }

    public void setFilteredStaffList(List<Staff> filteredStaffList) {
        this.filteredStaffList = filteredStaffList;
    }

    public List<HospitalDepartment> getAvailableDepartments() {
        return availableDepartments;
    }

    public void setAvailableDepartments(List<HospitalDepartment> availableDepartments) {
        this.availableDepartments = availableDepartments;
    }

    public List<UserRole> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(List<UserRole> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public void refreshList() {
        loadStaff();
    }

    public void deleteStaff(Staff staffToDelete) {
        if (staffService != null && staffToDelete != null) {
            System.out.println("Attempting to delete staff: " + staffToDelete.getName());
            staffService.deleteStaff(staffToDelete);
            loadStaff();
        }
    }

    public String getRoleStyleClass(String role) {
        if (role == null) return "";
        return "role-" + role.replaceAll("\\s+", "");
    }

    public String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
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

    // ✅ Global Filter Function
    public boolean globalFilterFunction(Object value, Object filter, java.util.Locale locale) {
        String filterText = (filter == null) ? "" : filter.toString().toLowerCase();
        if (filterText.isBlank()) return true;

        Staff staff = (Staff) value;

        return (staff.getName() != null && staff.getName().toLowerCase().contains(filterText)) ||
                (staff.getEmail() != null && staff.getEmail().toLowerCase().contains(filterText)) ||
                (staff.getRole() != null && staff.getRole().toString().toLowerCase().contains(filterText)) ||
                (staff.getDepartment() != null && staff.getDepartment().toString().toLowerCase().contains(filterText)) ||
                (staff.getPhone() != null && staff.getPhone().toLowerCase().contains(filterText));
    }
}
