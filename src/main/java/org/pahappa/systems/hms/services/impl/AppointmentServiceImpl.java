package org.pahappa.systems.hms.services.impl;

import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.dao.impl.AppointmentDaoImpl;
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

    private final SessionFactory factory;
    private final AppointmentDaoImpl appointmentDao;

    public AppointmentServiceImpl() {
        this.appointmentDao = new AppointmentDaoImpl();
        this.factory = HibernateUtil.getSessionFactory();
    }

    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        try (Session session = factory.openSession()) {
            Query<Appointment> query = session.createQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.patient " +
                            "WHERE a.doctor.staffId = :doctorId ORDER BY a.dateTime DESC", Appointment.class);
            query.setParameter("doctorId", doctorId);
            List<Appointment> appointments = query.getResultList();
            if (!appointments.isEmpty()) {
                for(Appointment appointment : appointments) {
                    Hibernate.initialize(appointment.getPatient());
                    Hibernate.initialize(appointment.getDoctor());
                    Hibernate.initialize(appointment.getBill());
                }
            }
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ... Implement other Appointment methods like getAppointmentsForPatient, findById, etc. ...

    public Optional<Appointment> bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason) {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Patient patient = session.get(Patient.class, patientId);
            Staff doctor = session.get(Staff.class, doctorId);

            if (patient == null || doctor == null) {
                if(tx.isActive()) tx.rollback();
                return Optional.empty();
            }

            Hibernate.initialize(doctor.getAvailableSlots());
            if (doctor.removeAvailability(dateTime)) { // Attempt to consume the slot
                Appointment appointment = new Appointment(patient, doctor, dateTime, reason);
                appointmentDao.save(appointment);
                return Optional.of(appointment);
            } else {
                // Slot was not available
                if(tx.isActive()) tx.rollback();
                return Optional.empty();
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null) session.close();
        }
    }

    public boolean recordDiagnosis(Long appointmentId, String diagnosisNotes) {
        // ... (implementation is complex, involving updates to Appointment and Patient)
        // This method also needs a service-level transaction as it modifies two entities.
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if(appointment == null || appointment.getStatus() != AppointmentStatus.SCHEDULED) {
                if (tx.isActive()) tx.rollback();
                return false;
            }

            appointment.setDiagnosisNotes(diagnosisNotes);
            appointment.setStatus(AppointmentStatus.COMPLETED);

            Patient patient = appointment.getPatient();
            if(patient != null) {
                String historyEntry = "Date: " + appointment.getDateTime().toLocalDate() + ", Diagnosis: " + diagnosisNotes;
                patient.addMedicalHistory(historyEntry);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }





    public boolean rescheduleAppointment(Long appointmentId, Long newDoctorId, LocalDateTime newDateTime) {
        Session session = null;
        Transaction tx = null;
        System.out.println("SERVICE: Rescheduling Appt ID " + appointmentId + " to Dr ID " + newDoctorId + " at " + newDateTime);

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment == null) {
                System.err.println("SERVICE: Appointment " + appointmentId + " not found for reschedule.");
                if (tx.isActive()) tx.rollback();
                return false;
            }
            if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
                System.err.println("SERVICE: Appointment " + appointmentId + " is " + appointment.getStatus() + " and cannot be rescheduled.");
                if (tx.isActive()) tx.rollback();
                return false;
            }

            Staff oldDoctor = appointment.getDoctor();
            LocalDateTime oldDateTime = appointment.getDateTime();

            Staff newDoctor = session.get(Staff.class, newDoctorId);
            if (newDoctor == null) {
                System.err.println("SERVICE: New Doctor ID " + newDoctorId + " not found.");
                if (tx.isActive()) tx.rollback();
                return false;
            }
            if (newDoctor.getRole() != org.pahappa.systems.hms.constants.UserRole.DOCTOR){
                System.err.println("SERVICE: Selected new staff (ID: " + newDoctorId + ") is not a DOCTOR.");
                if (tx.isActive()) tx.rollback();
                return false;
            }


            // 1. Make the old slot available again for the old doctor
            if (oldDoctor != null && oldDateTime != null) {
                Hibernate.initialize(oldDoctor.getAvailableSlots());
                oldDoctor.addAvailability(oldDateTime);
                session.merge(oldDoctor); // Persist change to old doctor's availability
                System.out.println("SERVICE: Old slot " + oldDateTime + " made available for Dr. " + oldDoctor.getName());
            }

            // 2. Check if the new slot is available for the new doctor and book it
            Hibernate.initialize(newDoctor.getAvailableSlots());
            if (newDoctor.getAvailableSlots().contains(newDateTime)) {
                if (newDoctor.removeAvailability(newDateTime)) { // removeAvailability returns true if element was present and removed
                    appointment.setDoctor(newDoctor);
                    appointment.setDateTime(newDateTime);
                    appointment.setStatus(AppointmentStatus.SCHEDULED); // Back to scheduled
                    session.merge(appointment); // Persist changes to appointment
                    session.merge(newDoctor);     // Persist changes to new doctor's availability
                    System.out.println("SERVICE: New slot " + newDateTime + " booked with Dr. " + newDoctor.getName());
                } else {
                    // This case implies the slot was in availableSlots but removeAvailability failed, which is odd.
                    // More likely, contains() would be false if the slot wasn't there.
                    System.err.println("SERVICE: New slot " + newDateTime + " was listed but could not be removed for Dr. " + newDoctor.getName() + ". Concurrency issue?");
                    if (tx.isActive()) tx.rollback();
                    return false;
                }
            } else {
                System.err.println("SERVICE: New slot " + newDateTime + " is NOT available for Dr. " + newDoctor.getName());
                // IMPORTANT: If we get here, we've made the old slot available but couldn't book the new one.
                // We MUST roll back the entire transaction to revert making the old slot available.
                if (tx.isActive()) tx.rollback();
                return false;
            }

            tx.commit();
            System.out.println("SERVICE: Appointment " + appointmentId + " successfully rescheduled.");
            return true;

        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Exception during appointment reschedule for ID: " + appointmentId);
            e.printStackTrace();
            if (tx != null && tx.isActive()) {
                System.err.println("SERVICE: Rolling back transaction due to exception.");
                tx.rollback();
            }
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }



    public boolean cancelAppointment(Long appointmentId) {
        Session session = null;
        Transaction tx = null;
        System.out.println("SERVICE: Attempting to cancel appointment ID: " + appointmentId);

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            Appointment appointment = session.get(Appointment.class, appointmentId); // Use get or find

            if (appointment != null) {
                // Check if the appointment can be cancelled (e.g., not already completed or cancelled)
                if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                    System.out.println("SERVICE: Appointment " + appointmentId + " is already cancelled.");
                    tx.commit(); // Commit if no changes needed, or just rollback if preferred
                    return true; // Or false if "already cancelled" is considered not a successful new cancellation
                }
                if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
                    System.out.println("SERVICE: Appointment " + appointmentId + " is already completed and cannot be cancelled.");
                    tx.commit();
                    return false;
                }

                // Proceed with cancellation
                appointment.setStatus(AppointmentStatus.CANCELLED);
                System.out.println("SERVICE: Appointment " + appointmentId + " status set to CANCELLED.");

                // Get the doctor associated with this appointment
                Staff doctor = appointment.getDoctor(); // Assuming Appointment has a getDoctor() returning Staff
                LocalDateTime appointmentDateTime = appointment.getDateTime();

                if (doctor != null && appointmentDateTime != null) {
                    Hibernate.initialize(doctor.getAvailableSlots());

                    // Add the slot back to the doctor's availability
                    // The addAvailability method is on your Staff entity
                    if (doctor.addAvailability(appointmentDateTime)) {
                        System.out.println("SERVICE: Slot " + appointmentDateTime + " made available again for Dr. " + doctor.getName());
                    } else {
                        System.out.println("SERVICE: Slot " + appointmentDateTime + " was already marked as available or could not be added for Dr. " + doctor.getName());
                    }
                    // Hibernate will detect changes to the 'doctor' entity's 'availableSlots' collection
                    // and persist them if 'doctor' is managed and cascading is appropriate or if you merge.
                    session.merge(doctor); // Explicitly merge changes to the doctor's state
                } else {
                    System.err.println("SERVICE WARNING: Could not retrieve doctor or appointment time for appointment ID " + appointmentId + " to update availability.");
                }

                session.merge(appointment);


                tx.commit();
                System.out.println("SERVICE: Appointment " + appointmentId + " cancellation processed and committed.");
                return true;

            } else {
                System.out.println("SERVICE: Appointment ID " + appointmentId + " not found. Cancellation failed.");
                // No need to rollback if no transaction was effectively started or nothing changed
                if (tx != null && tx.isActive()) { // Check if active, though it might not be if find returned null early
                    tx.commit(); // or tx.rollback(); depending on preference for "not found"
                }
                return false;
            }

        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Exception during appointment cancellation for ID: " + appointmentId);
            e.printStackTrace(); // Log error
            if (tx != null && tx.isActive()) {
                System.err.println("SERVICE: Rolling back transaction due to exception.");
                tx.rollback();
            }
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
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
        Session session = null;
        Transaction tx = null; // Read-only, but transaction is good for initialize
        try {
            session = factory.openSession();
            tx = session.beginTransaction(); // Start transaction

            Appointment appointment = session.get(Appointment.class, appointmentId);

            if (appointment != null) {
                // Explicitly initialize the proxied associations while the session is open
                Hibernate.initialize(appointment.getPatient());
                Hibernate.initialize(appointment.getDoctor());
                // If Patient or Doctor have further lazy associations you need, initialize them too:
                // e.g., Hibernate.initialize(appointment.getPatient().getSomeLazyCollection());
            }

            tx.commit(); // Commit transaction
            return Optional.ofNullable(appointment);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }



    public List<Appointment> getAppointmentsForPatient(Long patientId) {

        Session session = null; // Method-local session
        Transaction tx = null;   // Method-local transaction
        List<Appointment> appointmentList = null;

        try {
            session = factory.openSession();
            tx = session.beginTransaction();

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

    public long getTodaysScheduledAppointmentCount() {
        // Here we can use the specific DAO method
        return appointmentDao.findByDateAndStatus(LocalDate.now(), AppointmentStatus.SCHEDULED).size();
    }
}