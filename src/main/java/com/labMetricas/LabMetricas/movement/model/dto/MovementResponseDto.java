package com.labMetricas.LabMetricas.movement.model.dto;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovementResponseDto {
    private Integer id;
    
    // Información del movimiento
    private TipoMovimiento tipo;
    private BigDecimal cantidad;
    private String motivo;
    private String referencia;
    
    // Información del catálogo (nombres legibles)
    private Integer stockCatalogueId;
    private String stockCatalogueName;
    private String stockCatalogueSku;
    
    // Información del usuario (nombres legibles)
    private UUID userId;
    private String userName;
    private String userEmail;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

