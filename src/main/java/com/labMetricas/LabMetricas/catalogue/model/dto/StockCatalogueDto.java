package com.labMetricas.LabMetricas.catalogue.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class StockCatalogueDto {
    private Integer id;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 100, message = "SKU must be less than 100 characters")
    private String sku;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotBlank(message = "Unidad is required")
    @Size(max = 50, message = "Unidad must be less than 50 characters")
    private String unidad;

    // Stock actual is read-only, should not be set in DTOs for create/update
    private BigDecimal stockActual;

    private Integer stockCantidad;

    private Integer cantidad;

    private Integer envasesRechazados;

    private Integer envasesAprobados;

    // Campos calculados
    private Integer descuentos; // Productos con estado "terminado"
    private Integer cantidadSobrante; // stock_cantidad - descuentos

    private UUID createdByUserId;

    private String createdByUserName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

