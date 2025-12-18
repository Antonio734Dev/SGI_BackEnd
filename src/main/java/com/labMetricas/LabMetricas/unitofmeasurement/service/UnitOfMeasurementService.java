package com.labMetricas.LabMetricas.unitofmeasurement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;
import com.labMetricas.LabMetricas.unitofmeasurement.repository.UnitOfMeasurementRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UnitOfMeasurementService {

    @Autowired
    private UnitOfMeasurementRepository unitOfMeasurementRepository;

    public List<UnitOfMeasurement> getAllUnits() {
        return unitOfMeasurementRepository.findAll().stream()
            .filter(uom -> uom.getDeletedAt() == null)
            .toList();
    }

    public List<UnitOfMeasurement> getMainUnits() {
        return unitOfMeasurementRepository.findByParentUnitIsNullAndDeletedAtIsNull();
    }

    public List<UnitOfMeasurement> getSubUnits(Integer parentUnitId) {
        return unitOfMeasurementRepository.findByParentUnitIdAndDeletedAtIsNull(parentUnitId);
    }

    public Optional<UnitOfMeasurement> getUnitById(Integer id) {
        return unitOfMeasurementRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<UnitOfMeasurement> getUnitByCode(String code) {
        return unitOfMeasurementRepository.findByCode(code);
    }

    public UnitOfMeasurement createUnit(UnitOfMeasurement unit) {
        unit.setCreatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());
        return unitOfMeasurementRepository.save(unit);
    }

    public UnitOfMeasurement updateUnit(UnitOfMeasurement unit) {
        unit.setUpdatedAt(LocalDateTime.now());
        return unitOfMeasurementRepository.save(unit);
    }

    public void deleteUnit(Integer id) {
        Optional<UnitOfMeasurement> unit = unitOfMeasurementRepository.findById(id);
        if (unit.isPresent()) {
            UnitOfMeasurement uom = unit.get();
            uom.setDeletedAt(LocalDateTime.now());
            unitOfMeasurementRepository.save(uom);
        }
    }
}

