package com.labMetricas.LabMetricas.status.service;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.status.model.ProductStatus;
import com.labMetricas.LabMetricas.status.model.dto.ProductStatusDto;
import com.labMetricas.LabMetricas.status.repository.ProductStatusRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductStatusService {
    private static final Logger logger = LoggerFactory.getLogger(ProductStatusService.class);

    @Autowired
    private ProductStatusRepository productStatusRepository;

    @Autowired
    private UserRepository userRepository;

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
                currentUser = userRepository.findByEmail(email).orElse(null);
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
    public ResponseEntity<ResponseObject> createProductStatus(ProductStatusDto productStatusDto) {
        try {
            // Check if name already exists
            if (productStatusRepository.existsByName(productStatusDto.getName())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Product status name already exists", null, TypeResponse.ERROR)
                );
            }

            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Create new ProductStatus
            ProductStatus productStatus = new ProductStatus();
            productStatus.setName(productStatusDto.getName());
            productStatus.setDescription(productStatusDto.getDescription());
            productStatus.setCreatedByUser(currentUser);
            productStatus.setCreatedAt(LocalDateTime.now());
            productStatus.setUpdatedAt(LocalDateTime.now());

            ProductStatus savedProductStatus = productStatusRepository.save(productStatus);
            ProductStatusDto responseDto = convertToDto(savedProductStatus);

            logger.info("Product status created successfully: {}", savedProductStatus.getName());

            // Registrar log de auditoría
            createAuditLog(String.format("Se agregó un estado de producto: %s", savedProductStatus.getName()));

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Product status created successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error creating product status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating product status: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateProductStatus(ProductStatusDto productStatusDto) {
        try {
            // Find existing product status
            ProductStatus existingProductStatus = productStatusRepository.findById(productStatusDto.getId())
                .orElseThrow(() -> new RuntimeException("Product status not found"));

            // Check if name is being changed and if new name already exists
            if (!existingProductStatus.getName().equals(productStatusDto.getName()) &&
                productStatusRepository.existsByName(productStatusDto.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject("Product status name already exists", null, TypeResponse.ERROR)
                );
            }

            // Update product status details
            existingProductStatus.setName(productStatusDto.getName());
            existingProductStatus.setDescription(productStatusDto.getDescription());
            existingProductStatus.setUpdatedAt(LocalDateTime.now());

            // Save updated product status
            ProductStatus updatedProductStatus = productStatusRepository.save(existingProductStatus);

            // Convert to DTO for response
            ProductStatusDto responseDto = convertToDto(updatedProductStatus);

            logger.info("Product status updated successfully: {}", updatedProductStatus.getName());

            // Registrar log de auditoría
            createAuditLog(String.format("Se actualizó el estado de producto: %s (ID: %d)", 
                updatedProductStatus.getName(), updatedProductStatus.getId()));

            return ResponseEntity.ok(
                new ResponseObject("Product status updated successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error updating product status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating product status: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getProductStatusById(Integer id) {
        try {
            ProductStatus productStatus = productStatusRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Product status not found"));

            return ResponseEntity.ok(
                new ResponseObject("Product status retrieved successfully", convertToDto(productStatus), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving product status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Product status not found", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getAllProductStatuses() {
        try {
            List<ProductStatusDto> productStatuses = productStatusRepository.findByDeletedAtIsNullOrderByNameAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                new ResponseObject("Product statuses retrieved successfully", productStatuses, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving product statuses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving product statuses", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteProductStatus(Integer id) {
        try {
            ProductStatus productStatus = productStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product status not found"));

            // Check if already deleted
            if (productStatus.getDeletedAt() != null) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Product status already deleted", null, TypeResponse.ERROR)
                );
            }

            // Soft delete
            productStatus.setDeletedAt(LocalDateTime.now());
            productStatus.setUpdatedAt(LocalDateTime.now());
            productStatusRepository.save(productStatus);

            logger.info("Product status deleted successfully: {}", productStatus.getName());

            return ResponseEntity.ok(
                new ResponseObject("Product status deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error deleting product status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting product status: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    // Helper method to convert ProductStatus to ProductStatusDto
    private ProductStatusDto convertToDto(ProductStatus productStatus) {
        ProductStatusDto dto = new ProductStatusDto();
        dto.setId(productStatus.getId());
        dto.setName(productStatus.getName());
        dto.setDescription(productStatus.getDescription());
        dto.setCreatedAt(productStatus.getCreatedAt());
        dto.setUpdatedAt(productStatus.getUpdatedAt());
        dto.setDeletedAt(productStatus.getDeletedAt());
        
        if (productStatus.getCreatedByUser() != null) {
            dto.setCreatedByUserId(productStatus.getCreatedByUser().getId());
            dto.setCreatedByUserName(productStatus.getCreatedByUser().getName());
        }
        
        return dto;
    }
}

