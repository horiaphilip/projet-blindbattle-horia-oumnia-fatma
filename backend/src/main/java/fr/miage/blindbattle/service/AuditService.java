package fr.miage.blindbattle.service;

import fr.miage.blindbattle.dto.AuditLogView;
import fr.miage.blindbattle.repository.AuditLogRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<AuditLogView> list() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(a -> new AuditLogView(a.getId(), a.getType(), a.getUserId(), a.getDetails(), a.getCreatedAt()))
                .toList();
    }
}
