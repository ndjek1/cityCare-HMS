package services;



import constants.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import models.*;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static constants.UserRole.DOCTOR;
import static constants.UserRole.PATIENT;

@Named
@ApplicationScoped
public class HospitalService {

    private final  SessionFactory factory ;
    private static  UserAccount loggedInUser = null;
    // In-memory "database tables"
    private Map<String, UserAccount> userAccountsByUsername; // Key: username
    private Map<Long, Patient> patientsById;              // Key: patientId


    private List<Appointment> appointments; // Could also be a Map by appointmentId
    private List<Bill> bills;               // Could also be a Map by billId
    private List<Payment> payments;           // Could also be a Map by paymentId

    // To store the currently logged-in specific entity (Patient, Doctor, etc.)
    private Object loggedInEntityDetails = null;

    public HospitalService() {
        this.factory = HibernateUtil.getSessionFactory();
        System.out.println("HospitalService CDI bean CREATED.");
        this.userAccountsByUsername = new HashMap<>();
        this.patientsById = new HashMap<>();

        this.appointments = new ArrayList<>();
        this.bills = new ArrayList<>();
        this.payments = new ArrayList<>();
    }



    // --- Authentication and Session ---
    public Optional<UserAccount> login(String username, String password) {
        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        UserAccount account = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // If 'username' is not the @Id, you need a query:
            // Make sure UserAccount entity has a field 'username'
            Query<UserAccount> query = session.createQuery(
                    "FROM UserAccount ua WHERE ua.username = :username", UserAccount.class
            );
            query.setParameter("username", username);
            account = query.uniqueResultOptional().orElse(null);

            tx.commit(); // Commit after read

            if (account != null && decryptCaesar(account.getPassword(), 32).equals(password)) { // Password check should be secure (hashing)
                loadLoggedInEntityDetails(account); // This still uses in-memory for now
                loggedInUser = account;
                return Optional.of(account);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        logout(); // Clear details on failed login
        return Optional.empty();
    }

    // This method needs to be re-thought. If UserAccount is from DB,
    // entityId should allow fetching Patient/Doctor from DB too.
    private void loadLoggedInEntityDetails(UserAccount account) {
        // For now, let's assume entityId allows fetching the detailed object
        // This part needs to be updated to fetch from DB if Patient/Doctor are fully DB managed
        Long entityId = account.getEntityId(); // Assuming entityId is Long
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            switch (account.getRole()) {
                case PATIENT:
                    loggedInEntityDetails = session.find(Patient.class, entityId);
                    break;
                case DOCTOR:
                    loggedInEntityDetails = session.find(Staff.class, entityId);
                    break;
                // Add Administrator, Receptionist if they are entities with IDs
                default:
                    loggedInEntityDetails = null;
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            loggedInEntityDetails = null;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }


    public Object getLoggedInEntityDetails() {
        return loggedInEntityDetails;
    }

    public void logout() {
        loggedInEntityDetails = null;
    }

    // --- Registration ---

    public Optional<Patient> registerPatient(String username, String password, String name, String dob, String address, String phone, String email) {
        // 1. Check username uniqueness (ideally query the UserAccount table)
        if (userAccountsByUsername.containsKey(username)) {
            System.out.println("Error: Username '" + username + "' already taken.");
            return Optional.empty();
        }

        Patient patient = new Patient( name, dob, address, phone, email);
        UserAccount account = null; // Initialize account

        Session session = null;
        Transaction tx = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // 2. Persist the Patient entity first
            session.persist(patient);
            // After persist (and often after flush/commit for IDENTITY), patient.getPatientId() will be populated by Hibernate if it's DB-generated.
            // For some strategies, a session.flush() might be needed here to get the ID before commit,
            // but often commit is enough. Let's assume commit handles it for now.
            String encryptedPassword = encryptCaesar(password,32);
            // 3. NOW create the UserAccount with the generated Patient ID
            // Ensure patient.getPatientId() returns the correct type for UserAccount.entityId
            account = new UserAccount(username, encryptedPassword, patient.getRole(), patient.getPatientId());
            System.out.println("Patient ID passed to UserAccount: " + patient.getPatientId()); // Debug
            session.persist(account);

            tx.commit();

            // Optional: Update in-memory maps if you're still using them for some reason
            // patientsById.put(patient.getPatientId(), patient);
            // userAccountsByUsername.put(username, account);

            System.out.println(patient.getName() + " (Patient) registered successfully. Patient ID: " + patient.getPatientId() +
                    ", Account Entity ID: " + (account != null ? account.getEntityId() : "N/A"));
            return Optional.of(patient);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    System.err.println("Rollback failed: " + rbEx.getMessage());
                }
            }
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }



