package org.pahappa.systems.hms.dao.impl;

import org.hibernate.query.Query;
import org.pahappa.systems.hms.dao.StaffDao;
import org.pahappa.systems.hms.models.Staff;
import org.hibernate.Hibernate;
import org.pahappa.systems.hms.models.UserAccount;

import java.util.List;
import java.util.Optional;

public class StaffDaoImpl extends AbstractDao<Staff, Long> implements StaffDao {

    public StaffDaoImpl() {
        super(Staff.class);
    }

    @Override
    public Optional<Staff> findByIdWithSlots(Long id) {
        return execute(session -> {
            Optional<Staff> staffOpt = Optional.ofNullable(session.get(Staff.class, id));
            staffOpt.ifPresent(staff -> Hibernate.initialize(staff.getAvailableSlots()));
            return staffOpt;
        });
    }
    @Override
    public void delete(Staff entity) {
        execute(session -> {
            entity.setDeleted(true);
            session.merge(entity);
            return null;
        });
    }
    @Override
    public Optional<Staff> findById(Long id) {
        return execute(session -> {
            if (id == null) {
                return Optional.empty();
            }

            Staff staff = session.get(Staff.class, id);
            if (staff != null) {
                Hibernate.initialize(staff.getAvailableSlots()); // initialize lazy collection
            }

            return Optional.ofNullable(staff);
        });
    }

    @Override
    public List<Staff> findAll() {
        return execute(session -> {
            Query<Staff> query = session.createQuery(
                    "FROM Staff s WHERE s.deleted = false ", Staff.class);
            return query.getResultList();
        });
    }


}