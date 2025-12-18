package com.labMetricas.LabMetricas.warehousetype.model;

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
@Table(name = "warehouse_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WarehouseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code; // MPS, MES, MEM, MPM

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Nombre descriptivo del almacén

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "warehouseType")
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    public WarehouseType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}

