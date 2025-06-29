package backingbeans;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped; // Good scope for displaying lists
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Staff; // Your Staff model
import services.HospitalService; // Your service

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("staffListBean")
@ViewScoped // ViewScoped is good for pages that display data; data is fetched once per view.
public class StaffListBean implements Serializable {

    @Inject
    private HospitalService hospitalService;

    private List<Staff> staffList;

    @PostConstruct
    public void init() {
        loadStaff();
    }

    private void loadStaff() {
        if (hospitalService != null) {
            staffList = hospitalService.getAllStaffs();
            if (staffList == null) {
                staffList = new ArrayList<>(); // Avoid NullPointerException in DataTable
                System.err.println("StaffListBean: hospitalService.getAllStaffs() returned null.");
            } else {
                System.out.println("StaffListBean: Loaded " + staffList.size() + " staff members.");
            }
        } else {
            staffList = new ArrayList<>();
            System.err.println("StaffListBean: HospitalService not injected, cannot load staff.");
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
        if (hospitalService != null && staffToDelete != null) {
            System.out.println("Attempting to delete staff: " + staffToDelete.getName());
            hospitalService.deleteStaff(staffToDelete.getStaffId()); // Assuming deleteStaff takes ID or object
            loadStaff(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }
}