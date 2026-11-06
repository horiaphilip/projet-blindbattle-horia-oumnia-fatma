package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.GameRequest;
import fr.miage.blindbattle.dto.GameStateView;
import fr.miage.blindbattle.dto.GameView;
import fr.miage.blindbattle.dto.JoinRequest;
import fr.miage.blindbattle.dto.JoinView;
import fr.miage.blindbattle.security.AuthUser;
import fr.miage.blindbattle.service.GameService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService service;

    public GameController(GameService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<GameView> create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody GameRequest request) {
        GameView created = service.create(user, request);
        return ResponseEntity.created(URI.create("/api/games/" + created.code())).body(created);
    }

    @GetMapping("/{code}")
    public GameView get(@PathVariable("code") String code) { return service.get(code); }

    @PostMapping("/{code}/players")
    public ResponseEntity<JoinView> join(@PathVariable("code") String code, @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody JoinRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.join(code, user, request));
    }

    @PostMapping("/{code}/start")
    public GameStateView start(@PathVariable("code") String code, @AuthenticationPrincipal AuthUser user) {
        return service.start(code, user);
    }

    @GetMapping("/{code}/state")
    public GameStateView state(@PathVariable("code") String code) { return service.state(code); }
}
