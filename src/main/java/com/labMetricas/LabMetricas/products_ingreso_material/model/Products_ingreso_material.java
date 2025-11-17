package com.labMetricas.LabMetricas.products_ingreso_material.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.enums.EstatusMaterial;
import com.labMetricas.LabMetricas.products_stock.model.Products_stock;
import com.labMetricas.LabMetricas.products_stock_movements.model.Products_stock_movements;
import com.labMetricas.LabMetricas.qr_code.model.Qr_code;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products_ingreso_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Products_ingreso_material {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", columnDefinition = "BINARY(16)")
    private User createdByUser;

    @Column(name = "material", columnDefinition = "VARCHAR(200)", length = 200)
    private String material;

    @Column(name = "nombre", columnDefinition = "VARCHAR(200)", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "codigo", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String codigo;

    @Column(name = "lote", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, columnDefinition = "ENUM('aprobado', 'rechazado', 'cuarentena', 'otro') DEFAULT 'otro'")
    private EstatusMaterial estatus = EstatusMaterial.otro;

    @Column(name = "caducidad", nullable = false)
    private LocalDate caducidad;

    @Column(name = "reanalisis")
    private LocalDate reanalisis;

    @Column(name = "cantidad_texto", columnDefinition = "VARCHAR(255)", nullable = false, length = 255)
    private String cantidadTexto;

    @Column(name = "total_envases", nullable = false)
    private Integer totalEnvases;

    @Column(name = "envases_rechazados", columnDefinition = "INT DEFAULT 0")
    private Integer envasesRechazados = 0;

    @Column(name = "envases_aprobados", columnDefinition = "INT DEFAULT 0")
    private Integer envasesAprobados = 0;

    @Column(name = "creado_en", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime actualizadoEn;

    // Relaciones
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Qr_code> qrCodes;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Products_stock productsStock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Products_stock_movements> stockMovements;
}
