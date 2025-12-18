package com.labMetricas.LabMetricas.unitofmeasurement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasurementRepository extends JpaRepository<UnitOfMeasurement, Integer> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Optional<UnitOfMeasurement> findByName(String name);
    Optional<UnitOfMeasurement> findByCode(String code);
    Optional<UnitOfMeasurement> findByIdAndDeletedAtIsNull(Integer id);
    List<UnitOfMeasurement> findByParentUnitIdAndDeletedAtIsNull(Integer parentUnitId);
    List<UnitOfMeasurement> findByParentUnitIsNullAndDeletedAtIsNull(); // Unidades principales (sin padre)
}

