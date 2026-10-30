package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.AuditLogView;
import fr.miage.blindbattle.dto.OutboxEventView;
import fr.miage.blindbattle.service.AuditService;
import fr.miage.blindbattle.service.OutboxService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AuditService audit;
    private final OutboxService outbox;

    public AdminController(AuditService audit, OutboxService outbox) {
        this.audit = audit;
        this.outbox = outbox;
    }

    @GetMapping("/audit-logs")
    public List<AuditLogView> auditLogs() { return audit.list(); }

    @GetMapping("/outbox")
    public List<OutboxEventView> outbox() { return outbox.list(); }
}
