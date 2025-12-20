package com.labMetricas.LabMetricas.catalogue.service;

import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.catalogue.model.dto.StockCatalogueDto;
import com.labMetricas.LabMetricas.catalogue.repository.StockCatalogueRepository;
import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.movement.repository.ProductStockMovementRepository;
import com.labMetricas.LabMetricas.product.model.Product;
import com.labMetricas.LabMetricas.product.repository.ProductRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StockCatalogueService {
    private static final Logger logger = LoggerFactory.getLogger(StockCatalogueService.class);

    @Autowired
    private StockCatalogueRepository stockCatalogueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockMovementRepository productStockMovementRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Método helper para crear logs de auditoría
    private void createAuditLog(String action) {
        try {
            // Obtener el usuario actual autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                currentUser = userOpt.orElse(null);
            }
            
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setUser(currentUser);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);
            
            logger.debug("Audit log created: {} by user: {}", action, 
                currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
        } catch (Exception e) {
            logger.error("Error creating audit log: {}", action, e);
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> createStockCatalogue(StockCatalogueDto stockCatalogueDto) {
        try {
            // Check if SKU already exists (if provided)
            if (stockCatalogueDto.getSku() != null && !stockCatalogueDto.getSku().isEmpty() &&
                stockCatalogueRepository.existsBySku(stockCatalogueDto.getSku())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("SKU already exists", null, TypeResponse.ERROR)
                );
            }

            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Create new StockCatalogue
            StockCatalogue stockCatalogue = new StockCatalogue();
            stockCatalogue.setName(stockCatalogueDto.getName());
            stockCatalogue.setSku(stockCatalogueDto.getSku());
            stockCatalogue.setDescription(stockCatalogueDto.getDescription());
            stockCatalogue.setStatus(stockCatalogueDto.getStatus() != null ? stockCatalogueDto.getStatus() : true);
            stockCatalogue.setCreatedByUser(currentUser);
            stockCatalogue.setCreatedAt(LocalDateTime.now());
            stockCatalogue.setUpdatedAt(LocalDateTime.now());

            StockCatalogue savedStockCatalogue = stockCatalogueRepository.save(stockCatalogue);
            StockCatalogueDto responseDto = convertToDto(savedStockCatalogue);

            logger.info("Stock catalogue created successfully: {}", savedStockCatalogue.getName());

            // Registrar log de auditoría
            createAuditLog(String.format("Se agregó un catálogo de stock: %s (SKU: %s)", 
                savedStockCatalogue.getName(), 
                savedStockCatalogue.getSku() != null ? savedStockCatalogue.getSku() : "N/A"));

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Stock catalogue created successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error creating stock catalogue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating stock catalogue: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateStockCatalogue(StockCatalogueDto stockCatalogueDto) {
        try {
            // Find existing stock catalogue
            StockCatalogue existingStockCatalogue = stockCatalogueRepository.findById(stockCatalogueDto.getId())
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found"));

            // Check if SKU is being changed and if new SKU already exists
            if (stockCatalogueDto.getSku() != null && !stockCatalogueDto.getSku().isEmpty()) {
                if (existingStockCatalogue.getSku() == null || 
                    !existingStockCatalogue.getSku().equals(stockCatalogueDto.getSku())) {
                    if (stockCatalogueRepository.existsBySku(stockCatalogueDto.getSku())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            new ResponseObject("SKU already exists", null, TypeResponse.ERROR)
                        );
                    }
                }
            }

            // Update stock catalogue details
            existingStockCatalogue.setName(stockCatalogueDto.getName());
            existingStockCatalogue.setSku(stockCatalogueDto.getSku());
            existingStockCatalogue.setDescription(stockCatalogueDto.getDescription());
            if (stockCatalogueDto.getStatus() != null) {
                existingStockCatalogue.setStatus(stockCatalogueDto.getStatus());
            }
            existingStockCatalogue.setUpdatedAt(LocalDateTime.now());

            // Save updated stock catalogue
            StockCatalogue updatedStockCatalogue = stockCatalogueRepository.save(existingStockCatalogue);

            // Convert to DTO for response
            StockCatalogueDto responseDto = convertToDto(updatedStockCatalogue);

            logger.info("Stock catalogue updated successfully: {}", updatedStockCatalogue.getName());

            // Registrar log de auditoría
            createAuditLog(String.format("Se actualizó el catálogo de stock: %s (ID: %d)", 
                updatedStockCatalogue.getName(), updatedStockCatalogue.getId()));

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogue updated successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error updating stock catalogue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating stock catalogue: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getStockCatalogueById(Integer id) {
        try {
            StockCatalogue stockCatalogue = stockCatalogueRepository.findByIdAndStatusTrue(id)
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found or inactive"));

            // NO registrar log de auditoría para consultas (evitar spam)

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogue retrieved successfully", convertToDto(stockCatalogue), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving stock catalogue", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Stock catalogue not found", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getAllStockCatalogues(String search) {
        try {
            java.util.List<StockCatalogue> stockCatalogues;

            if (search != null && !search.trim().isEmpty()) {
                // Search by name (all statuses)
                stockCatalogues = stockCatalogueRepository.findAll().stream()
                    .filter(sc -> sc.getName() != null && 
                           sc.getName().toLowerCase().contains(search.trim().toLowerCase()))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(java.util.stream.Collectors.toList());
            } else {
                // Get all (all statuses, ordered by createdAt DESC)
                stockCatalogues = stockCatalogueRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Convert to DTOs
            java.util.List<StockCatalogueDto> dtos = stockCatalogues.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList());

            // NO registrar log de auditoría para consultas (evitar spam)

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogues retrieved successfully", dtos, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving stock catalogues", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving stock catalogues", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteStockCatalogue(Integer id) {
        try {
            StockCatalogue stockCatalogue = stockCatalogueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found"));

            // Change status to inactive instead of soft delete
            stockCatalogue.setStatus(false);
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            stockCatalogueRepository.save(stockCatalogue);

            logger.info("Stock catalogue status changed to inactive: {}", stockCatalogue.getName());

            // Registrar log de auditoría
            createAuditLog(String.format("Se eliminó el catálogo de stock: %s (ID: %d)", 
                stockCatalogue.getName(), stockCatalogue.getId()));

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogue status changed to inactive successfully", convertToDto(stockCatalogue), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error changing stock catalogue status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error changing stock catalogue status: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> toggleStockCatalogueStatus(Integer id) {
        try {
            StockCatalogue stockCatalogue = stockCatalogueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found"));

            boolean oldStatus = stockCatalogue.getStatus();
            stockCatalogue.setStatus(!stockCatalogue.getStatus());
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            StockCatalogue updatedStockCatalogue = stockCatalogueRepository.save(stockCatalogue);

            String statusMessage = updatedStockCatalogue.getStatus() ? "activated" : "deactivated";
            logger.info("Stock catalogue {} status changed from {} to {}", 
                updatedStockCatalogue.getName(), oldStatus, updatedStockCatalogue.getStatus());

            // Registrar log de auditoría
            String statusText = updatedStockCatalogue.getStatus() ? "Activo" : "Inactivo";
            createAuditLog(String.format("Se cambió el estado del catálogo de stock: %s (ID: %d) a %s", 
                updatedStockCatalogue.getName(), updatedStockCatalogue.getId(), statusText));

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogue " + statusMessage + " successfully", 
                    convertToDto(updatedStockCatalogue), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error toggling stock catalogue status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error toggling stock catalogue status: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Calcula el total de productos referenciados a este stock
     */
    private Integer calculateTotalProductos(StockCatalogue stockCatalogue) {
        try {
            var productos = productRepository.findByStockCatalogueIdAndDeletedAtIsNull(stockCatalogue.getId());
            return productos.stream()
                .map(p -> p.getCantidadTotal() != null ? p.getCantidadTotal() : 0)
                .reduce(0, Integer::sum);
        } catch (Exception e) {
            logger.warn("Error calculating total productos for stock catalogue {}: {}", stockCatalogue.getId(), e.getMessage());
            return 0;
        }
    }

    // Helper method to convert StockCatalogue to StockCatalogueDto
    private StockCatalogueDto convertToDto(StockCatalogue stockCatalogue) {
        StockCatalogueDto dto = new StockCatalogueDto();
        dto.setId(stockCatalogue.getId());
        dto.setName(stockCatalogue.getName());
        dto.setSku(stockCatalogue.getSku());
        dto.setDescription(stockCatalogue.getDescription());
        
        // Calcular conteos desde los productos asociados
        Integer totalProductos = calculateTotalProductos(stockCatalogue);
        
        dto.setTotalProductos(totalProductos);
        dto.setStatus(stockCatalogue.getStatus());
        
        dto.setCreatedAt(stockCatalogue.getCreatedAt());
        dto.setUpdatedAt(stockCatalogue.getUpdatedAt());
        dto.setDeletedAt(stockCatalogue.getDeletedAt());
        
        if (stockCatalogue.getCreatedByUser() != null) {
            dto.setCreatedByUserId(stockCatalogue.getCreatedByUser().getId());
            dto.setCreatedByUserName(stockCatalogue.getCreatedByUser().getName());
        }
        
        return dto;
    }
}

