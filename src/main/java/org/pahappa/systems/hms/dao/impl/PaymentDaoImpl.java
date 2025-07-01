package org.pahappa.systems.hms.dao.impl;

import org.pahappa.systems.hms.dao.PaymentDao;
import org.pahappa.systems.hms.models.Payment;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class PaymentDaoImpl extends AbstractDao<Payment, Long> implements PaymentDao {

    public PaymentDaoImpl() {
        super(Payment.class);
    }

    @Override
    public List<Payment> findByBillId(Long billId) {
        return execute(session -> {
            if (billId == null) return Collections.emptyList();
            Query<Payment> query = session.createQuery(
                    "FROM Payment p WHERE p.billId = :billId ORDER BY p.paymentDate DESC", Payment.class);
            query.setParameter("billId", billId);
            return query.getResultList();
        });
    }
}
