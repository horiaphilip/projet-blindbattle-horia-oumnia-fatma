package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.AnswerAccepted;
import fr.miage.blindbattle.dto.AnswerRequest;
import fr.miage.blindbattle.dto.GameRequest;
import fr.miage.blindbattle.dto.GameStateView;
import fr.miage.blindbattle.dto.GameView;
import fr.miage.blindbattle.dto.JoinRequest;
import fr.miage.blindbattle.dto.JoinView;
import fr.miage.blindbattle.dto.PodiumEntryView;
import fr.miage.blindbattle.security.AuthUser;
import fr.miage.blindbattle.service.AnswerService;
import fr.miage.blindbattle.service.GameResultService;
import fr.miage.blindbattle.service.GameService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService service;
    private final AnswerService answers;
    private final GameResultService results;

    public GameController(GameService service, AnswerService answers, GameResultService results) {
        this.service = service;
        this.answers = answers;
        this.results = results;
    }

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

    @GetMapping("/{code}/results")
    public List<PodiumEntryView> results(@PathVariable("code") String code) { return results.podium(code); }

    @PostMapping("/{code}/answers")
    public ResponseEntity<AnswerAccepted> answer(@PathVariable("code") String code,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody AnswerRequest request) {
        return ResponseEntity.accepted().body(answers.submit(code, request, idempotencyKey));
    }

    @GetMapping(value = "/{code}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable("code") String code) { return service.subscribe(code); }
}
