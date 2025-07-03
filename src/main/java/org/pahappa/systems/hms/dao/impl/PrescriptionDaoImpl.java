package org.pahappa.systems.hms.dao.impl;

import org.hibernate.Hibernate;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.dao.PrescriptionDao;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.models.Prescription;

import java.util.Collections;
import java.util.List;

public class PrescriptionDaoImpl extends AbstractDao<Prescription,Long> implements PrescriptionDao {

    public PrescriptionDaoImpl() {
        super(Prescription.class);
    }

    @Override
    public  List<Prescription> getPrescriptionsForPatient(Long patientId){
        System.out.println("getPrescriptionsForPatient");
        return execute(session -> {
            if (patientId == null) return Collections.emptyList();
            Query<Prescription> query = session.createQuery(
                    "FROM Prescription p WHERE p.patient.patientId = :patientId ORDER BY p.startDate DESC", Prescription.class);
            query.setParameter("patientId", patientId);
            List<Prescription> prescriptions = query.getResultList();
            if(!prescriptions.isEmpty()){
                for(Prescription prescription: prescriptions){
                    Hibernate.initialize(prescription.getPatient());
                    Hibernate.initialize(prescription.getAppointment());
                    Hibernate.initialize(prescription.getDoctor());
                }
            }
            return query.getResultList();
        });
    }

    @Override
    public List<Prescription> getPrescriptionsForAppointment(Long appointmentId){
        return execute(session ->  {
            if (appointmentId == null) return Collections.emptyList();
            Query<Prescription> query = session.createQuery(
                    "FROM Prescription  p WHERE p.appointment.appointmentId = :appointmentId ORDER BY p.startDate DESC", Prescription.class
            );
            query.setParameter("appointmentId", appointmentId);
            return query.getResultList();
        });
    }


}
