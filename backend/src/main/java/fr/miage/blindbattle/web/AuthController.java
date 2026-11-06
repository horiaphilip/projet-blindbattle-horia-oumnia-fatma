package fr.miage.blindbattle.web;

import fr.miage.blindbattle.dto.LoginRequest;
import fr.miage.blindbattle.dto.LoginView;
import fr.miage.blindbattle.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    public LoginView login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
}
