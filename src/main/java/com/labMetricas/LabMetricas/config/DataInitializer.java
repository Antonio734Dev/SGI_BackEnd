package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.catalogue.repository.StockCatalogueRepository;
import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.movement.model.ProductStockMovement;
import com.labMetricas.LabMetricas.movement.repository.ProductStockMovementRepository;
import com.labMetricas.LabMetricas.product.model.Product;
import com.labMetricas.LabMetricas.product.repository.ProductRepository;
import com.labMetricas.LabMetricas.qrcode.model.QrCode;
import com.labMetricas.LabMetricas.qrcode.repository.QrCodeRepository;
import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.role.repository.RoleRepository;
import com.labMetricas.LabMetricas.status.model.ProductStatus;
import com.labMetricas.LabMetricas.status.repository.ProductStatusRepository;
import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;
import com.labMetricas.LabMetricas.unitofmeasurement.repository.UnitOfMeasurementRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;
import com.labMetricas.LabMetricas.warehousetype.repository.WarehouseTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProductStatusRepository productStatusRepository;

    @Autowired
    private StockCatalogueRepository stockCatalogueRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private ProductStockMovementRepository productStockMovementRepository;

    @Autowired
    private WarehouseTypeRepository warehouseTypeRepository;

    @Autowired
    private UnitOfMeasurementRepository unitOfMeasurementRepository;

    @Override
    public void run(String... args) {
        try {
            // Initialize roles
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Create default users
            createDefaultUsers();

            // Initialize warehouse types
            initializeWarehouseTypes();

            // Initialize units of measurement
            initializeUnitsOfMeasurement();

            // Initialize product statuses
            initializeProductStatuses();

            // Initialize stock catalogues
            initializeStockCatalogues();

            // Initialize products with QR codes
            initializeProducts();

            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage(), e);
            throw e;
        }
    }


    private void createDefaultUsers() {
        // Administrador del Sistema
        createUserIfNotExists(
            "Antonio García González", 
            "antoniogarciagonzalez212@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Administrador del Sistema - Amador Casillas
        createUserIfNotExists(
            "Amador Casillas", 
            "amadorcasillasdr@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Administrador del Sistema - Creparits Dev
        createUserIfNotExists(
            "Creparits Dev", 
            "creparitsdev@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Supervisor
        createUserIfNotExists(
            "Supervisor UTEZ", 
            "20233tn106@utez.edu.mx", 
            "Admin2024#Secure", 
            "SUPERVISOR",
            "Supervisor de Laboratorio"
        );
    }

    private void createUserIfNotExists(
            String name, 
            String email, 
            String password, 
            String roleName,
            String position
    ) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPosition(position);
            user.setRole(roleRepository.findByName(roleName).orElseThrow());
            user.setEnabled(true);
            user.setStatus(true);

            // Optional: Add phone number if needed
            // user.setPhone("5551234567");

            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Created user: {} ({}) with role {}", name, email, roleName);
        }
    }

    private void createRoleIfNotFound(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            roleRepository.save(role);
            logger.info("Created role: {}", name);
        }
    }

    private void initializeWarehouseTypes() {
        // Check if table is empty
        if (warehouseTypeRepository.count() == 0) {
            // Crear los tipos de almacén: MPS, MES, MEM, MPM
            createWarehouseTypeIfNotExists("MPS", "Materia Prima de Suplementos", 
                "Almacén de materia prima de suplementos");
            createWarehouseTypeIfNotExists("MES", "Materia Prima de Suplementos - Especial", 
                "Almacén especial de materia prima de suplementos");
            createWarehouseTypeIfNotExists("MEM", "Materia Prima de Suplementos - Muestra", 
                "Almacén de muestras de materia prima de suplementos");
            createWarehouseTypeIfNotExists("MPM", "Materia Prima de Suplementos - Mantenimiento", 
                "Almacén de mantenimiento de materia prima de suplementos");

            logger.info("Warehouse types initialized successfully");
        } else {
            logger.info("Warehouse types already exist. Skipping initialization.");
        }
    }

    private void createWarehouseTypeIfNotExists(String code, String name, String description) {
        if (!warehouseTypeRepository.existsByCode(code)) {
            WarehouseType warehouseType = new WarehouseType();
            warehouseType.setCode(code);
            warehouseType.setName(name);
            warehouseType.setDescription(description);
            warehouseType.setCreatedAt(LocalDateTime.now());
            warehouseType.setUpdatedAt(LocalDateTime.now());
            warehouseTypeRepository.save(warehouseType);
            logger.info("Created warehouse type: {} - {}", code, name);
        }
    }

    private void initializeUnitsOfMeasurement() {
        // Check if table is empty
        if (unitOfMeasurementRepository.count() == 0) {
            // Crear unidades principales
            UnitOfMeasurement kg = createUnitIfNotExists("Kilogramo", "KG", "Unidad de peso en kilogramos", null);
            // Crear sub-unidades relacionadas con KG
            createUnitIfNotExists("Gramo", "G", "Unidad de peso en gramos", kg);
            createUnitIfNotExists("Mililitro", "ML", "Unidad de volumen en mililitros", kg);
            createUnitIfNotExists("Litro", "L", "Unidad de volumen en litros", kg);

            logger.info("Units of measurement initialized successfully");
        } else {
            logger.info("Units of measurement already exist. Skipping initialization.");
        }
    }

    private UnitOfMeasurement createUnitIfNotExists(String name, String code, String description, UnitOfMeasurement parentUnit) {
        if (!unitOfMeasurementRepository.existsByCode(code)) {
            UnitOfMeasurement unit = new UnitOfMeasurement();
            unit.setName(name);
            unit.setCode(code);
            unit.setDescription(description);
            unit.setParentUnit(parentUnit);
            unit.setCreatedAt(LocalDateTime.now());
            unit.setUpdatedAt(LocalDateTime.now());
            unitOfMeasurementRepository.save(unit);
            logger.info("Created unit of measurement: {} ({})", name, code);
            return unit;
        } else {
            return unitOfMeasurementRepository.findByCode(code).orElse(null);
        }
    }

    private void initializeProductStatuses() {
        // Check if table is empty
        if (productStatusRepository.count() == 0) {
            // Get the first admin user to set as created_by_user_id
            User adminUser = userRepository.findByEmail("antoniogarciagonzalez212@gmail.com")
                .orElseGet(() -> {
                    // If admin doesn't exist, get any user or create a default
                    return userRepository.findAll().stream()
                        .findFirst()
                        .orElse(null);
                });

            if (adminUser == null) {
                logger.warn("No user found to assign as creator for product statuses. Skipping initialization.");
                return;
            }

            // Create the 4 default product statuses
            createProductStatusIfNotExists("Sellado", "Producto cerrado de fábrica", adminUser);
            createProductStatusIfNotExists("Abierto", "Producto en uso", adminUser);
            createProductStatusIfNotExists("Terminado", "Producto agotado o vacio", adminUser);
            createProductStatusIfNotExists("Cuarentena", "Producto en revisión de calidad", adminUser);

            logger.info("Product statuses initialized successfully");
        } else {
            logger.info("Product statuses already exist. Skipping initialization.");
        }
    }

    private void createProductStatusIfNotExists(String name, String description, User createdByUser) {
        if (!productStatusRepository.existsByName(name)) {
            ProductStatus productStatus = new ProductStatus();
            productStatus.setName(name);
            productStatus.setDescription(description);
            productStatus.setCreatedByUser(createdByUser);
            productStatus.setCreatedAt(LocalDateTime.now());
            productStatus.setUpdatedAt(LocalDateTime.now());
            productStatusRepository.save(productStatus);
            logger.info("Created product status: {} - {}", name, description);
        }
    }

    private void initializeStockCatalogues() {
        // Check if table is empty
        if (stockCatalogueRepository.count() == 0) {
            User adminUser = userRepository.findByEmail("antoniogarciagonzalez212@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

            if (adminUser == null) {
                logger.warn("No user found to assign as creator for stock catalogues. Skipping initialization.");
                return;
            }

            // Create sample stock catalogues (solo contenedor/diccionario)
            createStockCatalogueIfNotExists("Azúcar Morena", "SKU-AZU-001", 
                "Azúcar morena", adminUser);

            createStockCatalogueIfNotExists("Harina de Trigo", "SKU-HAR-002", 
                "Harina de trigo", adminUser);

            createStockCatalogueIfNotExists("Aceite Vegetal", "SKU-ACE-003", 
                "Aceite vegetal", adminUser);

            createStockCatalogueIfNotExists("Sal de Mesa", "SKU-SAL-004", 
                "Sal de mesa", adminUser);

            logger.info("Stock catalogues initialized successfully");
        } else {
            logger.info("Stock catalogues already exist. Skipping initialization.");
        }
    }

    private void createStockCatalogueIfNotExists(String name, String sku, String description, 
            User createdByUser) {
        if (!stockCatalogueRepository.existsBySku(sku)) {
            StockCatalogue stockCatalogue = new StockCatalogue();
            stockCatalogue.setName(name);
            stockCatalogue.setSku(sku);
            stockCatalogue.setDescription(description);
            stockCatalogue.setStatus(true); // Activo por defecto
            stockCatalogue.setCreatedByUser(createdByUser);
            stockCatalogue.setCreatedAt(LocalDateTime.now());
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            stockCatalogueRepository.save(stockCatalogue);
            logger.info("Created stock catalogue: {} ({})", name, sku);
        }
    }

    private void initializeProducts() {
        // Check if table is empty
        if (productRepository.count() == 0) {
            User adminUser = userRepository.findByEmail("antoniogarciagonzalez212@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

            if (adminUser == null) {
                logger.warn("No user found to assign as creator for products. Skipping initialization.");
                return;
            }

            // Get stock catalogues and statuses
            StockCatalogue azucar = stockCatalogueRepository.findBySku("SKU-AZU-001").orElse(null);

            ProductStatus sellado = productStatusRepository.findByName("Sellado").orElse(null);

            // Get warehouse types
            WarehouseType mps = warehouseTypeRepository.findByCode("MPS").orElse(null);

            // Get units of measurement
            UnitOfMeasurement kgUnit = unitOfMeasurementRepository.findByCode("KG").orElse(null);

            if (azucar == null || sellado == null) {
                logger.warn("Required stock catalogue or status not found. Skipping product initialization.");
                return;
            }

            // Create sample products - Variedad de productos de prueba
            // Producto 1: Azúcar Sellado
            createProductWithQrAndMovement("LOTE-AZU-2024-001", azucar, sellado, 
                LocalDate.now().minusDays(5), LocalDate.now().plusYears(1), 
                50, 10, adminUser,
                "PROV-AZU-2024-001", "Azucarera del Sur S.A. de C.V.", "Distribuidora Central México",
                mps, kgUnit, "AN-001", "COD-AZU-001", LocalDate.now().plusMonths(6), "SKU-AZU-001-LOTE-AZU-2024-001");

            // Producto 2: Azúcar Sellado (diferente lote)
            createProductWithQrAndMovement("LOTE-AZU-2024-002", azucar, sellado, 
                LocalDate.now().minusDays(3), LocalDate.now().plusYears(1), 
                50, 15, adminUser,
                "PROV-AZU-2024-002", "Azucarera del Sur S.A. de C.V.", "Distribuidora Central México",
                mps, kgUnit, "AN-002", "COD-AZU-002", LocalDate.now().plusMonths(6), "SKU-AZU-001-LOTE-AZU-2024-002");

            logger.info("Products initialized successfully");
        } else {
            logger.info("Products already exist. Skipping initialization.");
        }
    }

    private void createProductWithQrAndMovement(String lote, StockCatalogue stockCatalogue, 
            ProductStatus productStatus, LocalDate fechaIngreso, LocalDate fechaCaducidad, 
            Integer cantidad, Integer totalEnvases, User createdByUser,
            String loteProveedor, String fabricante, String distribuidor,
            WarehouseType warehouseType, UnitOfMeasurement unitOfMeasurement,
            String numeroAnalisis, String codigoProducto, LocalDate reanalisis, String codigo) {
        try {
            // Create Product
            Product product = new Product();
            product.setStockCatalogue(stockCatalogue);
            product.setProductStatus(productStatus);
            product.setCreatedByUser(createdByUser);
            product.setWarehouseType(warehouseType);
            product.setUnitOfMeasurement(unitOfMeasurement);
            product.setLote(lote);
            product.setLoteProveedor(loteProveedor);
            product.setFabricante(fabricante);
            product.setDistribuidor(distribuidor);
            product.setFecha(fechaIngreso);
            product.setCaducidad(fechaCaducidad);
            product.setReanalisis(reanalisis);
            product.setNombre(stockCatalogue.getName());
            // Usar código proporcionado o generar automáticamente
            product.setCodigo(codigo != null && !codigo.isEmpty() ? codigo : generateProductCode(stockCatalogue, lote));
            product.setCodigoProducto(codigoProducto);
            product.setNumeroAnalisis(numeroAnalisis);
            // CRÍTICO: numeroContenedores es obligatorio (@NotNull)
            product.setNumeroContenedores(totalEnvases);
            // Cantidad total de piezas (basado en cantidad o totalEnvases según lógica de negocio)
            product.setCantidadTotal(cantidad != null ? cantidad : totalEnvases);
            // Descuentos inicializados en 0
            product.setDescuentos(0);
            
            // Calcular automáticamente cantidadSobrante y totalSobrante: cantidadTotal - descuentos
            BigDecimal cantidadSobranteCalculada = BigDecimal.valueOf(product.getCantidadTotal() - product.getDescuentos());

            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            Product savedProduct = productRepository.save(product);

            // Generate and save QR Code
            String qrHash = generateQrHash(savedProduct.getId(), lote);
            QrCode qrCode = new QrCode();
            qrCode.setQrContenido(qrHash);
            qrCode.setCreatedAt(LocalDateTime.now());
            qrCode.setUpdatedAt(LocalDateTime.now());
            QrCode savedQrCode = qrCodeRepository.save(qrCode);

            // Associate QR to Product
            savedProduct.setQrCode(savedQrCode);
            productRepository.save(savedProduct);

            // Create Stock Movement
            ProductStockMovement movement = new ProductStockMovement();
            movement.setUser(createdByUser);
            movement.setStockCatalogue(stockCatalogue);
            movement.setTipo(TipoMovimiento.entrada);
            movement.setCantidad(BigDecimal.valueOf(totalEnvases));
            movement.setReferencia("Ingreso Inicial - Lote " + lote);
            movement.setCreatedAt(LocalDateTime.now());
            movement.setUpdatedAt(LocalDateTime.now());
            productStockMovementRepository.save(movement);

            // Actualizar timestamp del StockCatalogue (los conteos se calculan dinámicamente desde los productos)
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            stockCatalogueRepository.save(stockCatalogue);

            logger.info("Created product: {} with QR hash: {}", lote, qrHash);
        } catch (Exception e) {
            logger.error("Error creating product with lote: {}", lote, e);
        }
    }

    private String generateQrHash(Integer productId, String lote) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String rawHash = productId + "_" + lote + "_" + timestamp;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawHash.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating QR hash", e);
            return productId + "_" + lote + "_" + System.currentTimeMillis();
        }
    }

    private String generateProductCode(StockCatalogue stockCatalogue, String lote) {
        String sku = stockCatalogue.getSku() != null && !stockCatalogue.getSku().isEmpty() 
            ? stockCatalogue.getSku() 
            : "SKU-" + stockCatalogue.getId();
        return sku + "-" + lote + "-" + System.currentTimeMillis() % 10000;
    }


} 