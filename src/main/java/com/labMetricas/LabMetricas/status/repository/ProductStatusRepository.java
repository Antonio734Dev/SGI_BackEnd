package com.labMetricas.LabMetricas.status.repository;

import com.labMetricas.LabMetricas.status.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStatusRepository extends JpaRepository<ProductStatus, Integer> {
    // Find by name
    Optional<ProductStatus> findByName(String name);
    
    // Check if name exists
    boolean existsByName(String name);
    
    // Find all non-deleted statuses
    List<ProductStatus> findByDeletedAtIsNull();
    
    // Find all non-deleted statuses ordered by name
    List<ProductStatus> findByDeletedAtIsNullOrderByNameAsc();
    
    // Find by id and not deleted
    Optional<ProductStatus> findByIdAndDeletedAtIsNull(Integer id);
    
    // Count all (including deleted)
    long count();
}

