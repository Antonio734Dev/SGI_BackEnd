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
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
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

    @Override
    public void run(String... args) {
        try {
            // Initialize roles
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Create default users
            createDefaultUsers();

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

        // Supervisor
        createUserIfNotExists(
            "Supervisor UTEZ", 
            "20233tn106@utez.edu.mx", 
            "Super2024#Lab", 
            "SUPERVISOR",
            "Supervisor de Laboratorio"
        );

        // Operador
        createUserIfNotExists(
            "Desarrollador LabMétricas", 
            "labmetricasdev@gmail.com", 
            "Oper2024#Lab", 
            "OPERADOR",
            "Operador de Laboratorio"
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

            // Create sample stock catalogues
            createStockCatalogueIfNotExists("Azúcar Morena 1kg", "SKU-AZU-001", 
                "Azúcar morena en presentación de 1 kilogramo", "kg", 
                50, adminUser);

            createStockCatalogueIfNotExists("Harina de Trigo 2kg", "SKU-HAR-002", 
                "Harina de trigo en presentación de 2 kilogramos", "kg", 
                25, adminUser);

            createStockCatalogueIfNotExists("Aceite Vegetal 1L", "SKU-ACE-003", 
                "Aceite vegetal en presentación de 1 litro", "litros", 
                12, adminUser);

            createStockCatalogueIfNotExists("Sal de Mesa 500g", "SKU-SAL-004", 
                "Sal de mesa en presentación de 500 gramos", "kg", 
                5, adminUser);

            logger.info("Stock catalogues initialized successfully");
        } else {
            logger.info("Stock catalogues already exist. Skipping initialization.");
        }
    }

    private void createStockCatalogueIfNotExists(String name, String sku, String description, 
            String unidad, Integer cantidad, User createdByUser) {
        if (!stockCatalogueRepository.existsBySku(sku)) {
            StockCatalogue stockCatalogue = new StockCatalogue();
            stockCatalogue.setName(name);
            stockCatalogue.setSku(sku);
            stockCatalogue.setDescription(description);
            stockCatalogue.setUnidad(unidad);
            stockCatalogue.setStockActual(BigDecimal.ZERO);
            stockCatalogue.setCantidad(cantidad != null ? cantidad : 0);
            stockCatalogue.setStockCantidad(0);
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
            StockCatalogue harina = stockCatalogueRepository.findBySku("SKU-HAR-002").orElse(null);
            StockCatalogue aceite = stockCatalogueRepository.findBySku("SKU-ACE-003").orElse(null);

            ProductStatus sellado = productStatusRepository.findByName("Sellado").orElse(null);
            ProductStatus abierto = productStatusRepository.findByName("Abierto").orElse(null);
            ProductStatus cuarentena = productStatusRepository.findByName("Cuarentena").orElse(null);

            if (azucar == null || sellado == null) {
                logger.warn("Required stock catalogue or status not found. Skipping product initialization.");
                return;
            }

            // Create sample products
            createProductWithQrAndMovement("LOTE-AZU-2024-001", azucar, sellado, 
                LocalDate.now().minusDays(5), LocalDate.now().plusYears(1), 
                50, 1, adminUser);

            createProductWithQrAndMovement("LOTE-AZU-2024-002", azucar, sellado, 
                LocalDate.now().minusDays(3), LocalDate.now().plusYears(1), 
                50, 1, adminUser);

            if (harina != null && abierto != null) {
                createProductWithQrAndMovement("LOTE-HAR-2024-001", harina, abierto, 
                    LocalDate.now().minusDays(10), LocalDate.now().plusMonths(6), 
                    25, 2, adminUser);
            }

            if (aceite != null && cuarentena != null) {
                createProductWithQrAndMovement("LOTE-ACE-2024-001", aceite, cuarentena, 
                    LocalDate.now().minusDays(2), LocalDate.now().plusMonths(12), 
                    12, 12, adminUser);
            }

            logger.info("Products initialized successfully");
        } else {
            logger.info("Products already exist. Skipping initialization.");
        }
    }

    private void createProductWithQrAndMovement(String lote, StockCatalogue stockCatalogue, 
            ProductStatus productStatus, LocalDate fechaIngreso, LocalDate fechaCaducidad, 
            Integer cantidad, Integer totalEnvases, User createdByUser) {
        try {
            // Create Product
            Product product = new Product();
            product.setStockCatalogue(stockCatalogue);
            product.setProductStatus(productStatus);
            product.setCreatedByUser(createdByUser);
            product.setLote(lote);
            product.setFecha(fechaIngreso);
            product.setCaducidad(fechaCaducidad);
            product.setNombre(stockCatalogue.getName());
            product.setCodigo(generateProductCode(stockCatalogue, lote));
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

            // Update Stock Catalogue stock_actual
            BigDecimal currentStock = stockCatalogue.getStockActual() != null ? 
                stockCatalogue.getStockActual() : BigDecimal.ZERO;
            stockCatalogue.setStockActual(currentStock.add(BigDecimal.valueOf(totalEnvases)));
            if (stockCatalogue.getCantidad() == null || stockCatalogue.getCantidad() == 0) {
                stockCatalogue.setCantidad(cantidad);
            }
            Integer currentStockCantidad = stockCatalogue.getStockCantidad() != null ? 
                stockCatalogue.getStockCantidad() : 0;
            stockCatalogue.setStockCantidad(currentStockCantidad + totalEnvases);
            Integer currentEnvasesAprobados = stockCatalogue.getEnvasesAprobados() != null ? 
                stockCatalogue.getEnvasesAprobados() : 0;
            stockCatalogue.setEnvasesAprobados(currentEnvasesAprobados + totalEnvases);
            incrementStatusCounter(stockCatalogue, productStatus, totalEnvases);
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

    private void incrementStatusCounter(StockCatalogue stockCatalogue, ProductStatus status, int delta) {
        if (stockCatalogue == null || status == null || status.getName() == null) {
            return;
        }

        String name = status.getName().toLowerCase();
        switch (name) {
            case "sellado" -> stockCatalogue.setStockSellado(
                Math.max(0, (stockCatalogue.getStockSellado() != null ? stockCatalogue.getStockSellado() : 0) + delta));
            case "abierto" -> stockCatalogue.setStockAbierto(
                Math.max(0, (stockCatalogue.getStockAbierto() != null ? stockCatalogue.getStockAbierto() : 0) + delta));
            case "terminado" -> stockCatalogue.setStockTerminado(
                Math.max(0, (stockCatalogue.getStockTerminado() != null ? stockCatalogue.getStockTerminado() : 0) + delta));
            case "cuarentena" -> stockCatalogue.setStockCuarentena(
                Math.max(0, (stockCatalogue.getStockCuarentena() != null ? stockCatalogue.getStockCuarentena() : 0) + delta));
            default -> logger.warn("Unknown status '{}' when initializing counters", status.getName());
        }
    }

} 