package com.labMetricas.LabMetricas.warehousetype.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;
import com.labMetricas.LabMetricas.warehousetype.repository.WarehouseTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WarehouseTypeService {

    @Autowired
    private WarehouseTypeRepository warehouseTypeRepository;

    public List<WarehouseType> getAllWarehouseTypes() {
        return warehouseTypeRepository.findAll().stream()
            .filter(wt -> wt.getDeletedAt() == null)
            .toList();
    }

    public Optional<WarehouseType> getWarehouseTypeById(Integer id) {
        return warehouseTypeRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<WarehouseType> getWarehouseTypeByCode(String code) {
        return warehouseTypeRepository.findByCode(code);
    }

    public WarehouseType createWarehouseType(WarehouseType warehouseType) {
        warehouseType.setCreatedAt(LocalDateTime.now());
        warehouseType.setUpdatedAt(LocalDateTime.now());
        return warehouseTypeRepository.save(warehouseType);
    }

    public WarehouseType updateWarehouseType(WarehouseType warehouseType) {
        warehouseType.setUpdatedAt(LocalDateTime.now());
        return warehouseTypeRepository.save(warehouseType);
    }

    public void deleteWarehouseType(Integer id) {
        Optional<WarehouseType> warehouseType = warehouseTypeRepository.findById(id);
        if (warehouseType.isPresent()) {
            WarehouseType wt = warehouseType.get();
            wt.setDeletedAt(LocalDateTime.now());
            warehouseTypeRepository.save(wt);
        }
    }
}

