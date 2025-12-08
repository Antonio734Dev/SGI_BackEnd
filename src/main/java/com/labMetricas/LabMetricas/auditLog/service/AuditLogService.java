package com.labMetricas.LabMetricas.auditLog.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.model.dto.AuditLogDto;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AuditLogDto> getAllLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
            .map(log -> new AuditLogDto(
                log.getAction(),
                log.getCreatedAt(),
                log.getUser() != null ? log.getUser().getEmail() : null,
                log.getUser() != null ? log.getUser().getName() : null
            ))
            .collect(Collectors.toList());
    }

    public List<AuditLogDto> getLogsByUserEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        User user = userOpt.get();
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(log -> new AuditLogDto(
                log.getAction(),
                log.getCreatedAt(),
                log.getUser().getEmail(),
                log.getUser().getName()
            ))
            .collect(Collectors.toList());
    }
    
    public List<AuditLogDto> getLogsByUserId(java.util.UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        User user = userOpt.get();
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(log -> new AuditLogDto(
                log.getAction(),
                log.getCreatedAt(),
                log.getUser().getEmail(),
                log.getUser().getName()
            ))
            .collect(Collectors.toList());
    }
} 