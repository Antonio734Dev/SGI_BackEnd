package com.labMetricas.LabMetricas.products_stock_movements.model;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.products_ingreso_material.model.Products_ingreso_material;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products_stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Products_stock_movements {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products_ingreso_material product;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "ENUM('entrada', 'salida', 'ajuste')")
    private TipoMovimiento tipo;

    @Column(name = "cantidad", columnDefinition = "DECIMAL(10,2)", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "motivo", columnDefinition = "VARCHAR(255)", length = 255)
    private String motivo;

    @Column(name = "referencia", columnDefinition = "VARCHAR(100)", length = 100)
    private String referencia;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
