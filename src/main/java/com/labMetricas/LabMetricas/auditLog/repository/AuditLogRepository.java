package com.labMetricas.LabMetricas.auditLog.repository;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    // Método estándar de Spring Data JPA - busca logs por usuario
    List<AuditLog> findByUser(User user);
    
    // Método estándar para buscar logs por usuario ordenados por fecha descendente
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
} 