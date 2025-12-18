package com.labMetricas.LabMetricas.warehousetype.controller;

import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;
import com.labMetricas.LabMetricas.warehousetype.service.WarehouseTypeService;
import com.labMetricas.LabMetricas.enums.TypeResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouse-types")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WarehouseTypeController {

    @Autowired
    private WarehouseTypeService warehouseTypeService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllWarehouseTypes() {
        List<WarehouseType> warehouseTypes = warehouseTypeService.getAllWarehouseTypes();
        return ResponseEntity.ok(
            new ResponseObject("Warehouse types retrieved successfully", warehouseTypes, TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getWarehouseTypeById(@PathVariable Integer id) {
        Optional<WarehouseType> warehouseType = warehouseTypeService.getWarehouseTypeById(id);
        if (warehouseType.isPresent()) {
            return ResponseEntity.ok(
                new ResponseObject("Warehouse type retrieved successfully", warehouseType.get(), TypeResponse.SUCCESS)
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ResponseObject("Warehouse type not found", null, TypeResponse.ERROR)
        );
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createWarehouseType(@RequestBody WarehouseType warehouseType) {
        try {
            WarehouseType created = warehouseTypeService.createWarehouseType(warehouseType);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Warehouse type created successfully", created, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating warehouse type: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateWarehouseType(@PathVariable Integer id, @RequestBody WarehouseType warehouseType) {
        try {
            Optional<WarehouseType> existing = warehouseTypeService.getWarehouseTypeById(id);
            if (existing.isPresent()) {
                warehouseType.setId(id);
                WarehouseType updated = warehouseTypeService.updateWarehouseType(warehouseType);
                return ResponseEntity.ok(
                    new ResponseObject("Warehouse type updated successfully", updated, TypeResponse.SUCCESS)
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Warehouse type not found", null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating warehouse type: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteWarehouseType(@PathVariable Integer id) {
        try {
            warehouseTypeService.deleteWarehouseType(id);
            return ResponseEntity.ok(
                new ResponseObject("Warehouse type deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting warehouse type: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }
}

