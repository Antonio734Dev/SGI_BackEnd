package com.labMetricas.LabMetricas.movement.model;

import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class ProductStockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "UUID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "stock_catalogue_id", nullable = false)
    private StockCatalogue stockCatalogue;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "VARCHAR(20)")
    private TipoMovimiento tipo;

    @Column(name = "cantidad", columnDefinition = "DECIMAL(10,2)", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "motivo", columnDefinition = "VARCHAR(255)", length = 255)
    private String motivo;

    @Column(name = "referencia", columnDefinition = "VARCHAR(100)", length = 100)
    private String referencia;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

