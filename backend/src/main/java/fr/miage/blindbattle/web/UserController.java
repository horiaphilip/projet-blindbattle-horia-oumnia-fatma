package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.StatsView;
import fr.miage.blindbattle.dto.UserRequest;
import fr.miage.blindbattle.dto.UserView;
import fr.miage.blindbattle.service.StatsService;
import fr.miage.blindbattle.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;
    private final StatsService stats;

    public UserController(UserService service, StatsService stats) {
        this.service = service;
        this.stats = stats;
    }

    @GetMapping
    public List<UserView> list() { return service.list(); }

    @GetMapping("/{id}")
    public UserView get(@PathVariable("id") long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<UserView> create(@Valid @RequestBody UserRequest request) {
        UserView created = service.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public UserView update(@PathVariable("id") long id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public StatsView stats(@PathVariable("id") long id) { return stats.forUser(id); }
}
