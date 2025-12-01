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
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.qrcode.service.QrCodeService;
import com.labMetricas.LabMetricas.util.PageResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
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
            product.setLote(createProductDto.getLote());
            product.setLoteProveedor(createProductDto.getLoteProveedor());
            product.setFabricante(createProductDto.getFabricante());
            product.setDistribuidor(createProductDto.getDistribuidor());
            product.setFecha(createProductDto.getFechaIngreso());
            product.setCaducidad(createProductDto.getFechaCaducidad());
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            // Generar nombre y codigo basados en el catálogo
            product.setNombre(stockCatalogue.getName());
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
            movement.setCantidad(BigDecimal.valueOf(createProductDto.getTotalEnvases()));
            movement.setReferencia("Ingreso Inicial - Lote " + createProductDto.getLote());
            movement.setCreatedAt(LocalDateTime.now());
            movement.setUpdatedAt(LocalDateTime.now());
            
            ProductStockMovement savedMovement = productStockMovementRepository.save(movement);
            logger.info("Stock movement saved with ID: {}", savedMovement.getId());

            // PASO E: Actualizar Stock Global
            logger.info("Updating stock catalogue stock_actual...");
            BigDecimal currentStock = stockCatalogue.getStockActual() != null ? 
                stockCatalogue.getStockActual() : BigDecimal.ZERO;
            BigDecimal newStock = currentStock.add(BigDecimal.valueOf(createProductDto.getTotalEnvases()));
            stockCatalogue.setStockActual(newStock);

            if (stockCatalogue.getCantidad() == null || stockCatalogue.getCantidad() == 0) {
                stockCatalogue.setCantidad(createProductDto.getCantidad());
            }
            Integer currentStockCantidad = stockCatalogue.getStockCantidad() != null ? 
                stockCatalogue.getStockCantidad() : 0;
            stockCatalogue.setStockCantidad(currentStockCantidad + createProductDto.getTotalEnvases());

            Integer currentEnvasesAprobados = stockCatalogue.getEnvasesAprobados() != null ? 
                stockCatalogue.getEnvasesAprobados() : 0;
            stockCatalogue.setEnvasesAprobados(currentEnvasesAprobados + createProductDto.getTotalEnvases());

            stockCatalogue.setUpdatedAt(LocalDateTime.now());
            
            StockCatalogue updatedStockCatalogue = stockCatalogueRepository.save(stockCatalogue);
            logger.info("Stock catalogue updated. New stock_actual: {}", updatedStockCatalogue.getStockActual());

            // Preparar respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("productId", savedProduct.getId());
            responseData.put("qrCodeId", savedQrCode.getId());
            responseData.put("qrHash", qrHash);
            responseData.put("movementId", savedMovement.getId());
            responseData.put("stockActual", updatedStockCatalogue.getStockActual());

            logger.info("Product creation transaction completed successfully for lote: {}", createProductDto.getLote());

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

            // Validar y actualizar product status si se proporciona (permite cambiar estado)
            if (updateProductDto.getProductStatusId() != null) {
                ProductStatus productStatus = productStatusRepository.findByIdAndDeletedAtIsNull(updateProductDto.getProductStatusId())
                    .orElseThrow(() -> new RuntimeException("Product status not found or deleted"));
                existingProduct.setProductStatus(productStatus);
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
            if (updateProductDto.getFechaIngreso() != null) {
                existingProduct.setFecha(updateProductDto.getFechaIngreso());
            }
            if (updateProductDto.getFechaCaducidad() != null) {
                existingProduct.setCaducidad(updateProductDto.getFechaCaducidad());
            }
            if (updateProductDto.getReanalisis() != null) {
                existingProduct.setReanalisis(updateProductDto.getReanalisis());
            }

            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product updatedProduct = productRepository.save(existingProduct);
            ProductResponseDto responseDto = convertToResponseDto(updatedProduct);

            logger.info("Product updated successfully: Product ID {}", updatedProduct.getId());

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
     * Calcula los descuentos (productos con estado "terminado") para un stock
     */
    private Integer calculateDescuentos(StockCatalogue stockCatalogue) {
        try {
            var productosTerminados = productRepository.findByStockCatalogueIdAndProductStatusNameIgnoreCaseAndDeletedAtIsNull(
                stockCatalogue.getId(), "terminado"
            );

            if (productosTerminados.isEmpty()) {
                return 0;
            }

            int totalDescuentos = 0;
            for (Product product : productosTerminados) {
                var movimientos = productStockMovementRepository
                    .findByStockCatalogueIdAndDeletedAtIsNull(stockCatalogue.getId());
                
                for (var movimiento : movimientos) {
                    if (movimiento.getTipo() == TipoMovimiento.entrada && 
                        movimiento.getReferencia() != null &&
                        movimiento.getReferencia().contains("Lote " + product.getLote())) {
                        totalDescuentos += movimiento.getCantidad().intValue();
                        break;
                    }
                }
            }

            return totalDescuentos;
        } catch (Exception e) {
            logger.warn("Error calculating descuentos for stock catalogue {}: {}", stockCatalogue.getId(), e.getMessage());
            return 0;
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
        dto.setFecha(product.getFecha());
        dto.setCaducidad(product.getCaducidad());
        dto.setReanalisis(product.getReanalisis());
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

        // Información del stock (después del producto)
        if (product.getStockCatalogue() != null) {
            StockCatalogue stockCatalogue = product.getStockCatalogue();
            dto.setStockCatalogueId(stockCatalogue.getId());
            dto.setStockCatalogueName(stockCatalogue.getName());
            dto.setStockCatalogueSku(stockCatalogue.getSku());
            dto.setStockCatalogueUnidad(stockCatalogue.getUnidad());
            
            // Métricas de stock calculadas
            Integer cantidadTotal = stockCatalogue.getStockCantidad() != null ? stockCatalogue.getStockCantidad() : 0;
            Integer descuentos = calculateDescuentos(stockCatalogue);
            Integer cantidadSobrante = Math.max(0, cantidadTotal - descuentos);
            
            dto.setCantidadTotal(cantidadTotal);
            dto.setDescuentos(descuentos);
            dto.setCantidadSobrante(cantidadSobrante);
            dto.setEnvasesRechazados(stockCatalogue.getEnvasesRechazados());
            dto.setEnvasesAprobados(stockCatalogue.getEnvasesAprobados());
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
            return qrImage;
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code image", e);
            throw new RuntimeException("Error generating QR code image: " + e.getMessage());
        }
    }
}