    public Optional<Staff> registerStaff(String username, String password, UserRole userRole, String name, HospitalDepartment department,String address, String email, String phone, String dob, String gender) {
        // 1. Check username uniqueness (ideally query the UserAccount table from DB)
        if (isUsernameTaken(username)) {
            System.out.println("Error: Username '" + username + "' already taken.");
            return Optional.empty();
        }

        Staff staff = new Staff( name, userRole, department,address, email, phone, dob, gender);
        UserAccount account = null; // Initialize account

        Session session = null;
        Transaction tx = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // 2. Persist the Staff entity first
            session.persist(staff);
            // After persist (and typically after flush/commit for IDENTITY strategy),
            // staff.getStaffId() will be populated by Hibernate if it's DB-generated.

            // 3. NOW create the UserAccount with the generated Staff ID
            // Ensure staff.getStaffId() returns the correct type for UserAccount.entityId (e.g., Long, String)
            String encryptedPassword = encryptCaesar(password,32);
            account = new UserAccount(username, encryptedPassword, staff.getRole(), staff.getStaffId());
            // Your debug line was for Doctor ID, let's make it generic or specific to Staff
            System.out.println("Staff ID passed to UserAccount: " + staff.getStaffId()); // Debug

            session.persist(account);

            tx.commit();

            // Optional: Update in-memory maps if you are still using them for some transitional phase
            // staffById_memory.put(staff.getStaffId(), staff); // Assuming you have a map for staff
            // userAccountsByUsername_memory.put(username, account);

            System.out.println(staff.getName() + " (" + staff.getRole() + ") registered successfully. Staff ID: " + staff.getStaffId() +
                    ", Account's linked Entity ID: " + (account != null ? account.getEntityId() : "N/A"));
            return Optional.of(staff);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    System.err.println("Rollback failed for staff registration: " + rbEx.getMessage());
                }
            }
            e.printStackTrace(); // Log or handle more gracefully
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }


    public Optional<Patient> findPatientById(Long patientId) {
        Transaction tx = null; // Transaction still needs explicit management

        // Try-with-resources for the Session
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();

            Patient foundPatient = session.find(Patient.class, patientId);

            tx.commit();
            if (foundPatient != null) {
                Hibernate.initialize(foundPatient.getMedicalHistory());
            }
            return Optional.ofNullable(foundPatient);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    System.err.println("Error during transaction rollback: " + rbEx.getMessage());
                }
            }
            e.printStackTrace();
            return Optional.empty();
        }
        // Session is automatically closed here by try-with-resources if it was successfully opened
    }

    // In HospitalService.java or wherever findDoctorById is
    public Optional<Staff> findDoctorByIdWithSlots(Long doctorId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Staff doctor = session.find(Staff.class, doctorId);
            if (doctor != null) {
                // Explicitly initialize the collection while the session is open
                Hibernate.initialize(doctor.getAvailableSlots());
            }
            tx.commit();
            return Optional.ofNullable(doctor);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
    // ... finders for other entities if needed by ID directly

    public List<Patient> getAllPatients() {
        Session session = null;
        Transaction tx = null;
        List<Patient> patients ;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Query<Patient> query = session.createQuery(
                    "SELECT p FROM Patient p", Patient.class
            );

            patients = query.getResultList();
            tx.commit();
            for (Patient patient : patients) {
                Hibernate.initialize(patient.getMedicalHistory());
            }
            return patients;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) session.close();
        }
        return null;
    }

    public List<Staff> getAllStaffs() {
        Session session = null;
        Transaction tx = null;
        List<Staff> staffs;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Query<Staff> query = session.createQuery(
                    "SELECT st FROM Staff st ", Staff.class
            );

            staffs = query.getResultList();
            tx.commit();

            return staffs;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) session.close();
        }
        return null;

    }

    public List<UserAccount> getAllUserAccounts() {
        Session session = null;
        Transaction tx = null;
        List<UserAccount> userAccounts = new ArrayList<>();
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Query<UserAccount> query = session.createQuery(
                    "SELECT ac FROM UserAccount ac", UserAccount.class
            );

            userAccounts = query.getResultList();
            tx.commit();

            return userAccounts;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) session.close();
        }
        return null;
    }


    // --- Patient Specific ---
    public boolean updatePatientRecord(Long patientId, String address, String phone, String email, String insurance) {

        Session session = null;
        Transaction tx = null;
        Patient patient;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            patient = findPatientById(patientId).get();
            if (address != null && !address.isEmpty()) patient.setAddress(address);
            if (phone != null && !phone.isEmpty()) patient.setPhoneNumber(phone);
            if (email != null && !email.isEmpty()) patient.setEmail(email);
            if (insurance != null && !insurance.isEmpty()) patient.setInsuranceDetails(insurance);
            session.merge(patient);
            tx.commit();
            System.out.println("Patient record updated for " + patient.getName());
            return true;
        }catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) session.close();
        }
        return false;
    }


    public boolean updateLoginCredentials(String newUsername, String newPassword) {


        Session session = null;
        Transaction tx = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // Find the UserAccount based on the entityId
            // This query assumes UserAccount has an 'entityId' field that matches the ID from Patient/Doctor etc.
            // And also that entityId + role is unique, or just entityId is unique enough if IDs are globally unique across entity types.
            // A more robust query might be specific if entity IDs are not globally unique:
            // "FROM UserAccount ua WHERE ua.entityId = :entityId AND ua.role = :role"




            boolean changed = false;
            // Update username if provided and different
            if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(loggedInUser.getUsername())) {
                // Optional: Check if the new username is already taken by another account
                Query<Long> checkUserQuery = session.createQuery(
                        "SELECT count(ua) FROM UserAccount ua WHERE ua.username = :newUsername AND ua.accountId != :currentAccountId", Long.class);
                checkUserQuery.setParameter("newUsername", newUsername.trim());
                checkUserQuery.setParameter("currentAccountId", loggedInUser.getAccountId()); // Assuming UserAccount has an accountId PK
                if (checkUserQuery.uniqueResult() > 0) {
                    System.err.println("Error: New username '" + newUsername.trim() + "' is already taken.");
                    if (tx.isActive()) tx.rollback();
                    return false;
                }
                loggedInUser.setUsername(newUsername.trim());
                changed = true;
                System.out.println("Username updated for entity ID: " + loggedInUser.getEntityId());
            }

            // Update password if provided
            // IMPORTANT: newPassword should be hashed BEFORE being passed to this method or hashed here.
            // Storing plain text passwords is a major security risk.
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Example: Assume newPassword is already hashed, or you have a hashing utility
                String encryptedPassword = encryptCaesar(newPassword,32);
                // userAccountToUpdate.setPassword(hashedPassword);
                loggedInUser.setPassword(encryptedPassword); // Storing as is for now, but HASH IT!
                changed = true;
                System.out.println("Password updated for entity ID: " + loggedInUser.getEntityId());
            }

            if (changed) {
                session.update(loggedInUser); // Or session.update(userAccountToUpdate) if it was just loaded
                tx.commit();
                System.out.println("Login credentials updated successfully for entity ID: " + loggedInUser.getEntityId());
                return true;
            } else {
                System.out.println("No changes detected for username or password for entity ID: " + loggedInUser.getEntityId());
                if (tx.isActive()) tx.commit(); // Commit even if no changes, to close transaction properly
                return true; // Successfully processed, even if no DB change
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rbEx) {
                    System.err.println("Rollback failed: " + rbEx.getMessage());
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void addMedicalHistory(Long patientId, String entry) {
        findPatientById(patientId).ifPresent(p -> {
            p.addMedicalHistory(entry);
            System.out.println("Medical history added for " + p.getName());
        });
    }

    // --- Doctor Specific ---
    public List<Staff> findAvailableDoctors(String specialty, LocalDateTime dateTime) {

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        List<Staff> doctors = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // Assuming UserAccount has 'username' as its @Id or a unique constrained column
            // If 'username' is the @Id:
            // account = session.find(UserAccount.class, username);

            // If 'username' is not the @Id, you need a query:
            // Make sure UserAccount entity has a field 'username'
            Query<Staff> query = session.createQuery(
                    "FROM Staff d WHERE d.department = :department", Staff.class
            );
            query.setParameter("department", specialty);
            doctors = query.getResultList();

            tx.commit(); // Commit after read

            return doctors;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }


    public void deleteStaff(Long staffId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Staff staff = session.get(Staff.class, staffId);
            if (staff != null) {
                session.remove(staff);
                System.out.println("Deleted " + staff.getName());
            }
        tx.commit();
        }catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    // Then in bookAppointment:
    public Optional<Appointment> bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason) {
        Optional<Patient> patientOpt = findPatientById(patientId);
        Optional<Staff> doctorOpt = findDoctorByIdWithSlots(doctorId); // Use the new method

        if (!patientOpt.isPresent()) { /* ... */ return Optional.empty(); }
        if (!doctorOpt.isPresent()) { /* ... */ return Optional.empty(); }

        Staff doctor = doctorOpt.get(); // Now 'doctor' has its 'availableSlots' loaded

        if (doctor.getAvailableSlots().contains(dateTime)) { // This should now work
            // ... rest of your booking logic ...
            // Remember to handle saving changes to availableSlots within a new transaction
            // if you modify it (e.g., doctor.removeAvailability)
            Session bookingSession = null;
            Transaction bookingTx = null;
            try {
                Appointment appointment = new Appointment(patientOpt.get(), doctorOpt.get(), dateTime, reason);
                bookingSession = factory.openSession();
                bookingTx = bookingSession.beginTransaction();

                bookingSession.persist(appointment);

                boolean slotRemoved = doctor.getAvailableSlots().remove(dateTime); // Modify the already loaded list
                if(slotRemoved) {
                    Staff managedDoctor = bookingSession.merge(doctor); // Re-attach and merge changes
                    // managedDoctor.getAvailableSlots().remove(dateTime); // Or modify after merge
                }

                bookingTx.commit();
                System.out.println("Appointment (ID: " + appointment.getAppointmentId() + ") booked...");
                return Optional.of(appointment);
            } catch (Exception e) {
                if (bookingTx != null && bookingTx.isActive()) bookingTx.rollback();
                e.printStackTrace();
                return Optional.empty();
            } finally {
                if (bookingSession != null && bookingSession.isOpen()) bookingSession.close();
            }
        } else {
            System.out.println("Error: Dr. " + doctor.getName() + " is not available...");
            return Optional.empty();
        }
    }

    public boolean rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction


        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Optional<Appointment> appOpt = findAppointmentById(appointmentId);
            if (appOpt.isPresent()) {
                Appointment appointment = appOpt.get();
                Optional<Staff> doctorOpt = findDoctorByIdWithSlots(appointment.getDoctor().getStaffId());
                if(doctorOpt.isPresent()) {
                    Staff doctor = doctorOpt.get();
                    // Make old slot available again
                    doctor.addAvailability(appointment.getDateTime());
                    // Check if new slot is available and book it
                    if (doctor.getAvailableSlots().contains(newDateTime) && doctor.removeAvailability(newDateTime)) {
                        appointment.setDateTime(newDateTime);
                        appointment.setStatus(AppointmentStatus.SCHEDULED);
                        // DB: UPDATE Appointments SET dateTime = newDateTime WHERE appointmentId = ?
                        session.update(appointment);
                        session.update(doctor);
                        tx.commit(); // Commit after read
                        // DB: Manage DoctorAvailability table updates
                        System.out.println("Appointment " + appointmentId + " rescheduled to " + newDateTime);
                        return true;
                    } else {
                        System.out.println("Doctor not available at new time. Reschedule failed.");
                        doctor.removeAvailability(appointment.getDateTime()); // Re-book original slot if new one failed
                        session.update(doctor);
                        return false;
                    }
                }
            }



        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return false;
    }

    public boolean cancelAppointment(Long appointmentId) {
        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

                Appointment appointment = session.find(Appointment.class, appointmentId);
                if(appointment != null) {
                    appointment.setStatus(AppointmentStatus.CANCELLED);
                    System.out.println("Appointment " + appointmentId + "has been  cancelled.");
                }
            session.update(appointment);
            tx.commit(); // Commit after read


        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        System.out.println("Appointment not found. Cancellation failed.");
        return false;
    }

    public Optional<Appointment> findAppointmentById(Long appointmentId) {

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Appointment appointment = session.find(Appointment.class, appointmentId);

            tx.commit(); // Commit after read

            return Optional.ofNullable(appointment);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }

    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        List<Appointment> appointmentList = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Staff doctor = session.find(Staff.class, doctorId);
            if (doctor != null) {
                // Explicitly initialize the collection while the session is open
                Hibernate.initialize(doctor.getAvailableSlots());

            }


            // Assuming UserAccount has 'username' as its @Id or a unique constrained column
            // If 'username' is the @Id:
            // account = session.find(UserAccount.class, username);

            // If 'username' is not the @Id, you need a query:
            // Make sure UserAccount entity has a field 'username'
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE a.doctor.staffId = :doctorId", Appointment.class
            );
            query.setParameter("doctorId", doctorId);
            appointmentList = query.getResultList();

            tx.commit(); // Commit after read

            for (Appointment appointment : appointmentList) {
                if (appointment != null) {
                    Hibernate.initialize(appointment.getPatient());
                    Hibernate.initialize(appointment.getPatient().getMedicalHistory());
                    Hibernate.initialize(appointment.getDoctor());
                }
            }
            return appointmentList;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }

    public List<Appointment> getAppointmentsForPatient(Long patientId) {

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        List<Appointment> appointmentList = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // Assuming UserAccount has 'username' as its @Id or a unique constrained column
            // If 'username' is the @Id:
            // account = session.find(UserAccount.class, username);

            // If 'username' is not the @Id, you need a query:
            // Make sure UserAccount entity has a field 'username'
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE a.patient.patientId = :patientId", Appointment.class
            );
            query.setParameter("patientId", patientId);
            appointmentList = query.getResultList();

            tx.commit(); // Commit after read
            for (Appointment appointment : appointmentList) {
                if (appointment != null) {
                    Hibernate.initialize(appointment.getPatient());
                    Hibernate.initialize(appointment.getDoctor());
                }
            }
            return appointmentList;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }

    // In HospitalService.java
    public void recordDiagnosis(Long appointmentId, String diagnosisNotes) { // Assuming appointmentId is Long
        Session session = null;
        Transaction tx = null;

        System.out.println("SERVICE DEBUG: recordDiagnosis called with AppID: " + appointmentId + ", Notes: '" + diagnosisNotes + "'"); // DEBUG

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // 1. Find the Appointment
            Appointment appointment = session.find(Appointment.class, appointmentId);

            if (appointment == null) {
                System.err.println("SERVICE ERROR: Appointment with ID " + appointmentId + " not found in database.");
                // if (tx.isActive()) tx.rollback(); // Optional: rollback if entity not found
                // session.close(); // Close session before returning
                return; // <<<< POTENTIAL EARLY EXIT 1
            }

            // 2. Check if the appointment can have a diagnosis recorded
            // (e.g., is it in SCHEDULED state? This check is also in UI, but good to have in service too)
            if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
                System.err.println("SERVICE ERROR: Appointment " + appointmentId + " is not in SCHEDULED state. Current status: " + appointment.getStatus());
                // if (tx.isActive()) tx.rollback();
                // session.close();
                return; // <<<< POTENTIAL EARLY EXIT 2
            }

            // 3. Update the Appointment
            appointment.setDiagnosisNotes(diagnosisNotes);
            appointment.setStatus(AppointmentStatus.COMPLETED); // Mark as completed
            // session.merge(appointment); // Merge is often better for entities that might have been loaded/modified
            // For an entity just found in this session, direct modification is usually fine before commit.
            // However, if there's any chance 'appointment' was fetched and held onto, merge is safer.
            // For simplicity, direct modification and commit often works if it's freshly found.

            System.out.println("SERVICE DEBUG: Appointment " + appointmentId + " updated in memory. Notes: '" + appointment.getDiagnosisNotes() + "', Status: " + appointment.getStatus());

            // 4. Update Patient's Medical History (if applicable)
            Patient patient = session.find(Patient.class, appointment.getPatient().getPatientId()); // Assuming getPatientIdFk()
            if (patient != null) {
                Staff doctor = session.find(Staff.class, appointment.getDoctor().getStaffId()); // Assuming getDoctorIdFk()
                String doctorName = (doctor != null) ? doctor.getName() : "Unknown Doctor";

                String historyEntry = "Date: " + appointment.getDateTime().toLocalDate() +
                        ", Doctor: " + doctorName +
                        ", Diagnosis: " + diagnosisNotes;
                patient.addMedicalHistory(historyEntry); // This modifies the Patient object
                // session.merge(patient); // If Patient has an @ElementCollection for medicalHistory,
                // changes to it on a managed entity will be persisted.
                System.out.println("SERVICE DEBUG: Medical history added for Patient ID: " + patient.getPatientId());
            } else {
                System.err.println("SERVICE WARNING: Patient with ID " + appointment.getPatient().getPatientId() + " not found to update medical history.");
            }

            tx.commit(); // <<<< This is where changes are actually written to the DB
            System.out.println("SERVICE INFO: Diagnosis recorded and committed for Appointment ID: " + appointmentId);

        } catch (Exception e) {
            System.err.println("SERVICE EXCEPTION in recordDiagnosis for AppID: " + appointmentId); // DEBUG
            e.printStackTrace(); // Log the full exception
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                    System.err.println("SERVICE INFO: Transaction rolled back for AppID: " + appointmentId);
                } catch (Exception rbEx) {
                    System.err.println("SERVICE ERROR: Could not rollback transaction: " + rbEx.getMessage());
                }
            }
            // Depending on your design, you might re-throw the exception or a custom one
            // throw new RuntimeException("Failed to record diagnosis", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    // --- Billing and Payment (Largely the same, uses IDs) ---
    public Optional<Bill> generateBill(Long appointmentId, List<ServiceItem> serviceItems) {
        Optional<Appointment> appOpt = findAppointmentById(appointmentId);

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        List<Appointment> appointmentList = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            if (!appOpt.isPresent()) {
                System.out.println("Cannot generate bill: Appointment " + appointmentId + " not found.");
                return Optional.empty();
            }
            Appointment appointment = appOpt.get();
            if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
                System.out.println("Bill can only be generated for COMPLETED appointments. Appt status: " + appointment.getStatus());
                return Optional.empty();
            }

            Bill bill = new Bill(appointment.getPatient(), appointmentId);
            serviceItems.forEach(item -> bill.addService(item.getName(), item.getCost()));
            bill.addService("Doctor Consultation Fee", 100.00); // Example base fee

            session.persist(bill);
            tx.commit(); // Commit after read
            bills.add(bill); // DB: INSERT INTO Bills and BillItems
            System.out.println("Bill generated: " + bill.getBillId() + " for patient " + appointment.getPatient().getName());
            return Optional.ofNullable(bill);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        if (!appOpt.isPresent()) {
            System.out.println("Cannot generate bill: Appointment " + appointmentId + " not found.");
            return Optional.empty();
        }
        Appointment appointment = appOpt.get();
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            System.out.println("Bill can only be generated for COMPLETED appointments. Appt status: " + appointment.getStatus());
            return Optional.empty();
        }

        Bill bill = new Bill(appointment.getPatient(), appointmentId);
        serviceItems.forEach(item -> bill.addService(item.getName(), item.getCost()));
        bill.addService("Doctor Consultation Fee", 100.00); // Example base fee

        bills.add(bill); // DB: INSERT INTO Bills and BillItems
        System.out.println("Bill generated: " + bill.getBillId() + " for patient " + appointment.getPatient().getName());
        return Optional.of(bill);
    }

    public Optional<Bill> findBillById(Long billId) {

        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Bill bill = session.get(Bill.class, billId);
            tx.commit();

            if (bill != null) {
                Hibernate.initialize(bill.getServices());
                return Optional.of(bill);
            }else {
                System.out.println("Cannot find bill with ID: " + billId);
            }
        }catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return Optional.empty();
    }

    public Optional<Payment> processPayment(Long billId, double amountPaid, PaymentMethod method) {
        Optional<Bill> billOpt ;
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            billOpt = findBillById(billId);

            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();
                if (bill.getPaymentStatus() == PaymentStatus.PAID) {
                    System.out.println("Bill " + billId + " is already paid.");
                    return Optional.empty();
                }
                Payment payment = new Payment(bill, amountPaid, method);
                session.persist(payment); // DB: INSERT INTO Payments
                // Simplified: assumes full payment if amountPaid >= totalAmount
                if (amountPaid >= bill.getTotalAmount()) {
                    bill.setPaymentStatus(PaymentStatus.PAID); // DB: UPDATE Bills
                } else if (amountPaid > 0) {
                    bill.setPaymentStatus(PaymentStatus.PARTIALLY_PAID); // DB: UPDATE Bills
                }
                session.merge(bill);
                tx.commit();
                System.out.println("Payment of UGX " + amountPaid + " processed for bill " + billId);
                return Optional.of(payment);
            }
        }catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        System.out.println("Bill not found. Payment failed.");
        return Optional.empty();
    }

    public List<Bill> getUnpaidBillsForPatient(Long patientId) {
        return bills.stream()
                .filter(b -> b.getPatientId().equals(patientId) && b.getPaymentStatus() != PaymentStatus.PAID)
                .collect(Collectors.toList());
    }

    // Make your data seeding method public
    public boolean seedInitialData() {
        System.out.println("HospitalService: seedInitialData() called.");
        // Implement your logic to check if admin exists and create if not,
        // using proper Hibernate session and transaction management.
        // Example structure:
        Session session = null;
        Transaction tx = null;
        try {
            session = this.factory.openSession();
            tx = session.beginTransaction();

            // Check if admin user "admin" already exists
            Query<Long> query = session.createQuery("SELECT count(u) FROM UserAccount u WHERE u.username = :username", Long.class);
            query.setParameter("username", "doc.who");
            Long count = query.uniqueResult();

            if (count == 0) {
                // Create Staff for Admin
                Staff adminStaff = new Staff("Strange ", DOCTOR, HospitalDepartment.NEUROLOGY, "Kikoni", "time@citycaire.com", "0706080193", "04-07-2025", "Male");
                session.persist(adminStaff); // Persist staff first to get ID

                // Create UserAccount for Admin
                String encryptedPassword = encryptCaesar("time", 32); // Remember to use strong hashing
                UserAccount adminUserAccount = new UserAccount("doc.who", encryptedPassword, UserRole.ADMINISTRATOR, adminStaff.getStaffId());
                session.persist(adminUserAccount);
                System.out.println("Default admin user created.");
            } else {
                System.out.println("Default admin user already exists.");
            }

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error during seedInitialData: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return false;
    }

    private  boolean isUsernameTaken(String username) {
        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        UserAccount account;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // Assuming UserAccount has 'username' as its @Id or a unique constrained column
            // If 'username' is the @Id:
            // account = session.find(UserAccount.class, username);

            // If 'username' is not the @Id, you need a query:
            // Make sure UserAccount entity has a field 'username'
            Query<UserAccount> query = session.createQuery(
                    "FROM UserAccount ua WHERE ua.username = :username", UserAccount.class
            );
            query.setParameter("username", username);
            account = query.uniqueResultOptional().orElse(null);

            tx.commit(); // Commit after read

            if (account != null) { // Should return null, if not null means username taken
                return true;
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Log error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return false;
    }



    public static String encryptCaesar(String text, int shift) {
        if (text == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        // Normalize shift to be within the range of the alphabet (0-25)
        // A positive shift moves forward (A -> B), negative moves backward (B -> A)
        int effectiveShift = shift % 26; // Modulo 26 for alphabet size

        for (char character : text.toCharArray()) {
            if (Character.isUpperCase(character)) {
                // (char - 'A' + shift) % 26 gives 0-25. Add 'A' back.
                // Handle negative shift correctly with modulo
                char ch = (char) ('A' + (character - 'A' + effectiveShift + 26) % 26);
                result.append(ch);
            } else if (Character.isLowerCase(character)) {
                char ch = (char) ('a' + (character - 'a' + effectiveShift + 26) % 26);
                result.append(ch);
            } else {
                result.append(character); // Append non-alphabetic characters as is
            }
        }
        return result.toString();
    }

    public static String decryptCaesar(String cipherText, int shift) {
        if (cipherText == null) {
            return null;
        }
        // Decryption is just encryption with the opposite shift
        return encryptCaesar(cipherText, -shift);
    }



}
