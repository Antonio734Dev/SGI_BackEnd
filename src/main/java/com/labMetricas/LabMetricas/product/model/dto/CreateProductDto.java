package com.labMetricas.LabMetricas.product.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotBlank(message = "Lote is required")
    @Size(max = 100, message = "Lote must be less than 100 characters")
    private String lote;

    @NotNull(message = "Fecha ingreso is required")
    private LocalDate fechaIngreso;

    @NotNull(message = "Fecha caducidad is required")
    private LocalDate fechaCaducidad;

    @NotNull(message = "Cantidad texto is required")
    @Positive(message = "Cantidad texto must be positive")
    private Integer cantidadTexto;

    @NotNull(message = "Total envases is required")
    @Positive(message = "Total envases must be positive")
    private Integer totalEnvases;
}

