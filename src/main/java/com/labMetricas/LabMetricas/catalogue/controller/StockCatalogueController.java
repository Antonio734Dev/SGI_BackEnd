package com.labMetricas.LabMetricas.catalogue.controller;

import com.labMetricas.LabMetricas.catalogue.model.dto.StockCatalogueDto;
import com.labMetricas.LabMetricas.catalogue.service.StockCatalogueService;
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
@RequestMapping("/api/stock-catalogues")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockCatalogueController {
    private static final Logger logger = LoggerFactory.getLogger(StockCatalogueController.class);

    private final StockCatalogueService stockCatalogueService;

    @Autowired
    public StockCatalogueController(StockCatalogueService stockCatalogueService) {
        this.stockCatalogueService = stockCatalogueService;
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createStockCatalogue(@Valid @RequestBody StockCatalogueDto stockCatalogueDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to create a new stock catalogue: {}", auth.getName(), stockCatalogueDto.getName());
        
        return stockCatalogueService.createStockCatalogue(stockCatalogueDto);
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateStockCatalogue(@Valid @RequestBody StockCatalogueDto stockCatalogueDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to update stock catalogue with id {}", auth.getName(), stockCatalogueDto.getId());
        
        return stockCatalogueService.updateStockCatalogue(stockCatalogueDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getStockCatalogueById(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve stock catalogue with id {}", auth.getName(), id);
        
        return stockCatalogueService.getStockCatalogueById(id);
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getAllStockCatalogues(
            @RequestParam(required = false) String search) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all stock catalogues - search: {}", 
            auth.getName(), search);
        
        return stockCatalogueService.getAllStockCatalogues(search);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteStockCatalogue(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to deactivate stock catalogue with id {}", auth.getName(), id);
        
        return stockCatalogueService.deleteStockCatalogue(id);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ResponseObject> toggleStockCatalogueStatus(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to toggle status of stock catalogue with id {}", auth.getName(), id);
        
        return stockCatalogueService.toggleStockCatalogueStatus(id);
    }
}

