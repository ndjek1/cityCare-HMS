package models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import constants.HospitalDepartment;
import constants.UserRole;

@Entity
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;
    private HospitalDepartment department;
    private UserRole role;
    private String name;
    private String address;
    private String email;
    private String phone;
    private String dob;
    private String gender;



    public Staff(String name, UserRole role, HospitalDepartment department, String address, String email, String phone, String dob, String gender) {
        this.name = name;
        this.role = role;
        this.department = department;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;

    }

    public Staff() {

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStaffId() { return staffId; }
    public HospitalDepartment getDepartment() { return department; }
    public void setDepartment(HospitalDepartment department) { this.department = department; }

    @Override
    public String toString() {
        String lineSeparator = System.lineSeparator();
        int labelWidth = 18; // Consistent label width
        StringBuilder sb = new StringBuilder();

        // Start with a clear header for Staff Information
        sb.append("--------------------------------------").append(lineSeparator);

        // Integrate superclass information more deliberately
        // Instead of just super.toString(), we can reformat it or extract parts if User has getters
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Staff Member", getName() != null ? getName() : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Staff ID (Hospital)", staffId != null ? staffId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Role", getRole() != null ? getRole().toString() : "N/A"));


        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Department",
                department != null ? department.getDisplayName() : "N/A")); // Assuming department enum has getDisplayName()

        sb.append("--------------------------------------").append(lineSeparator);

        return sb.toString();
    }

}
