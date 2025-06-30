package services.impl;

import constants.HospitalDepartment;
import constants.UserRole;
import dao.impl.StaffDaoImpl;
import dao.impl.UserAccountDaoImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Staff;
import models.UserAccount;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import services.StaffService;
import utils.HibernateUtil;
import utils.PasswordUtil;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class StaffServiceImpl implements StaffService {

    private final SessionFactory factory;
    private final StaffDaoImpl staffDao;
    private final UserAccountDaoImpl  userAccountDao;

    private final AuthServiceImpl authServiceImpl; // Inject other services as needed

    public StaffServiceImpl() {
        this.staffDao = new StaffDaoImpl();
        this.factory = HibernateUtil.getSessionFactory();
        this.authServiceImpl = new AuthServiceImpl();
        this.userAccountDao = new UserAccountDaoImpl();
    }


    @Override
    public Optional<Staff> registerStaff(String username, String password, UserRole role, String name, HospitalDepartment department, String address, String email, String phone, Date dob, String gender) {
        if (authServiceImpl.usernameExists(username)) {
            System.err.println("SERVICE: Username '" + username + "' already taken.");
            return Optional.empty();
        }

        try {

            Staff staff = new Staff(name, role, department, address, email, phone, dob, gender);
            staffDao.save(staff);

            String hashedPassword = PasswordUtil.hashPassword(password);
            UserAccount account = new UserAccount(username, hashedPassword, role, staff.getStaffId());
            userAccountDao.save(account);


            return Optional.of(staff);
        } catch (Exception e) {

            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Staff> findStaffById(Long staffId) {
        try (Session session = factory.openSession()) {
            return Optional.ofNullable(session.get(Staff.class, staffId));
        }
    }

    @Override
    public List<Staff> getAllStaffs() {
        try (Session session = factory.openSession()) {
            return session.createQuery("FROM Staff", Staff.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean updateStaffRecord(Long staffId, String address, String phone, String email) {
        Optional<Staff> staff = staffDao.findById(staffId);
        if (staff.isPresent()) {
            Staff staffToUpdate = staff.get();
            staffToUpdate.setAddress(address);
            staffToUpdate.setPhone(phone);
            staffToUpdate.setEmail(email);
            staffDao.update(staffToUpdate);
            return true;
        }
        return false;
    }

    @Override
    public boolean addDoctorAvailability(Long doctorId, List<LocalDateTime> newSlots) {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Staff doctor = session.get(Staff.class, doctorId);
            if (doctor != null && doctor.getRole() == UserRole.DOCTOR) {
                Hibernate.initialize(doctor.getAvailableSlots());
                newSlots.forEach(doctor::addAvailability);
                tx.commit();
                return true;
            }
            if (tx.isActive()) tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    // ... Implement other staff-related methods like updateStaffRecord, deleteStaff, removeDoctorAvailability etc. ...
    // They will follow the same pattern of opening a session/transaction and performing the logic.

    @Override
    public void deleteStaff(Staff staff) {
      staffDao.delete(staff);
    }

    public List<LocalDateTime> getDoctorAvailableSlotsForDate(Long doctorId, LocalDate date) {

        List<LocalDateTime> slotsOnDate = new ArrayList<>();

        if (doctorId == null || date == null) {
            System.err.println("SERVICE: Doctor ID or Date is null for getting available slots.");
            return slotsOnDate; // Return empty list
        }

        System.out.println("SERVICE: Fetching slots for Doctor ID: " + doctorId + " on Date: " + date);

        try {

            Optional<Staff> doctorOpt = staffDao.findById(doctorId);

            Staff doctor = doctorOpt.orElse(null);
            if (doctor != null) {


                if (doctor.getAvailableSlots() != null) {
                    for (LocalDateTime slotDateTime : doctor.getAvailableSlots()) {
                        if (slotDateTime.toLocalDate().equals(date)) {
                            slotsOnDate.add(slotDateTime);
                        }
                    }
                    // Sort them if needed
                    slotsOnDate.sort(LocalDateTime::compareTo);
                    System.out.println("SERVICE: Found " + slotsOnDate.size() + " slots for Dr. " + doctor.getName() + " on " + date);
                } else {
                    System.out.println("SERVICE: Dr. " + doctor.getName() + " has no available slots defined.");
                }
            } else {
                System.err.println("SERVICE: Doctor with ID " + doctorId + " not found.");
            }

        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Could not fetch available slots for doctor " + doctorId + " on " + date);
            e.printStackTrace();

        }
        return slotsOnDate;
    }

    // Method to REMOVE existing available slots for a doctor
    public boolean removeDoctorAvailability(Long doctorId, List<LocalDateTime> slotsToRemove) {
        if (doctorId == null || slotsToRemove == null || slotsToRemove.isEmpty()) {
            return false;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Optional<Staff> doctorOpt = staffDao.findById(doctorId);
            Staff doctor = doctorOpt.orElse(null);
            if (doctor != null) {
                Hibernate.initialize(doctor.getAvailableSlots());
                int removedCount = 0;
                for (LocalDateTime slot : slotsToRemove) {
                    if (doctor.removeAvailability(slot)) { // removeAvailability is on Staff entity
                        removedCount++;
                    }
                }
                if (removedCount > 0) {
                    session.merge(doctor);
                    System.out.println("SERVICE: Removed " + removedCount + " slots for Dr. ID " + doctorId);
                }
                tx.commit();
                return removedCount > 0;
            } else {
                if(tx.isActive()) tx.rollback();
                return false;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}