package com.labMetricas.LabMetricas.Notice.service;

import com.labMetricas.LabMetricas.config.ProductionEmailService;
import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.Notice.model.dto.NoticeDto;
import com.labMetricas.LabMetricas.Notice.repository.NoticeRepository;
import com.labMetricas.LabMetricas.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeService {
    private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private ProductionEmailService productionEmailService;

    private void sendNoticeEmail(Notice notice) {
        try {
            // Send real email notification using ProductionEmailService
            String subject = "Nueva Notificación: " + notice.getTitle();
            String htmlContent = createNoticeEmailBody(notice);
            
            productionEmailService.sendCustomEmail(
                notice.getCreatedBy().getEmail(),
                subject,
                htmlContent
            );
            
            logger.info("Notice email sent to: {}", notice.getCreatedBy().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send notice email to: {}", notice.getCreatedBy().getEmail(), e);
        }
    }

    private String createNoticeEmailBody(Notice notice) {
        return String.format(
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            "        .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }" +
            "        .title { color: #007bff; font-weight: bold; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <div class='header'>" +
            "            <h1>LabMetricas - Notificación</h1>" +
            "        </div>" +
            "        <div class='content'>" +
            "            <h2 class='title'>%s</h2>" +
            "            <p><strong>Descripción:</strong> %s</p>" +
            "            <p><strong>Fecha de creación:</strong> %s</p>" +
            "            <p>Por favor, revisa tu panel de notificaciones para más detalles.</p>" +
            "            <p>Saludos,<br>Sistema de Gestión de Mantenimiento</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>",
            notice.getTitle(),
            notice.getDescription(),
            notice.getCreatedAt().toString()
        );
    }


    @Transactional(readOnly = true)
    public List<NoticeDto> getActiveNoticesByUser(User user) {
        return noticeRepository.findByCreatedByAndStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc(user)
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllNoticesByUser(User user) {
        return noticeRepository.findByCreatedByAndDeletedAtIsNullOrderByCreatedAtDesc(user)
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllActiveNotices() {
        return noticeRepository.findByStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllNotices() {
        return noticeRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

} 