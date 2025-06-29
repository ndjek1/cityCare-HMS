package dao;

import models.Staff;

import java.io.Serializable;
import java.util.Optional;

public interface StaffDao extends GenericDao<Staff, Long> {
    public Optional<Staff> findByIdWithSlots(Long id);
}
