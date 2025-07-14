package org.pahappa.systems.hms.services.impl;

import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.dao.impl.AppointmentDaoImpl;
import org.pahappa.systems.hms.dao.impl.PatientDaoImpl;
import org.pahappa.systems.hms.dao.impl.StaffDaoImpl;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Staff;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class AppointmentServiceImpl {

    private final AppointmentDaoImpl appointmentDao;
    private final PatientDaoImpl patientDao;
    private final StaffDaoImpl staffDao;

    public AppointmentServiceImpl() {
        this.appointmentDao = new AppointmentDaoImpl();
        patientDao = new PatientDaoImpl();
        staffDao = new StaffDaoImpl();

    }

    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return appointmentDao.getAppointmentsForDoctor(doctorId);
    }

    // ... Implement other Appointment methods like getAppointmentsForPatient, findById, etc. ...

    public Optional<Appointment> bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason) {
        try {


            Patient patient = patientDao.findById(patientId).orElse(null);
            Staff doctor = staffDao.findById(doctorId).orElse(null);

            if (patient == null || doctor == null) {
                return Optional.empty();
            }

            Hibernate.initialize(doctor.getAvailableSlots());
            if (doctor.removeAvailability(dateTime)) { // Attempt to consume the slot
                Appointment appointment = new Appointment(patient, doctor, dateTime, reason);
                appointmentDao.save(appointment);
                return Optional.of(appointment);
            } else {
                // Slot was not available
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean recordDiagnosis(Long appointmentId, String diagnosisNotes) {
        return appointmentDao.recordDiagnosis(appointmentId, diagnosisNotes);
    }





    public boolean rescheduleAppointment(Long appointmentId, Long newDoctorId, LocalDateTime newDateTime) {
        return appointmentDao.rescheduleAppointment(appointmentId, newDoctorId, newDateTime);
    }



    public boolean cancelAppointment(Long appointmentId) {
        return appointmentDao.cancelAppointment(appointmentId);
    }


    public List<Appointment> findAllAppointments() {

        try {
            System.err.println("Fetching completed appointments.");
            return appointmentDao.findAll();
        } catch (Exception e) {

            e.printStackTrace(); // Log error
        }
        return null;
    }


    public Optional<Appointment> findAppointmentById(Long appointmentId) {
        return (appointmentDao.findAppointmentById(appointmentId));
    }



    public List<Appointment> getAppointmentsForPatient(Long patientId) {
       return appointmentDao.getAppointmentsForPatient(patientId);
    }

    public long getTodaysScheduledAppointmentCount() {
        // Here we can use the specific DAO method
        return appointmentDao.findByDateAndStatus(LocalDate.now(), AppointmentStatus.SCHEDULED).size();
    }
}