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

    // Campos calculados desde los productos asociados
    private Integer totalProductos; // Conteo total de productos referenciados a este stock

    private Boolean status; // Estado activo/inactivo

    private UUID createdByUserId;

    private String createdByUserName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}

