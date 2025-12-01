package com.labMetricas.LabMetricas.product.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Integer id;
    
    // Información del producto (primero)
    private String nombre;
    private String lote; // Lote interno
    private String loteProveedor;
    private String fabricante;
    private String distribuidor;
    private String codigo;
    private LocalDate fecha;
    private LocalDate caducidad;
    private LocalDate reanalisis;
    
    // Información del estado (nombres legibles)
    private Integer productStatusId;
    private String productStatusName;
    private String productStatusDescription;
    
    // Información del QR
    private Integer qrCodeId;
    private String qrHash;
    
    // Información del creador
    private UUID createdByUserId;
    private String createdByUserName;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información del stock (después del producto)
    private Integer stockCatalogueId;
    private String stockCatalogueName;
    private String stockCatalogueSku;
    private String stockCatalogueUnidad;
    
    // Métricas de stock
    private Integer cantidadTotal; // stock_cantidad (máximo acumulado)
    private Integer descuentos; // Productos con estado "terminado"
    private Integer cantidadSobrante; // cantidadTotal - descuentos
    private Integer envasesRechazados;
    private Integer envasesAprobados;
}

