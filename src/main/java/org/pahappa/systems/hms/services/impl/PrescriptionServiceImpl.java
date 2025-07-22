package org.pahappa.systems.hms.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.constants.PaymentMethod;
import org.pahappa.systems.hms.constants.PaymentStatus;
import org.pahappa.systems.hms.dao.AppointmentDao;
import org.pahappa.systems.hms.dao.PrescriptionDao;
import org.pahappa.systems.hms.dao.impl.AppointmentDaoImpl;
import org.pahappa.systems.hms.dao.impl.PaymentDaoImpl;
import org.pahappa.systems.hms.dao.impl.PrescriptionDaoImpl;
import org.pahappa.systems.hms.models.*;
import org.pahappa.systems.hms.services.PrescriptionService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named("prescriptionService")
@ApplicationScoped
public class PrescriptionServiceImpl implements PrescriptionService {


    private final SessionFactory factory; // Inject factory for service-level transactions


    private final PrescriptionDao prescriptionDao; // Inject the DAO interface
    private final PaymentDaoImpl paymentDao;

    private final  AppointmentDao appointmentDao; // Inject other DAOs as needed

    // The constructor is now empty. CDI handles injections.
    public PrescriptionServiceImpl() {
        this.appointmentDao = new AppointmentDaoImpl();
        this.prescriptionDao = new PrescriptionDaoImpl();
        this.paymentDao = new PaymentDaoImpl();
        this.factory = HibernateUtil.getSessionFactory();
        System.out.println("PrescriptionServiceImpl CDI bean CREATED.");
    }

    /**
     * Creates a new prescription and associates it with an appointment.
     * This method is responsible for business logic, like ensuring the appointment exists.
     * It uses a service-level transaction because it modifies multiple related objects.
     *
     * @param appointmentId The ID of the appointment this prescription is for.
     * @param items The list of medications prescribed.
     * @return true if the prescription was created successfully.
     */
    @Override
    public boolean createPrescription(Long appointmentId, List<PrescribedMedication> items) {
        if (appointmentId == null || items == null || items.isEmpty()) {
            System.err.println("SERVICE ERROR: Cannot create prescription with invalid appointment ID or empty medication list.");
            return false;
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();

            // Fetch the managed instance of the Appointment
            Appointment managedAppointment = session.get(Appointment.class, appointmentId);
            if (managedAppointment == null) {
                System.err.println("SERVICE ERROR: Appointment with ID " + appointmentId + " not found.");
                if (tx.isActive()) tx.rollback();
                return false;
            }

            // Create the new prescription and link it
            Prescription prescription = new Prescription();
            prescription.setAppointment(managedAppointment);
            prescription.setPatient(managedAppointment.getPatient());
            prescription.setDoctor(managedAppointment.getDoctor());
            // The PrescribedMedication items are @Embeddable, so we just set the list
            prescription.setPrescribedMedications(items);

            session.persist(prescription);

            tx.commit();
            System.out.println("SERVICE: Successfully created prescription ID " + prescription.getPrescriptionId() + " for appointment ID " + appointmentId);
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("SERVICE EXCEPTION: Failed to create prescription for appointment ID " + appointmentId);
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    // This method replaces your old `registerPrescription` and is more descriptive.
    public Optional<Prescription> savePrescription(Prescription prescription) {
        try {
            prescriptionDao.save(prescription);
            return Optional.of(prescription);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Prescription> findByPatientId(Long patientId) {
        if (patientId == null) {
            return Collections.emptyList(); // Always return a list, never null
        }
        try {
            // Assuming your PrescriptionDao has a method for this
            return prescriptionDao.findByPatientId(patientId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Prescription> findByAppointmentId(Long appointmentId) {
        if (appointmentId == null) {
            return Collections.emptyList();
        }
        try {
            // Assuming your PrescriptionDao has a method for this
            return prescriptionDao.findByAppointmentId(appointmentId);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Prescription> findAllUnpaid() {

        List<Prescription> allUnpaidPrescriptions;

        System.out.println("SERVICE: Fetching all unpaid or partially paid bills.");
        allUnpaidPrescriptions = prescriptionDao.findAllUnpaid();

        return allUnpaidPrescriptions;
    }


    public long findByDoctorId(Long doctorId) {

        List<Prescription> allUnpaidPrescriptions;

        System.out.println("SERVICE: Fetching all unpaid or partially paid bills.");
        allUnpaidPrescriptions = prescriptionDao.findByDoctorId(doctorId);

        return allUnpaidPrescriptions.size();
    }

    public Optional<Payment> processPaymentForPrescription(Long prescriptionId, double amountPaid, PaymentMethod method) {
        if (prescriptionId == null || amountPaid <= 0) {
            System.err.println("Invalid prescriptionId or amountPaid. prescriptionId=" + prescriptionId + ", amountPaid=" + amountPaid);
            return Optional.empty();
        }

        try {
            Optional<Prescription> prescriptionOpt = prescriptionDao.findById(prescriptionId);

            if (prescriptionOpt.isPresent()) {
                Prescription prescription = prescriptionOpt.get();

                if (prescription.getPaymentStatus() == PaymentStatus.PAID) {
                    System.out.println("Bill " + prescriptionId + " is already fully paid.");
                    return Optional.empty();
                }

                Payment payment = new Payment(prescription, amountPaid, method);

                paymentDao.save(payment);

                if (amountPaid >= prescription.getTotalCost()) {
                    prescription.setPaymentStatus(PaymentStatus.PAID);
                } else {
                    prescription.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
                }

                prescriptionDao.update(prescription);
                return Optional.of(payment);
            } else {
                System.err.println("No Prescription found for ID: " + prescriptionId);
            }
        } catch (Exception e) {
            e.printStackTrace(); // You can log this more cleanly in real apps
        }

        return Optional.empty();
    }


    // You'll need to add an implementation for `getPrescriptionsForPatient` to your PrescriptionDao interface and implementation.
}