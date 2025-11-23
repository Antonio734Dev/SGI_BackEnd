package com.labMetricas.LabMetricas.status.controller;

import com.labMetricas.LabMetricas.status.model.dto.ProductStatusDto;
import com.labMetricas.LabMetricas.status.service.ProductStatusService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-statuses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductStatusController {
    private static final Logger logger = LoggerFactory.getLogger(ProductStatusController.class);

    private final ProductStatusService productStatusService;

    @Autowired
    public ProductStatusController(ProductStatusService productStatusService) {
        this.productStatusService = productStatusService;
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createProductStatus(@Valid @RequestBody ProductStatusDto productStatusDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to create a new product status: {}", auth.getName(), productStatusDto.getName());
        
        return productStatusService.createProductStatus(productStatusDto);
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateProductStatus(@Valid @RequestBody ProductStatusDto productStatusDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to update product status with id {}", auth.getName(), productStatusDto.getId());
        
        return productStatusService.updateProductStatus(productStatusDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getProductStatusById(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve product status with id {}", auth.getName(), id);
        
        return productStatusService.getProductStatusById(id);
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getAllProductStatuses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all product statuses", auth.getName());
        
        return productStatusService.getAllProductStatuses();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteProductStatus(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to delete product status with id {}", auth.getName(), id);
        
        return productStatusService.deleteProductStatus(id);
    }
}

