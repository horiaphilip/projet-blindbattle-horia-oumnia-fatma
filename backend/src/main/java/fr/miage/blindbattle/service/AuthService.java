package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.dto.LoginRequest;
import fr.miage.blindbattle.dto.LoginView;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.security.TokenService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final UserService userService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens, UserService userService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public LoginView login(LoginRequest request) {
        UserEntity user = users.findByEmail(request.email().strip().toLowerCase(Locale.ROOT))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentials::new);
        return new LoginView(tokens.issue(user), userService.view(user));
    }
}
