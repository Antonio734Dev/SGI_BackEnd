package com.labMetricas.LabMetricas.inventory.product.model;

import com.labMetricas.LabMetricas.inventory.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.inventory.qrcode.model.QrCode;
import com.labMetricas.LabMetricas.inventory.status.model.ProductStatus;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "stock_catalogue_id", nullable = false)
    private StockCatalogue stockCatalogue;

    @ManyToOne
    @JoinColumn(name = "product_status_id", nullable = false)
    private ProductStatus productStatus;

    @OneToOne
    @JoinColumn(name = "qr_code_id", unique = true)
    private QrCode qrCode;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", columnDefinition = "BINARY(16)")
    private User createdByUser;

    @Column(name = "material", length = 200)
    private String material;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "lote", nullable = false, length = 100)
    private String lote;

    @Column(name = "caducidad", nullable = false)
    private LocalDate caducidad;

    @Column(name = "reanalisis")
    private LocalDate reanalisis;

    @Column(name = "cantidad_texto", nullable = false, length = 255)
    private String cantidadTexto;

    @Column(name = "total_envases", nullable = false)
    private Integer totalEnvases;

    @Column(name = "envases_rechazados", nullable = false)
    private Integer envasesRechazados = 0;

    @Column(name = "envases_aprobados", nullable = false)
    private Integer envasesAprobados = 0;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

