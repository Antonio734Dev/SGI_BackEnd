package com.labMetricas.LabMetricas.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.movement.model.ProductStockMovement;
import com.labMetricas.LabMetricas.product.model.Product;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_catalogue",
    indexes = {
        @Index(name = "stock_catalogue_name_index", columnList = "name"),
        @Index(name = "stock_catalogue_sku_index", columnList = "sku")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockCatalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)", nullable = false, length = 255)
    private String name;

    @Column(name = "sku", columnDefinition = "VARCHAR(100)", unique = true, length = 100)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", columnDefinition = "UUID")
    private User createdByUser;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "stockCatalogue", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "stockCatalogue")
    @JsonIgnore
    private List<ProductStockMovement> stockMovements = new ArrayList<>();
}

