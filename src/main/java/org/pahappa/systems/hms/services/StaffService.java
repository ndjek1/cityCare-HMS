package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.constants.HospitalDepartment;
import org.pahappa.systems.hms.constants.UserRole;
import org.pahappa.systems.hms.models.Staff;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StaffService {
    Optional<Staff> registerStaff(String username, String password, UserRole role, String name, HospitalDepartment department, String address, String email, String phone, Date dob, String gender);
    Optional<Staff> findStaffById(Long staffId);
    List<Staff> getAllStaffs();
    boolean updateStaffRecord(Long staffId, String address, String phone, String email);

    void deleteStaff(Staff staff);

    List<LocalDateTime> getDoctorAvailableSlotsForDate(Long doctorId, LocalDate date);
    boolean addDoctorAvailability(Long doctorId, List<LocalDateTime> newSlots);
    boolean removeDoctorAvailability(Long doctorId, List<LocalDateTime> slotsToRemove);
}
