package com.labMetricas.LabMetricas.qrcode.repository;

import com.labMetricas.LabMetricas.qrcode.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Integer> {
    // Find by qr contenido
    Optional<QrCode> findByQrContenido(String qrContenido);
    
    // Find by qr contenido and not deleted
    Optional<QrCode> findByQrContenidoAndDeletedAtIsNull(String qrContenido);
    
    // Find by id and not deleted
    Optional<QrCode> findByIdAndDeletedAtIsNull(Integer id);
    
    // Find all non-deleted qr codes
    List<QrCode> findByDeletedAtIsNull();
}

