package com.labMetricas.LabMetricas.qrcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.labMetricas.LabMetricas.qrcode.repository.QrCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {
    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    @Autowired
    private QrCodeRepository qrCodeRepository;

    private static final int QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Genera una imagen QR a partir del hash del producto
     * @param qrHash El hash único del producto
     * @return Array de bytes de la imagen PNG del QR
     */
    public byte[] generateQrCodeImage(String qrHash) throws WriterException, IOException {
        logger.info("Generating QR code image for hash: {}", qrHash);

        // Configuración del QR Code
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // Crear el QR Code Writer
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrHash, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        // Convertir BitMatrix a BufferedImage
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage qrImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        qrImage.createGraphics();

        Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        // Dibujar el QR Code
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        graphics.dispose();

        // Convertir BufferedImage a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, IMAGE_FORMAT, baos);
        byte[] imageBytes = baos.toByteArray();

        logger.info("QR code image generated successfully. Size: {} bytes", imageBytes.length);
        return imageBytes;
    }

    /**
     * Obtiene el hash del QR code por ID del producto
     */
    public String getQrHashByProductId(Integer productId) {
        // Buscar el producto y obtener su QR code
        // Esto se puede hacer desde el ProductService, pero por ahora retornamos null si no se encuentra
        return null;
    }

    /**
     * Valida que el hash existe en la base de datos
     */
    public boolean validateQrHash(String qrHash) {
        return qrCodeRepository.findByQrContenidoAndDeletedAtIsNull(qrHash).isPresent();
    }
}

