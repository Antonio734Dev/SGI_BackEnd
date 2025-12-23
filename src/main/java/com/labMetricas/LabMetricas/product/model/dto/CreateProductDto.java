package com.labMetricas.LabMetricas.product.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateProductDto {
    
    @NotNull(message = "Stock catalogue ID is required")
    private Integer stockCatalogueId;

    @NotNull(message = "Product status ID is required")
    private Integer productStatusId;

    private Integer warehouseTypeId;

    private Integer unitOfMeasurementId;

    @NotBlank(message = "Nombre is required")
    @Size(max = 200, message = "Nombre must be less than 200 characters")
    private String nombre;

    @NotBlank(message = "Lote is required")
    @Size(max = 100, message = "Lote must be less than 100 characters")
    private String lote;

    @NotBlank(message = "Lote proveedor is required")
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

    @NotNull(message = "Fecha ingreso is required")
    private LocalDate fechaIngreso;

    private LocalDate fechaCaducidad;

    private LocalDate reanalisis;

    private LocalDate fechaMuestreo;

    @NotNull(message = "Número contenedores is required")
    @Positive(message = "Número contenedores must be positive")
    private Integer numeroContenedores;

    /**
     * Cantidad total de piezas del producto (número de piezas).
     * Se crea al momento de hacer la request.
     */
    @NotNull(message = "Cantidad total is required")
    @Positive(message = "Cantidad total must be positive")
    private Integer cantidadTotal;

    /**
     * Descuentos aplicados al producto (ajuste de piezas).
     * Se puede editar después sobre la cantidad total.
     */
    private Integer descuentos;

    //@NotNull(message = "Cantidad is required")
    //@Positive(message = "Cantidad must be positive")
    //private Integer cantidad;
}

