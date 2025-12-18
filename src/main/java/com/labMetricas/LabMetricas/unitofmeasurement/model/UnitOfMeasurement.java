package com.labMetricas.LabMetricas.unitofmeasurement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.product.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "units_of_measurement",
    indexes = {
        @Index(name = "unit_of_measurement_name_index", columnList = "name", unique = true),
        @Index(name = "unit_of_measurement_code_index", columnList = "code", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String name; // Ejemplo: "Kilogramo", "Litro"

    @Column(name = "code", columnDefinition = "VARCHAR(10)", nullable = false, unique = true, length = 10)
    private String code; // Ejemplo: "KG", "L", "G", "ML"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Relación con unidad padre (para sub-unidades)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id")
    @JsonIgnore
    private UnitOfMeasurement parentUnit;

    // Relación con unidades hijas (sub-unidades)
    @OneToMany(mappedBy = "parentUnit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UnitOfMeasurement> subUnits = new ArrayList<>();

    // Relación con productos
    @OneToMany(mappedBy = "unitOfMeasurement")
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public UnitOfMeasurement(String name, String code) {
        this.name = name;
        this.code = code;
    }
}

