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
import com.labMetricas.LabMetricas.util.PageResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            stockCatalogue.setUnidad(stockCatalogueDto.getUnidad());
            // IMPORTANT: stock_actual is always initialized to 0
            stockCatalogue.setStockActual(BigDecimal.ZERO);
            stockCatalogue.setStockCantidad(
                stockCatalogueDto.getStockCantidad() != null ? stockCatalogueDto.getStockCantidad() : 0);
            stockCatalogue.setCantidad(
                stockCatalogueDto.getCantidad() != null ? stockCatalogueDto.getCantidad() : 0);
            stockCatalogue.setEnvasesRechazados(
                stockCatalogueDto.getEnvasesRechazados() != null ? stockCatalogueDto.getEnvasesRechazados() : 0);
            stockCatalogue.setEnvasesAprobados(
                stockCatalogueDto.getEnvasesAprobados() != null ? stockCatalogueDto.getEnvasesAprobados() : 0);
            stockCatalogue.setCreatedByUser(currentUser);
            stockCatalogue.setCreatedAt(LocalDateTime.now());
            stockCatalogue.setUpdatedAt(LocalDateTime.now());

            StockCatalogue savedStockCatalogue = stockCatalogueRepository.save(stockCatalogue);
            StockCatalogueDto responseDto = convertToDto(savedStockCatalogue);

            logger.info("Stock catalogue created successfully: {}", savedStockCatalogue.getName());

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
            // IMPORTANT: stock_actual is NOT updated directly - it's only modified through movements
            existingStockCatalogue.setName(stockCatalogueDto.getName());
            existingStockCatalogue.setSku(stockCatalogueDto.getSku());
            existingStockCatalogue.setDescription(stockCatalogueDto.getDescription());
            existingStockCatalogue.setUnidad(stockCatalogueDto.getUnidad());
            if (stockCatalogueDto.getStockCantidad() != null) {
                existingStockCatalogue.setStockCantidad(stockCatalogueDto.getStockCantidad());
            }
            if (stockCatalogueDto.getCantidad() != null) {
                existingStockCatalogue.setCantidad(stockCatalogueDto.getCantidad());
            }
            if (stockCatalogueDto.getEnvasesRechazados() != null) {
                existingStockCatalogue.setEnvasesRechazados(stockCatalogueDto.getEnvasesRechazados());
            }
            if (stockCatalogueDto.getEnvasesAprobados() != null) {
                existingStockCatalogue.setEnvasesAprobados(stockCatalogueDto.getEnvasesAprobados());
            }
            existingStockCatalogue.setUpdatedAt(LocalDateTime.now());

            // Save updated stock catalogue
            StockCatalogue updatedStockCatalogue = stockCatalogueRepository.save(existingStockCatalogue);

            // Convert to DTO for response
            StockCatalogueDto responseDto = convertToDto(updatedStockCatalogue);

            logger.info("Stock catalogue updated successfully: {}", updatedStockCatalogue.getName());

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
            StockCatalogue stockCatalogue = stockCatalogueRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found"));

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

    public ResponseEntity<ResponseObject> getAllStockCatalogues(int page, int size, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<StockCatalogue> stockCataloguesPage;

            if (search != null && !search.trim().isEmpty()) {
                // Search by name
                stockCataloguesPage = stockCatalogueRepository.findByDeletedAtIsNullAndNameContainingIgnoreCase(search.trim(), pageable);
            } else {
                // Get all active
                stockCataloguesPage = stockCatalogueRepository.findByDeletedAtIsNull(pageable);
            }

            // Create paginated response
            PageResponse<StockCatalogueDto> pageResponse = new PageResponse<>(
                stockCataloguesPage.map(this::convertToDto)
            );

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogues retrieved successfully", pageResponse, TypeResponse.SUCCESS)
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
            StockCatalogue stockCatalogue = stockCatalogueRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found"));

            // Soft delete
            stockCatalogue.setDeletedAt(LocalDateTime.now());
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            stockCatalogueRepository.save(stockCatalogue);

            logger.info("Stock catalogue deleted successfully: {}", stockCatalogue.getName());

            return ResponseEntity.ok(
                new ResponseObject("Stock catalogue deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error deleting stock catalogue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting stock catalogue: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Calcula los descuentos (productos con estado "terminado") para un stock
     * Suma las cantidades de movimientos de entrada iniciales de productos terminados
     */
    private Integer calculateDescuentos(StockCatalogue stockCatalogue) {
        try {
            // Buscar productos con estado "terminado" para este stock
            var productosTerminados = productRepository.findByStockCatalogueIdAndProductStatusNameIgnoreCaseAndDeletedAtIsNull(
                stockCatalogue.getId(), "terminado"
            );

            if (productosTerminados.isEmpty()) {
                return 0;
            }

            // Sumar las cantidades de movimientos de entrada iniciales para estos productos
            int totalDescuentos = 0;
            for (Product product : productosTerminados) {
                // Buscar movimientos de entrada con referencia que contiene el lote
                var movimientos = productStockMovementRepository
                    .findByStockCatalogueIdAndDeletedAtIsNull(stockCatalogue.getId());
                
                for (var movimiento : movimientos) {
                    if (movimiento.getTipo() == TipoMovimiento.entrada && 
                        movimiento.getReferencia() != null &&
                        movimiento.getReferencia().contains("Lote " + product.getLote())) {
                        totalDescuentos += movimiento.getCantidad().intValue();
                        break; // Solo contar el movimiento inicial de cada producto
                    }
                }
            }

            return totalDescuentos;
        } catch (Exception e) {
            logger.warn("Error calculating descuentos for stock catalogue {}: {}", stockCatalogue.getId(), e.getMessage());
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
        dto.setUnidad(stockCatalogue.getUnidad());
        dto.setStockActual(stockCatalogue.getStockActual());
        dto.setStockCantidad(stockCatalogue.getStockCantidad());
        dto.setCantidad(stockCatalogue.getCantidad());
        dto.setEnvasesRechazados(stockCatalogue.getEnvasesRechazados());
        dto.setEnvasesAprobados(stockCatalogue.getEnvasesAprobados());
        
        // Calcular descuentos y cantidad sobrante
        Integer descuentos = calculateDescuentos(stockCatalogue);
        Integer cantidadTotal = stockCatalogue.getStockCantidad() != null ? stockCatalogue.getStockCantidad() : 0;
        Integer cantidadSobrante = Math.max(0, cantidadTotal - descuentos);
        
        dto.setDescuentos(descuentos);
        dto.setCantidadSobrante(cantidadSobrante);
        
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

