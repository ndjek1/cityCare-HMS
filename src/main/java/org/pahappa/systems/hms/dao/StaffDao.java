package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.models.Staff;

import java.util.Optional;

public interface StaffDao extends GenericDao<Staff, Long> {
    public Optional<Staff> findByIdWithSlots(Long id);
}
