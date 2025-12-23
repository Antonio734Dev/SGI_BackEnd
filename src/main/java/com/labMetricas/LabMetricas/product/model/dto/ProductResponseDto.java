package com.labMetricas.LabMetricas.product.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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
    private LocalDate fechaMuestreo;
    private String fabricante;
    private String distribuidor;
    private String codigo;
    private String codigoProducto;
    private String numeroAnalisis;
    private LocalDate fecha;
    private LocalDate caducidad;
    private LocalDate reanalisis;
    //private BigDecimal cantidadSobrante;
    //private BigDecimal totalSobrante;
    private Integer numeroContenedores;
    
    /**
     * Cantidad total de piezas del producto (número de piezas).
     */
    private Integer cantidadTotal;
    
    /**
     * Descuentos aplicados al producto (ajuste de piezas).
     */
    private Integer descuentos;
    
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
    
    // Información del warehouse type
    private Integer warehouseTypeId;
    private String warehouseTypeCode;
    private String warehouseTypeName;
    
    // Información de unidad de medida
    private Integer unitOfMeasurementId;
    private String unitOfMeasurementName;
    private String unitOfMeasurementCode;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información del stock (después del producto)
    private Integer stockCatalogueId;
    private String stockCatalogueName;
    private String stockCatalogueSku;
}

