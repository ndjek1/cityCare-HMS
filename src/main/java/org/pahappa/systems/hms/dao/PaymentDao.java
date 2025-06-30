package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.models.Payment;

import java.io.Serializable;
import java.util.List;

public interface PaymentDao extends Serializable {
    public List<Payment> findByBillId(Long billId);
}
