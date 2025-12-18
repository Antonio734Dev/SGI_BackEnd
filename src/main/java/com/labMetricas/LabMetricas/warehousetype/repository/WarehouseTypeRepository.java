package com.labMetricas.LabMetricas.warehousetype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;

import java.util.Optional;

@Repository
public interface WarehouseTypeRepository extends JpaRepository<WarehouseType, Integer> {
    Optional<WarehouseType> findByCode(String code);
    boolean existsByCode(String code);
    Optional<WarehouseType> findByIdAndDeletedAtIsNull(Integer id);
}

