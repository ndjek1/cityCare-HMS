package org.pahappa.systems.hms.services.impl;

import org.pahappa.systems.hms.constants.ServiceCategory;
import org.pahappa.systems.hms.dao.impl.ServiceCatalogItemDaoImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.ServiceCatalogItem;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.services.ServiceCatalogService;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Named
@ApplicationScoped
public class ServiceCatalogServiceImpl implements ServiceCatalogService {
    private final SessionFactory factory;
    private final ServiceCatalogItemDaoImpl serviceCatalogItemDao;


    public ServiceCatalogServiceImpl() {
        this.factory = HibernateUtil.getSessionFactory();
        this.serviceCatalogItemDao = new ServiceCatalogItemDaoImpl();
    }


    public List<ServiceCatalogItem> getFullServiceCatalog() {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction(); // Read-only is fine
            Query<ServiceCatalogItem> query = session.createQuery(
                    "FROM ServiceCatalogItem sci ORDER BY sci.category, sci.name",
                    ServiceCatalogItem.class
            );
            List<ServiceCatalogItem> items = query.getResultList();
            tx.commit();
            return items;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // Optional: Methods to check for existence if you want unique codes/names
    public boolean serviceCodeExists(String serviceCode) {
        if (serviceCode == null || serviceCode.trim().isEmpty()) return false;
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Query<Long> query = session.createQuery(
                    "SELECT count(sci.serviceId) FROM ServiceCatalogItem sci WHERE sci.serviceCode = :code", Long.class);
            query.setParameter("code", serviceCode.trim());
            boolean exists = query.uniqueResult() > 0;
            tx.commit();
            return exists;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false; // Or throw, to indicate an error during check
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    public boolean serviceNameExists(String serviceName) {
        // Similar logic as serviceCodeExists, querying by name
        if (serviceName == null || serviceName.trim().isEmpty()) return false;
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Query<Long> query = session.createQuery(
                    "SELECT count(sci.serviceId) FROM ServiceCatalogItem sci WHERE sci.name = :name", Long.class);
            query.setParameter("name", serviceName.trim());
            boolean exists = query.uniqueResult() > 0;
            tx.commit();
            return exists;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    public List<ServiceCatalogItem> getActiveServiceCatalogItems() {
        Session session = null;
        Transaction tx = null;
        try {
            session = factory.openSession();
            tx = session.beginTransaction();
            Query<ServiceCatalogItem> query = session.createQuery(
                    "FROM ServiceCatalogItem sci WHERE sci.active = true ORDER BY sci.category, sci.name",
                    ServiceCatalogItem.class
            );
            List<ServiceCatalogItem> items = query.getResultList();
            tx.commit();
            return items;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    // Method to add a new service to the catalog (for admin use)
    @Override
    public Optional<ServiceCatalogItem> addServiceToCatalog(String serviceCode, String name, double cost, ServiceCategory category, String description) {
        ServiceCatalogItem newItem = new ServiceCatalogItem(serviceCode, name, cost, category);
        newItem.setDescription(description);
        // Add uniqueness checks for serviceCode or name if necessary before persisting
        serviceCatalogItemDao.save(newItem);
        return Optional.of(newItem);
    }
    @Override
    public boolean deactivateService(ServiceCatalogItem serviceCatalogItem){
        return serviceCatalogItemDao.deactivateItem(serviceCatalogItem);
    }

    @Override
    public boolean activateService(ServiceCatalogItem serviceCatalogItem){
        return serviceCatalogItemDao.activateItem(serviceCatalogItem);
    }
}
