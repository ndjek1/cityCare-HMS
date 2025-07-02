package org.pahappa.systems.hms.services.impl;

import org.pahappa.systems.hms.dao.impl.PrescriptionDaoImpl;
import org.pahappa.systems.hms.models.Prescription;
import org.pahappa.systems.hms.services.PrescriptionService;

import java.util.List;
import java.util.Optional;

public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionDaoImpl prescriptionDao ;

    public PrescriptionServiceImpl() {
        this.prescriptionDao = new PrescriptionDaoImpl();
    }

    @Override
    public Optional<Prescription>  registerPrescription(Prescription prescription) {
        try{
            if(prescription != null) {
                prescriptionDao.save(prescription);
                return  Optional.of(prescription);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return Optional.empty();
    }
    @Override
    public List<Prescription> findByPatientId(Long patientId) {
        try{
            if(patientId != null) {
                List<Prescription> prescriptions = prescriptionDao.getPrescriptionsForPatient(patientId);
                if (prescriptions != null) {
                    return  prescriptions;
                }else  {
                    System.out.println("No prescriptions found for patient id " + patientId);
                    return null;
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Prescription> findByAppointmentId(Long appointmentId) {
        try{
            if(appointmentId != null) {
                List<Prescription> prescriptions = prescriptionDao.getPrescriptionsForAppointment(appointmentId);
                if (prescriptions != null) {
                    return  prescriptions;
                }else  {
                    System.out.println("No prescriptions found for patient id " + appointmentId);
                    return null;
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
