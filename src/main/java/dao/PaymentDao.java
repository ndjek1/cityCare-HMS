package dao;

import models.Payment;

import java.io.Serializable;
import java.util.List;

public interface PaymentDao extends Serializable {
    public List<Payment> findByBillId(Long billId);
}
