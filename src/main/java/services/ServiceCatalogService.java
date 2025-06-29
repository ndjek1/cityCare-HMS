package services;

import constants.ServiceCategory;
import models.ServiceCatalogItem;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogService {

    public List<ServiceCatalogItem> getFullServiceCatalog();
    public boolean serviceCodeExists(String serviceCode);
    public boolean serviceNameExists(String serviceName);
    public List<ServiceCatalogItem> getActiveServiceCatalogItems();
    public Optional<ServiceCatalogItem> addServiceToCatalog(String serviceCode, String name, double cost, ServiceCategory category, String description);
}
