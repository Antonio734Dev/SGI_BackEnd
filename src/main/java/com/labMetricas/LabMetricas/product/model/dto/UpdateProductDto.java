package com.labMetricas.LabMetricas.product.model.dto;

import jakarta.validation.constraints.NotNull;
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
public class UpdateProductDto {
    
    @NotNull(message = "Product ID is required")
    private Integer id;

    private Integer stockCatalogueId;

    private Integer productStatusId; // Permite cambiar el estado

    @Size(max = 100, message = "Lote must be less than 100 characters")
    private String lote;

    @Size(max = 100, message = "Lote proveedor must be less than 100 characters")
    private String loteProveedor;

    @Size(max = 200, message = "Fabricante must be less than 200 characters")
    private String fabricante;

    @Size(max = 200, message = "Distribuidor must be less than 200 characters")
    private String distribuidor;

    private LocalDate fechaIngreso;

    private LocalDate fechaCaducidad;

    private LocalDate reanalisis;
}

