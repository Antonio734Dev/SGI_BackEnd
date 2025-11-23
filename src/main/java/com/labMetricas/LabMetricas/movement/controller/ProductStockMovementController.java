package com.labMetricas.LabMetricas.movement.controller;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.movement.service.ProductStockMovementService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stock-movements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductStockMovementController {
    private static final Logger logger = LoggerFactory.getLogger(ProductStockMovementController.class);

    private final ProductStockMovementService productStockMovementService;

    @Autowired
    public ProductStockMovementController(ProductStockMovementService productStockMovementService) {
        this.productStockMovementService = productStockMovementService;
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer stockCatalogueId,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve stock movements - page: {}, size: {}, stockCatalogueId: {}, tipo: {}, fechaInicio: {}, fechaFin: {}", 
            auth.getName(), page, size, stockCatalogueId, tipoMovimiento, fechaInicio, fechaFin);
        
        return productStockMovementService.getStockMovements(page, size, stockCatalogueId, tipoMovimiento, fechaInicio, fechaFin);
    }
}

