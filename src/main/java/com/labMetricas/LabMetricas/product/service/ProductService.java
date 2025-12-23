package com.labMetricas.LabMetricas.product.service;

import com.labMetricas.LabMetricas.catalogue.model.StockCatalogue;
import com.labMetricas.LabMetricas.catalogue.repository.StockCatalogueRepository;
import com.labMetricas.LabMetricas.enums.TipoMovimiento;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.movement.model.ProductStockMovement;
import com.labMetricas.LabMetricas.movement.repository.ProductStockMovementRepository;
import com.labMetricas.LabMetricas.product.model.Product;
import com.labMetricas.LabMetricas.product.model.dto.CreateProductDto;
import com.labMetricas.LabMetricas.product.model.dto.ProductResponseDto;
import com.labMetricas.LabMetricas.product.model.dto.UpdateProductDto;
import com.labMetricas.LabMetricas.product.repository.ProductRepository;
import com.labMetricas.LabMetricas.qrcode.model.QrCode;
import com.labMetricas.LabMetricas.qrcode.repository.QrCodeRepository;
import com.labMetricas.LabMetricas.status.model.ProductStatus;
import com.labMetricas.LabMetricas.status.repository.ProductStatusRepository;
import com.labMetricas.LabMetricas.unitofmeasurement.model.UnitOfMeasurement;
import com.labMetricas.LabMetricas.unitofmeasurement.repository.UnitOfMeasurementRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.qrcode.service.QrCodeService;
import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.util.PageResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.warehousetype.model.WarehouseType;
import com.labMetricas.LabMetricas.warehousetype.repository.WarehouseTypeRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.zxing.WriterException;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockCatalogueRepository stockCatalogueRepository;

    @Autowired
    private ProductStatusRepository productStatusRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private ProductStockMovementRepository productStockMovementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private WarehouseTypeRepository warehouseTypeRepository;

    @Autowired
    private UnitOfMeasurementRepository unitOfMeasurementRepository;

    // Método helper para crear logs de auditoría mejorados
    private void createAuditLog(String action, User user, Product product) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setUser(user);
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogRepository.save(auditLog);
            
            logger.info("Audit log created: {} by user: {} for product: {}", action, 
                user != null ? user.getEmail() : "ANONYMOUS",
                product != null ? product.getId() : "N/A");
        } catch (Exception e) {
            logger.error("Error creating audit log: {}", action, e);
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> createProduct(CreateProductDto createProductDto) {
        try {
            // PASO A: Validaciones
            logger.info("Starting product creation transaction for lote: {}", createProductDto.getLote());

            // Validar que stock_catalogue_id existe
            StockCatalogue stockCatalogue = stockCatalogueRepository.findByIdAndDeletedAtIsNull(createProductDto.getStockCatalogueId())
                .orElseThrow(() -> new RuntimeException("Stock catalogue not found or deleted"));

            // Validar que product_status_id existe
            ProductStatus productStatus = productStatusRepository.findByIdAndDeletedAtIsNull(createProductDto.getProductStatusId())
                .orElseThrow(() -> new RuntimeException("Product status not found or deleted"));

            // Validar warehouse type si se proporciona
            WarehouseType warehouseType = null;
            if (createProductDto.getWarehouseTypeId() != null) {
                warehouseType = warehouseTypeRepository.findByIdAndDeletedAtIsNull(createProductDto.getWarehouseTypeId())
                    .orElseThrow(() -> new RuntimeException("Warehouse type not found or deleted"));
            }

            // Validar unit of measurement si se proporciona
            UnitOfMeasurement unitOfMeasurement = null;
            if (createProductDto.getUnitOfMeasurementId() != null) {
                unitOfMeasurement = unitOfMeasurementRepository.findByIdAndDeletedAtIsNull(createProductDto.getUnitOfMeasurementId())
                    .orElseThrow(() -> new RuntimeException("Unit of measurement not found or deleted"));
            }

            // Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // PASO B: Guardar Producto
            logger.info("Creating product entity...");
            Product product = new Product();
            product.setStockCatalogue(stockCatalogue);
            product.setProductStatus(productStatus);
            product.setCreatedByUser(currentUser);
            product.setWarehouseType(warehouseType);
            product.setUnitOfMeasurement(unitOfMeasurement);
            product.setLote(createProductDto.getLote());
            product.setLoteProveedor(createProductDto.getLoteProveedor());
            product.setFechaMuestreo(createProductDto.getFechaMuestreo());
            product.setFabricante(createProductDto.getFabricante());
            product.setDistribuidor(createProductDto.getDistribuidor());
            product.setCodigoProducto(createProductDto.getCodigoProducto());
            product.setNumeroAnalisis(createProductDto.getNumeroAnalisis());
            product.setFecha(createProductDto.getFechaIngreso());
            product.setCaducidad(createProductDto.getFechaCaducidad());
            product.setReanalisis(createProductDto.getReanalisis());
            product.setNumeroContenedores(createProductDto.getNumeroContenedores());
            product.setCantidadTotal(createProductDto.getCantidadTotal());
            product.setDescuentos(createProductDto.getDescuentos() != null ? createProductDto.getDescuentos() : 0);
            
            // Calcular automáticamente cantidadSobrante y totalSobrante: cantidadTotal - descuentos
            Integer descuentosValue = product.getDescuentos() != null ? product.getDescuentos() : 0;
            BigDecimal cantidadSobranteCalculada = BigDecimal.valueOf(product.getCantidadTotal() - descuentosValue);

            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            // Asignar nombre (se define desde la request)
            product.setNombre(createProductDto.getNombre() != null ? createProductDto.getNombre().trim() : null);
            product.setCodigo(generateProductCode(stockCatalogue, createProductDto.getLote()));

            Product savedProduct = productRepository.save(product);
            logger.info("Product saved with ID: {}", savedProduct.getId());

            // PASO C: Generar y Guardar QR
            logger.info("Generating QR code...");
            String qrHash = generateQrHash(savedProduct.getId(), createProductDto.getLote());
            
            QrCode qrCode = new QrCode();
            qrCode.setQrContenido(qrHash);
            qrCode.setCreatedAt(LocalDateTime.now());
            qrCode.setUpdatedAt(LocalDateTime.now());
            
            QrCode savedQrCode = qrCodeRepository.save(qrCode);
            logger.info("QR code saved with ID: {}", savedQrCode.getId());

            // Asociar QR al Product
            savedProduct.setQrCode(savedQrCode);
            productRepository.save(savedProduct);
            logger.info("QR code associated to product");

            // PASO D: Registrar Movimiento (Kardex)
            logger.info("Creating stock movement...");
            ProductStockMovement movement = new ProductStockMovement();
            movement.setUser(currentUser);
            movement.setStockCatalogue(stockCatalogue);
            movement.setTipo(TipoMovimiento.entrada);
            movement.setCantidad(BigDecimal.valueOf(createProductDto.getNumeroContenedores()));
            movement.setReferencia("Ingreso Inicial - Lote " + createProductDto.getLote());
            movement.setCreatedAt(LocalDateTime.now());
            movement.setUpdatedAt(LocalDateTime.now());
            
            ProductStockMovement savedMovement = productStockMovementRepository.save(movement);
            logger.info("Stock movement saved with ID: {}", savedMovement.getId());

            // PASO E: Actualizar timestamp del StockCatalogue (los conteos se calculan dinámicamente desde los productos)
            logger.info("Updating stock catalogue timestamp...");
            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            
            StockCatalogue updatedStockCatalogue = stockCatalogueRepository.save(stockCatalogue);
            logger.info("Stock catalogue updated. Product count will be calculated dynamically.");

            // Preparar respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productId", savedProduct.getId());
            responseData.put("qrCodeId", savedQrCode.getId());
            responseData.put("qrHash", qrHash);
            responseData.put("movementId", savedMovement.getId());

            logger.info("Product creation transaction completed successfully for lote: {}", createProductDto.getLote());

            // Registrar log de auditoría mejorado con información detallada
            String auditMessage = String.format(
                "CREACIÓN DE PRODUCTO - Usuario: %s | Producto: %s (ID: %d) | Lote: %s | Estado: %s | Catálogo: %s",
                currentUser.getName() != null ? currentUser.getName() : currentUser.getEmail(),
                savedProduct.getNombre(),
                savedProduct.getId(),
                savedProduct.getLote(),
                productStatus.getName(),
                stockCatalogue.getName()
            );
            createAuditLog(auditMessage, currentUser, savedProduct);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Product created successfully", responseData, TypeResponse.SUCCESS)
            );

        } catch (RuntimeException e) {
            logger.error("Validation error during product creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ResponseObject("Validation error: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error creating product - Transaction will be rolled back", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating product: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Genera un hash único para el QR code concatenando Product_ID + Lote + Timestamp
     */
    private String generateQrHash(Integer productId, String lote) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String rawHash = productId + "_" + lote + "_" + timestamp;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawHash.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal
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
            // Fallback: usar concatenación simple
            return productId + "_" + lote + "_" + System.currentTimeMillis();
        }
    }

    /**
     * Genera un código único para el producto
     */
    private String generateProductCode(StockCatalogue stockCatalogue, String lote) {
        String sku = stockCatalogue.getSku() != null && !stockCatalogue.getSku().isEmpty() 
            ? stockCatalogue.getSku() 
            : "SKU-" + stockCatalogue.getId();
        return sku + "-" + lote + "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * Busca un producto por su QR hash
     */
    public ResponseEntity<ResponseObject> getProductByQrHash(String qrHash) {
        try {
            logger.info("Searching product by QR hash: {}", qrHash);

            // Buscar QR code por hash
            QrCode qrCode = qrCodeRepository.findByQrContenidoAndDeletedAtIsNull(qrHash)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

            // Buscar producto asociado al QR
            Product product = productRepository.findByQrCodeIdAndDeletedAtIsNull(qrCode.getId())
                .orElseThrow(() -> new RuntimeException("Product not found for this QR code"));

            // Convertir a DTO con nombres legibles
            ProductResponseDto responseDto = convertToResponseDto(product);

            logger.info("Product found by QR hash: Product ID {}", product.getId());

            return ResponseEntity.ok(
                new ResponseObject("Product retrieved successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (RuntimeException e) {
            logger.error("Error retrieving product by QR hash: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Product not found: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error retrieving product by QR hash", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving product", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateProduct(UpdateProductDto updateProductDto) {
        try {
            logger.info("Starting product update transaction for product ID: {}", updateProductDto.getId());

            // Buscar producto existente
            Product existingProduct = productRepository.findByIdAndDeletedAtIsNull(updateProductDto.getId())
                .orElseThrow(() -> new RuntimeException("Product not found or deleted"));

            // Validar y actualizar stock catalogue si se proporciona
            if (updateProductDto.getStockCatalogueId() != null) {
                StockCatalogue stockCatalogue = stockCatalogueRepository.findByIdAndDeletedAtIsNull(updateProductDto.getStockCatalogueId())
                    .orElseThrow(() -> new RuntimeException("Stock catalogue not found or deleted"));
                existingProduct.setStockCatalogue(stockCatalogue);
            }

            // Obtener usuario autenticado para el log
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                .orElse(null);

            // Guardar estado anterior para el log
            ProductStatus oldStatus = existingProduct.getProductStatus();
            String oldStatusName = oldStatus != null ? oldStatus.getName() : "N/A";

            // Validar y actualizar warehouse type si se proporciona
            if (updateProductDto.getWarehouseTypeId() != null) {
                WarehouseType warehouseType = warehouseTypeRepository.findByIdAndDeletedAtIsNull(updateProductDto.getWarehouseTypeId())
                    .orElseThrow(() -> new RuntimeException("Warehouse type not found or deleted"));
                existingProduct.setWarehouseType(warehouseType);
            }

            // Validar y actualizar unit of measurement si se proporciona
            if (updateProductDto.getUnitOfMeasurementId() != null) {
                UnitOfMeasurement unitOfMeasurement = unitOfMeasurementRepository.findByIdAndDeletedAtIsNull(updateProductDto.getUnitOfMeasurementId())
                    .orElseThrow(() -> new RuntimeException("Unit of measurement not found or deleted"));
                existingProduct.setUnitOfMeasurement(unitOfMeasurement);
            }

            // Validar y actualizar product status si se proporciona (permite cambiar estado)
            ProductStatus newStatus = null;
            if (updateProductDto.getProductStatusId() != null) {
                newStatus = productStatusRepository.findByIdAndDeletedAtIsNull(updateProductDto.getProductStatusId())
                    .orElseThrow(() -> new RuntimeException("Product status not found or deleted"));
                existingProduct.setProductStatus(newStatus);
            }

            // Actualizar campos opcionales
            if (updateProductDto.getLote() != null && !updateProductDto.getLote().trim().isEmpty()) {
                existingProduct.setLote(updateProductDto.getLote());
            }
            if (updateProductDto.getLoteProveedor() != null) {
                existingProduct.setLoteProveedor(updateProductDto.getLoteProveedor());
            }
            if (updateProductDto.getFabricante() != null) {
                existingProduct.setFabricante(updateProductDto.getFabricante());
            }
            if (updateProductDto.getDistribuidor() != null) {
                existingProduct.setDistribuidor(updateProductDto.getDistribuidor());
            }
            if (updateProductDto.getCodigoProducto() != null) {
                existingProduct.setCodigoProducto(updateProductDto.getCodigoProducto());
            }
            if (updateProductDto.getNumeroAnalisis() != null) {
                existingProduct.setNumeroAnalisis(updateProductDto.getNumeroAnalisis());
            }
            if (updateProductDto.getNombre() != null && !updateProductDto.getNombre().trim().isEmpty()) {
                existingProduct.setNombre(updateProductDto.getNombre().trim());
            }
            if (updateProductDto.getFechaIngreso() != null) {
                existingProduct.setFecha(updateProductDto.getFechaIngreso());
            }
            if (updateProductDto.getFechaCaducidad() != null) {
                existingProduct.setCaducidad(updateProductDto.getFechaCaducidad());
            }
            if (updateProductDto.getFechaMuestreo() != null) {
                existingProduct.setFechaMuestreo(updateProductDto.getFechaMuestreo());
            }
            if (updateProductDto.getReanalisis() != null) {
                existingProduct.setReanalisis(updateProductDto.getReanalisis());
            }
            if (updateProductDto.getNumeroContenedores() != null) {
                existingProduct.setNumeroContenedores(updateProductDto.getNumeroContenedores());
            }
            if (updateProductDto.getCantidadTotal() != null) {
                existingProduct.setCantidadTotal(updateProductDto.getCantidadTotal());
            }
            if (updateProductDto.getDescuentos() != null) {
                existingProduct.setDescuentos(updateProductDto.getDescuentos());
            }

            // Recalcular automáticamente cantidadSobrante y totalSobrante cuando se actualicen cantidadTotal o descuentos
            Integer cantidadTotalValue = existingProduct.getCantidadTotal() != null ? existingProduct.getCantidadTotal() : 0;
            Integer descuentosValue = existingProduct.getDescuentos() != null ? existingProduct.getDescuentos() : 0;
            BigDecimal cantidadSobranteCalculada = BigDecimal.valueOf(cantidadTotalValue - descuentosValue);

            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(existingProduct);
            ProductResponseDto responseDto = convertToResponseDto(updatedProduct);

            logger.info("Product updated successfully: Product ID {}", updatedProduct.getId());

            // Registrar log de auditoría mejorado con información detallada
            String statusChangeInfo = "";
            if (newStatus != null && !oldStatusName.equals(newStatus.getName())) {
                statusChangeInfo = String.format(" | Cambio de estado: %s -> %s", oldStatusName, newStatus.getName());
            } else if (updatedProduct.getProductStatus() != null) {
                statusChangeInfo = String.format(" | Estado: %s", updatedProduct.getProductStatus().getName());
            }
            
            String auditMessage = String.format(
                "MODIFICACIÓN DE PRODUCTO - Usuario: %s | Producto: %s (ID: %d) | Lote: %s%s",
                currentUser != null ? (currentUser.getName() != null ? currentUser.getName() : currentUser.getEmail()) : "ANONYMOUS",
                updatedProduct.getNombre(),
                updatedProduct.getId(),
                updatedProduct.getLote(),
                statusChangeInfo
            );
            createAuditLog(auditMessage, currentUser, updatedProduct);

            return ResponseEntity.ok(
                new ResponseObject("Product updated successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (RuntimeException e) {
            logger.error("Validation error during product update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new ResponseObject("Validation error: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        } catch (Exception e) {
            logger.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating product: " + e.getMessage(), null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Lista productos con filtros opcionales
     */
    public ResponseEntity<ResponseObject> getAllProducts(int page, int size, Integer stockCatalogueId, Integer productStatusId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Product> productsPage;

            if (stockCatalogueId != null && productStatusId != null) {
                // Filtrar por ambos
                productsPage = productRepository.findByStockCatalogueIdAndProductStatusIdAndDeletedAtIsNull(
                    stockCatalogueId, productStatusId, pageable);
            } else if (stockCatalogueId != null) {
                // Filtrar solo por catálogo
                productsPage = productRepository.findByStockCatalogueIdAndDeletedAtIsNull(stockCatalogueId, pageable);
            } else if (productStatusId != null) {
                // Filtrar solo por estado
                productsPage = productRepository.findByProductStatusIdAndDeletedAtIsNull(productStatusId, pageable);
            } else {
                // Sin filtros
                productsPage = productRepository.findByDeletedAtIsNull(pageable);
            }

            // Convertir a DTOs
            PageResponse<ProductResponseDto> pageResponse = new PageResponse<>(
                productsPage.map(this::convertToResponseDto)
            );

            // NO registrar log de auditoría para consultas (evitar spam)

            return ResponseEntity.ok(
                new ResponseObject("Products retrieved successfully", pageResponse, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving products", null, TypeResponse.ERROR)
            );
        }
    }

    /**
     * Convierte Product a ProductResponseDto con nombres legibles
     * Primero datos del producto, luego datos del stock
     */
    private ProductResponseDto convertToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        
        // Información del producto (primero)
        dto.setId(product.getId());
        dto.setNombre(product.getNombre());
        dto.setLote(product.getLote()); // Lote interno
        dto.setLoteProveedor(product.getLoteProveedor());
        dto.setFabricante(product.getFabricante());
        dto.setDistribuidor(product.getDistribuidor());
        dto.setCodigo(product.getCodigo());
        dto.setCodigoProducto(product.getCodigoProducto());
        dto.setNumeroAnalisis(product.getNumeroAnalisis());
        dto.setFecha(product.getFecha());
        dto.setCaducidad(product.getCaducidad());
        dto.setReanalisis(product.getReanalisis());
        dto.setFechaMuestreo(product.getFechaMuestreo());
        dto.setNumeroContenedores(product.getNumeroContenedores());
        dto.setCantidadTotal(product.getCantidadTotal());
        dto.setDescuentos(product.getDescuentos());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Información del estado
        if (product.getProductStatus() != null) {
            dto.setProductStatusId(product.getProductStatus().getId());
            dto.setProductStatusName(product.getProductStatus().getName());
            dto.setProductStatusDescription(product.getProductStatus().getDescription());
        }

        // Información del QR
        if (product.getQrCode() != null) {
            dto.setQrCodeId(product.getQrCode().getId());
            dto.setQrHash(product.getQrCode().getQrContenido());
        }

        // Información del creador
        if (product.getCreatedByUser() != null) {
            dto.setCreatedByUserId(product.getCreatedByUser().getId());
            dto.setCreatedByUserName(product.getCreatedByUser().getName());
        }

        // Información del warehouse type
        if (product.getWarehouseType() != null) {
            dto.setWarehouseTypeId(product.getWarehouseType().getId());
            dto.setWarehouseTypeCode(product.getWarehouseType().getCode());
            dto.setWarehouseTypeName(product.getWarehouseType().getName());
        }

        // Información de unidad de medida
        if (product.getUnitOfMeasurement() != null) {
            dto.setUnitOfMeasurementId(product.getUnitOfMeasurement().getId());
            dto.setUnitOfMeasurementName(product.getUnitOfMeasurement().getName());
            dto.setUnitOfMeasurementCode(product.getUnitOfMeasurement().getCode());
        }

        // Información del stock (después del producto)
        if (product.getStockCatalogue() != null) {
            StockCatalogue stockCatalogue = product.getStockCatalogue();
            dto.setStockCatalogueId(stockCatalogue.getId());
            dto.setStockCatalogueName(stockCatalogue.getName());
            dto.setStockCatalogueSku(stockCatalogue.getSku());
            // Las métricas de stock ahora se calculan dinámicamente desde los productos
            // StockCatalogue es solo un contenedor/diccionario
        }

        return dto;
    }

    /**
     * Genera la imagen QR del producto a partir de su hash
     */
    public byte[] generateQrCodeImage(String qrHash) {
        try {
            logger.info("Generating QR code image for hash: {}", qrHash);

            // Validar que el hash existe
            if (!qrCodeRepository.findByQrContenidoAndDeletedAtIsNull(qrHash).isPresent()) {
                throw new RuntimeException("QR hash not found");
            }

            // Generar la imagen del QR
            byte[] qrImage = qrCodeService.generateQrCodeImage(qrHash);
            
            logger.info("QR code image generated successfully for hash: {}", qrHash);
            
            // NO registrar log de auditoría para consultas/generación de imágenes (evitar spam)
            
            return qrImage;
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code image", e);
            throw new RuntimeException("Error generating QR code image: " + e.getMessage());
        }
    }
}

