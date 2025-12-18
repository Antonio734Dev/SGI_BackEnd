package com.labMetricas.LabMetricas.unitofmeasurement.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;
import com.labMetricas.LabMetricas.unitofmeasurement.service.UnitOfMeasurementService;
import com.labMetricas.LabMetricas.util.ResponseObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/units-of-measurement")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UnitOfMeasurementController {

    @Autowired
    private UnitOfMeasurementService unitOfMeasurementService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllUnits() {
        List<UnitOfMeasurement> units = unitOfMeasurementService.getAllUnits();
        return ResponseEntity.ok(
            new ResponseObject("Units of measurement retrieved successfully", units, TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/main")
    public ResponseEntity<ResponseObject> getMainUnits() {
        List<UnitOfMeasurement> units = unitOfMeasurementService.getMainUnits();
        return ResponseEntity.ok(
            new ResponseObject("Main units retrieved successfully", units, TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/{id}/sub-units")
    public ResponseEntity<ResponseObject> getSubUnits(@PathVariable Integer id) {
        List<UnitOfMeasurement> subUnits = unitOfMeasurementService.getSubUnits(id);
        return ResponseEntity.ok(
            new ResponseObject("Sub-units retrieved successfully", subUnits, TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getUnitById(@PathVariable Integer id) {
        Optional<UnitOfMeasurement> unit = unitOfMeasurementService.getUnitById(id);
        if (unit.isPresent()) {
            return ResponseEntity.ok(
                new ResponseObject("Unit retrieved successfully", unit.get(), TypeResponse.SUCCESS)
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ResponseObject("Unit not found", null, TypeResponse.ERROR)
        );
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createUnit(@RequestBody UnitOfMeasurement unit) {
        try {
            UnitOfMeasurement created = unitOfMeasurementService.createUnit(unit);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Unit created successfully", created, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating unit: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateUnit(@PathVariable Integer id, @RequestBody UnitOfMeasurement unit) {
        try {
            Optional<UnitOfMeasurement> existing = unitOfMeasurementService.getUnitById(id);
            if (existing.isPresent()) {
                unit.setId(id);
                UnitOfMeasurement updated = unitOfMeasurementService.updateUnit(unit);
                return ResponseEntity.ok(
                    new ResponseObject("Unit updated successfully", updated, TypeResponse.SUCCESS)
                );
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Unit not found", null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating unit: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteUnit(@PathVariable Integer id) {
        try {
            unitOfMeasurementService.deleteUnit(id);
            return ResponseEntity.ok(
                new ResponseObject("Unit deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting unit: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }
}

