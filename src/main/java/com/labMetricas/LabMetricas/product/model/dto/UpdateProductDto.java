package com.labMetricas.LabMetricas.product.model.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDto {
    
    @NotNull(message = "Product ID is required")
    private Integer id;

    private Integer stockCatalogueId;

    private Integer productStatusId; // Permite cambiar el estado

    private Integer warehouseTypeId;

    private Integer unitOfMeasurementId;

    @Size(max = 200, message = "Nombre must be less than 200 characters")
    private String nombre;

    @Size(max = 100, message = "Lote must be less than 100 characters")
    private String lote;

    @Size(max = 100, message = "Lote proveedor must be less than 100 characters")
    private String loteProveedor;

    @Size(max = 200, message = "Fabricante must be less than 200 characters")
    private String fabricante;

    @Size(max = 200, message = "Distribuidor must be less than 200 characters")
    private String distribuidor;

    @Size(max = 50, message = "Código producto must be less than 50 characters")
    private String codigoProducto;

    @Size(max = 50, message = "Número análisis must be less than 50 characters")
    private String numeroAnalisis;

    private LocalDate fechaIngreso;

    private LocalDate fechaCaducidad;

    private LocalDate fechaMuestreo;

    private LocalDate reanalisis;

    private Integer numeroContenedores;

    /**
     * Cantidad total de piezas del producto (número de piezas).
     */
    private Integer cantidadTotal;

    /**
     * Descuentos aplicados al producto (ajuste de piezas).
     * Al editar descuentos, se recalcularán automáticamente cantidadSobrante y totalSobrante.
     */
    private Integer descuentos;
}

