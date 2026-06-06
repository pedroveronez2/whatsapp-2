package com.whatsappclone.backend.service;

import com.whatsappclone.backend.model.AuditLog;
import com.whatsappclone.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String username, String ip, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUsername(username);
        log.setIp(ip);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}