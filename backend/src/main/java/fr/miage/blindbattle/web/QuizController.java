package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.PageView;
import fr.miage.blindbattle.dto.QuizRequest;
import fr.miage.blindbattle.dto.QuizSummaryView;
import fr.miage.blindbattle.dto.QuizView;
import fr.miage.blindbattle.security.AuthUser;
import fr.miage.blindbattle.service.QuizService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    private final QuizService service;

    public QuizController(QuizService service) { this.service = service; }

    @GetMapping
    public PageView<QuizSummaryView> mine(@AuthenticationPrincipal AuthUser user,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return service.mine(user, page, size);
    }

    @PostMapping
    public ResponseEntity<QuizView> create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody QuizRequest request) {
        QuizView created = service.create(user, request);
        return ResponseEntity.created(URI.create("/api/quizzes/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public QuizView get(@PathVariable("id") long id, @AuthenticationPrincipal AuthUser user) { return service.get(id, user); }

    @PutMapping("/{id}")
    public QuizView update(@PathVariable("id") long id, @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody QuizRequest request) {
        return service.update(id, user, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id, @AuthenticationPrincipal AuthUser user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shared/{token}")
    public QuizView shared(@PathVariable("token") UUID token) { return service.shared(token); }
}
