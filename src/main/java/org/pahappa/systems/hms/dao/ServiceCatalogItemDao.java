package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.models.ServiceCatalogItem;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface ServiceCatalogItemDao extends Serializable {
    public Optional<ServiceCatalogItem> findByCode(String code);
    public Optional<ServiceCatalogItem> findByName(String name);
    public List<ServiceCatalogItem> findActiveItems();
    public boolean deactivateItem(ServiceCatalogItem item);
    public boolean activateItem(ServiceCatalogItem item);
}
