package com.labMetricas.LabMetricas.catalogue.repository;

import com.labMetricas.LabMetricas.catalogue.model.MeasurementUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnit, Integer> {

    boolean existsByName(String name);

    Optional<MeasurementUnit> findByName(String name);
}


