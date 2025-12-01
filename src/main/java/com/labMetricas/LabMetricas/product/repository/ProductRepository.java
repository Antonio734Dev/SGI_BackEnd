package com.labMetricas.LabMetricas.product.repository;

import com.labMetricas.LabMetricas.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Find by id and not deleted
    Optional<Product> findByIdAndDeletedAtIsNull(Integer id);
    
    // Find all non-deleted products
    List<Product> findByDeletedAtIsNull();
    
    // Find all non-deleted products with pagination
    Page<Product> findByDeletedAtIsNull(Pageable pageable);
    
    // Find by lote
    Optional<Product> findByLote(String lote);
    
    // Find by lote and not deleted
    Optional<Product> findByLoteAndDeletedAtIsNull(String lote);
    
    // Find by stock catalogue and not deleted
    List<Product> findByStockCatalogueIdAndDeletedAtIsNull(Integer stockCatalogueId);
    
    // Find by stock catalogue with pagination
    Page<Product> findByStockCatalogueIdAndDeletedAtIsNull(Integer stockCatalogueId, Pageable pageable);
    
    // Find by product status and not deleted
    Page<Product> findByProductStatusIdAndDeletedAtIsNull(Integer productStatusId, Pageable pageable);
    
    // Find by stock catalogue and product status
    Page<Product> findByStockCatalogueIdAndProductStatusIdAndDeletedAtIsNull(Integer stockCatalogueId, Integer productStatusId, Pageable pageable);
    
    // Find by QR code and not deleted
    Optional<Product> findByQrCodeIdAndDeletedAtIsNull(Integer qrCodeId);
    
    // Find products by stock catalogue and status "terminado" (case insensitive)
    List<Product> findByStockCatalogueIdAndProductStatusNameIgnoreCaseAndDeletedAtIsNull(
        Integer stockCatalogueId, String statusName);
}

