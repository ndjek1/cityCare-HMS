package services.impl;


import constants.UserRole;
import dao.PatientDao;
import dao.UserAccountDao;
import dao.impl.PatientDaoImpl;
import dao.impl.UserAccountDaoImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Patient;
import models.Staff;
import models.UserAccount;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import services.AuthService;
import services.PatientService;
import utils.PasswordUtil;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
}