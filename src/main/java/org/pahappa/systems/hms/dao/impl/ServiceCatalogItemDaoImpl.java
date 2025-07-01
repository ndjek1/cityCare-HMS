package org.pahappa.systems.hms.dao.impl;


import org.pahappa.systems.hms.dao.ServiceCatalogItemDao;
import org.pahappa.systems.hms.models.ServiceCatalogItem;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;


public class ServiceCatalogItemDaoImpl extends AbstractDao<ServiceCatalogItem, Long> implements ServiceCatalogItemDao {

    public ServiceCatalogItemDaoImpl() {
        super(ServiceCatalogItem.class);
    }

    @Override
    public Optional<ServiceCatalogItem> findByCode(String code) {
        return execute(session -> {
            if (code == null || code.trim().isEmpty()) return Optional.empty();
            Query<ServiceCatalogItem> query = session.createQuery(
                    "FROM ServiceCatalogItem sci WHERE sci.serviceCode = :code", ServiceCatalogItem.class);
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        });
    }

    @Override
    public Optional<ServiceCatalogItem> findByName(String name) {
        return execute(session -> {
            if (name == null || name.trim().isEmpty()) return Optional.empty();
            Query<ServiceCatalogItem> query = session.createQuery(
                    "FROM ServiceCatalogItem sci WHERE sci.name = :name", ServiceCatalogItem.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        });
    }

    @Override
    public List<ServiceCatalogItem> findActiveItems() {
        return execute(session -> {
            Query<ServiceCatalogItem> query = session.createQuery(
                    "FROM ServiceCatalogItem sci WHERE sci.active = true ORDER BY sci.category, sci.name", ServiceCatalogItem.class);
            return query.getResultList();
        });
    }

    @Override
    public boolean deactivateItem(ServiceCatalogItem item) {
        execute(session -> {
            item.setActive(false);
            session.merge(item);
            return true;
        });
        return false;
    }
    @Override
    public boolean activateItem(ServiceCatalogItem item) {
        execute(session -> {
            item.setActive(true);
            session.merge(item);
            return true;
        });
        return false;
    }
}
