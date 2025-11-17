package com.labMetricas.LabMetricas.products_stock.model;

import com.labMetricas.LabMetricas.products_ingreso_material.model.Products_ingreso_material;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Products_stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Products_ingreso_material product;

    @Column(name = "stock_actual", columnDefinition = "DECIMAL(10,2)", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", columnDefinition = "DECIMAL(10,2)", precision = 10, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "stock_maximo", columnDefinition = "DECIMAL(10,2)", precision = 10, scale = 2)
    private BigDecimal stockMaximo = BigDecimal.ZERO;

    @Column(name = "unidad", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String unidad;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
