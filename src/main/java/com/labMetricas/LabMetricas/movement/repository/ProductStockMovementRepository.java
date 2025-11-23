package com.labMetricas.LabMetricas.movement.repository;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.movement.model.ProductStockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockMovementRepository extends JpaRepository<ProductStockMovement, Integer> {
    // Find by id and not deleted
    Optional<ProductStockMovement> findByIdAndDeletedAtIsNull(Integer id);
    
    // Find all non-deleted movements
    List<ProductStockMovement> findByDeletedAtIsNull();
    
    // Find all non-deleted movements with pagination
    Page<ProductStockMovement> findByDeletedAtIsNull(Pageable pageable);
    
    // Find by stock catalogue and not deleted
    List<ProductStockMovement> findByStockCatalogueIdAndDeletedAtIsNull(Integer stockCatalogueId);
    
    // Find by stock catalogue with pagination
    Page<ProductStockMovement> findByStockCatalogueIdAndDeletedAtIsNull(Integer stockCatalogueId, Pageable pageable);
    
    // Find by stock catalogue ordered by created date desc
    List<ProductStockMovement> findByStockCatalogueIdAndDeletedAtIsNullOrderByCreatedAtDesc(Integer stockCatalogueId);
    
    // Find by tipo and not deleted
    Page<ProductStockMovement> findByTipoAndDeletedAtIsNull(TipoMovimiento tipo, Pageable pageable);
    
    // Find by stock catalogue and tipo
    Page<ProductStockMovement> findByStockCatalogueIdAndTipoAndDeletedAtIsNull(Integer stockCatalogueId, TipoMovimiento tipo, Pageable pageable);
    
    // Find by date range
    Page<ProductStockMovement> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by stock catalogue and date range
    Page<ProductStockMovement> findByStockCatalogueIdAndCreatedAtBetweenAndDeletedAtIsNull(Integer stockCatalogueId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by tipo and date range
    Page<ProductStockMovement> findByTipoAndCreatedAtBetweenAndDeletedAtIsNull(TipoMovimiento tipo, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by stock catalogue, tipo and date range
    Page<ProductStockMovement> findByStockCatalogueIdAndTipoAndCreatedAtBetweenAndDeletedAtIsNull(Integer stockCatalogueId, TipoMovimiento tipo, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

