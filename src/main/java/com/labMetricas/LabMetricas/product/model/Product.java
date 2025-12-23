package com.labMetricas.LabMetricas.product.model;

import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.qrcode.model.QrCode;
import com.labMetricas.LabMetricas.status.model.ProductStatus;
import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;

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

import java.math.BigDecimal;
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
    @JoinColumn(name = "created_by_user_id", columnDefinition = "UUID")
    private User createdByUser;

    @ManyToOne
    @JoinColumn(name = "warehouse_type_id")
    private WarehouseType warehouseType;

    @ManyToOne
    @JoinColumn(name = "unit_of_measurement_id")
    private UnitOfMeasurement unitOfMeasurement;

    @Column(name = "nombre", columnDefinition = "VARCHAR(200)", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha", columnDefinition = "DATE", nullable = false)
    private LocalDate fecha;

    @Column(name = "muestreo", columnDefinition = "DATE")
    private LocalDate fechaMuestreo;

    @Column(name = "codigo", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String codigo;

    @Column(name = "codigo_producto", columnDefinition = "VARCHAR(50)", length = 50)
    private String codigoProducto;

    @Column(name = "lote", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String lote;

    @Column(name = "lote_proveedor", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String loteProveedor;

    @Column(name = "fabricante", columnDefinition = "VARCHAR(200)", length = 200)
    private String fabricante;

    @Column(name = "distribuidor", columnDefinition = "VARCHAR(200)", length = 200)
    private String distribuidor;

    @Column(name = "numero_analisis", columnDefinition = "VARCHAR(50)", length = 50)
    private String numeroAnalisis;

    @Column(name = "caducidad", columnDefinition = "DATE")
    private LocalDate caducidad;

    @Column(name = "reanalisis", columnDefinition = "DATE")
    private LocalDate reanalisis;

    /*
     * Cantidad sobrante calculada: cantidadTotal - descuentos (resultado guardado).
     */
    //@Column(name = "cantidad_sobrante", columnDefinition = "DECIMAL(15,4)", precision = 15, scale = 4)
    //private BigDecimal cantidadSobrante;

    /**
     * Total sobrante calculado: acumulado de lo que queda (cantidadTotal - descuentos) con mayor precisión.
     */
    //@Column(name = "total_sobrante", columnDefinition = "DECIMAL(15,4)", precision = 15, scale = 4)
    //private BigDecimal totalSobrante;

    @Column(name = "numero_contenedores", columnDefinition = "INT", nullable = false)
    private Integer numeroContenedores;

    /**
     * Cantidad total de piezas del producto (número de piezas).
     */
    @Column(name = "cantidad_total", columnDefinition = "INT")
    private Integer cantidadTotal;

    /**
     * Descuentos aplicados al producto (ajuste de piezas).
     */
    @Column(name = "descuentos", columnDefinition = "INT")
    private Integer descuentos;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

