package org.pahappa.systems.hms.services.impl;


import org.hibernate.query.Query;
import org.pahappa.systems.hms.constants.UserRole;
import org.pahappa.systems.hms.dao.PatientDao;
import org.pahappa.systems.hms.dao.UserAccountDao;
import org.pahappa.systems.hms.dao.impl.PatientDaoImpl;
import org.pahappa.systems.hms.dao.impl.UserAccountDaoImpl;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.models.UserAccount;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.systems.hms.services.AuthService;
import org.pahappa.systems.hms.services.PatientService;
import utils.HibernateUtil;
import utils.PasswordUtil;

import java.util.*;

public class PatientServiceImpl implements PatientService {

   private final PatientDao patientDao;
    private final UserAccountDao userAccountDao;
   private  final AuthService authService;
   public  PatientServiceImpl() {
       this.authService = new AuthServiceImpl();
       this.patientDao = new PatientDaoImpl();
       this.userAccountDao = new UserAccountDaoImpl();
   }

    @Override
    public Optional<Patient> registerPatient(String username, String password, String name, Date dob, String address, String phone, String email) {
        if (authService.usernameExists(username)) {
            System.err.println("SERVICE: Username '" + username + "' already taken.");
            return Optional.empty();
        }

        Session session = null;
        Transaction tx = null;
        try {
            Patient patient = new Patient(name, dob, address, phone, email);
            patientDao.save(patient);

            String hashedPassword = PasswordUtil.hashPassword(password);
            UserAccount account = new UserAccount(username, hashedPassword, UserRole.PATIENT, patient.getPatientId());
            userAccountDao.save(account);

            return Optional.of(patient);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public Optional<Patient> findPatientById(Long patientId) {
        return patientDao.findByIdWithHistory(patientId);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientDao.findAll();
    }

    @Override
    public boolean updatePatientRecord(Long patientId, String address, String phone, String email, String insuranceDetails) {
        Optional<Patient> patientOpt = patientDao.findById(patientId);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            patient.setAddress(address);
            patient.setPhoneNumber(phone);
            patient.setEmail(email);
            patient.setInsuranceDetails(insuranceDetails);
            patientDao.update(patient);
            return true;
        }
        return false;
    }

    @Override
    public void deletePatient(Patient patient) {
        patientDao.delete(patient);
    }

    public List<Payment> findPaymentsForPatient(Long patientId) {
        if (patientId == null) {
            System.err.println("SERVICE: Cannot find payments, patientId is null.");
            return Collections.emptyList(); // Return an empty list for invalid input
        }

        Session session = null;
        Transaction tx = null;
        List<Payment> allPayments = new ArrayList<>();

        System.out.println("SERVICE: Fetching all payments for Patient ID: " + patientId);

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction(); // Use a transaction for read operations for consistency

            // --- Query 1: Get payments associated with Bills ---
            Query<Payment> billPaymentsQuery = session.createQuery(
                    // Select Payment p where its associated bill's patient's ID matches
                    "SELECT p FROM Payment p WHERE p.billId.patient.patientId = :patientId",
                    Payment.class
            );
            billPaymentsQuery.setParameter("patientId", patientId);
            List<Payment> billPayments = billPaymentsQuery.getResultList();
            System.out.println("SERVICE: Found " + billPayments.size() + " payment(s) related to bills.");
            allPayments.addAll(billPayments);

            // --- Query 2: Get payments associated with Prescriptions ---
            Query<Payment> prescriptionPaymentsQuery = session.createQuery(
                    // Select Payment p where its associated prescription's patient's ID matches
                    "SELECT p FROM Payment p WHERE p.prescription.patient.patientId = :patientId",
                    Payment.class
            );
            prescriptionPaymentsQuery.setParameter("patientId", patientId);
            List<Payment> prescriptionPayments = prescriptionPaymentsQuery.getResultList();
            System.out.println("SERVICE: Found " + prescriptionPayments.size() + " payment(s) related to prescriptions.");
            allPayments.addAll(prescriptionPayments);

            // --- Sort the combined list by date, most recent first ---
//            allPayments.sort(Comparator.comparing(Payment::getPaymentDate).reversed());

            tx.commit();
            System.out.println("SERVICE: Total payments found for patient " + patientId + ": " + allPayments.size());

        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Could not fetch payments for Patient ID: " + patientId);
            e.printStackTrace();
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return Collections.emptyList(); // Return an empty list on error
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return allPayments;
    }
}