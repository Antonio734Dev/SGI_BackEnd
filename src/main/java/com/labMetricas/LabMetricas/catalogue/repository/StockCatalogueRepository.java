package com.labMetricas.LabMetricas.catalogue.repository;

import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockCatalogueRepository extends JpaRepository<StockCatalogue, Integer> {
    // Find by SKU
    Optional<StockCatalogue> findBySku(String sku);
    
    // Check if SKU exists
    boolean existsBySku(String sku);
    
    // Find all non-deleted catalogues
    List<StockCatalogue> findByDeletedAtIsNull();
    
    // Find all non-deleted with pagination
    Page<StockCatalogue> findByDeletedAtIsNull(Pageable pageable);
    
    // Find all non-deleted ordered by name
    List<StockCatalogue> findByDeletedAtIsNullOrderByNameAsc();
    
    // Find by name containing (case insensitive) with pagination
    Page<StockCatalogue> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find by id and not deleted
    Optional<StockCatalogue> findByIdAndDeletedAtIsNull(Integer id);
}

