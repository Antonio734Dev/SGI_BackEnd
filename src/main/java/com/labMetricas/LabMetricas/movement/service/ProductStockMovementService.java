package com.labMetricas.LabMetricas.movement.service;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.movement.model.ProductStockMovement;
import com.labMetricas.LabMetricas.movement.model.dto.MovementResponseDto;
import com.labMetricas.LabMetricas.movement.repository.ProductStockMovementRepository;
import com.labMetricas.LabMetricas.util.PageResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ProductStockMovementService {
    private static final Logger logger = LoggerFactory.getLogger(ProductStockMovementService.class);

    @Autowired
    private ProductStockMovementRepository productStockMovementRepository;

    /**
     * Obtiene el historial de movimientos con filtros opcionales
     */
    public ResponseEntity<ResponseObject> getStockMovements(
            int page, 
            int size, 
            Integer stockCatalogueId, 
            TipoMovimiento tipoMovimiento,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ProductStockMovement> movementsPage;

            // Convertir LocalDate a LocalDateTime para la búsqueda
            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            
            if (fechaInicio != null) {
                startDateTime = fechaInicio.atStartOfDay();
            }
            if (fechaFin != null) {
                endDateTime = fechaFin.atTime(LocalTime.MAX);
            }

            // Aplicar filtros combinados
            if (stockCatalogueId != null && tipoMovimiento != null && startDateTime != null && endDateTime != null) {
                // Todos los filtros
                movementsPage = productStockMovementRepository.findByStockCatalogueIdAndTipoAndCreatedAtBetweenAndDeletedAtIsNull(
                    stockCatalogueId, tipoMovimiento, startDateTime, endDateTime, pageable);
            } else if (stockCatalogueId != null && tipoMovimiento != null) {
                // Catálogo y tipo
                movementsPage = productStockMovementRepository.findByStockCatalogueIdAndTipoAndDeletedAtIsNull(
                    stockCatalogueId, tipoMovimiento, pageable);
            } else if (stockCatalogueId != null && startDateTime != null && endDateTime != null) {
                // Catálogo y rango de fechas
                movementsPage = productStockMovementRepository.findByStockCatalogueIdAndCreatedAtBetweenAndDeletedAtIsNull(
                    stockCatalogueId, startDateTime, endDateTime, pageable);
            } else if (tipoMovimiento != null && startDateTime != null && endDateTime != null) {
                // Tipo y rango de fechas
                movementsPage = productStockMovementRepository.findByTipoAndCreatedAtBetweenAndDeletedAtIsNull(
                    tipoMovimiento, startDateTime, endDateTime, pageable);
            } else if (stockCatalogueId != null) {
                // Solo catálogo
                movementsPage = productStockMovementRepository.findByStockCatalogueIdAndDeletedAtIsNull(
                    stockCatalogueId, pageable);
            } else if (tipoMovimiento != null) {
                // Solo tipo
                movementsPage = productStockMovementRepository.findByTipoAndDeletedAtIsNull(
                    tipoMovimiento, pageable);
            } else if (startDateTime != null && endDateTime != null) {
                // Solo rango de fechas
                movementsPage = productStockMovementRepository.findByCreatedAtBetweenAndDeletedAtIsNull(
                    startDateTime, endDateTime, pageable);
            } else {
                // Sin filtros
                movementsPage = productStockMovementRepository.findByDeletedAtIsNull(pageable);
            }

            // Convertir a DTOs
            PageResponse<MovementResponseDto> pageResponse = new PageResponse<>(
                movementsPage.map(this::convertToResponseDto)
            );

            return ResponseEntity.ok(
                new ResponseObject("Stock movements retrieved successfully", pageResponse, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving stock movements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving stock movements", null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Convierte ProductStockMovement a MovementResponseDto con nombres legibles
     */
    private MovementResponseDto convertToResponseDto(ProductStockMovement movement) {
        MovementResponseDto dto = new MovementResponseDto();
        dto.setId(movement.getId());
        dto.setTipo(movement.getTipo());
        dto.setCantidad(movement.getCantidad());
        dto.setMotivo(movement.getMotivo());
        dto.setReferencia(movement.getReferencia());
        dto.setCreatedAt(movement.getCreatedAt());
        dto.setUpdatedAt(movement.getUpdatedAt());

        // Información del catálogo (nombres legibles)
        if (movement.getStockCatalogue() != null) {
            dto.setStockCatalogueId(movement.getStockCatalogue().getId());
            dto.setStockCatalogueName(movement.getStockCatalogue().getName());
            dto.setStockCatalogueSku(movement.getStockCatalogue().getSku());
        }

        // Información del usuario (nombres legibles)
        if (movement.getUser() != null) {
            dto.setUserId(movement.getUser().getId());
            dto.setUserName(movement.getUser().getName());
            dto.setUserEmail(movement.getUser().getEmail());
        }

        return dto;
    }
}

