package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.dto.UserRequest;
import fr.miage.blindbattle.dto.UserUpdateRequest;
import fr.miage.blindbattle.dto.UserView;
import fr.miage.blindbattle.messaging.Destinations;
import fr.miage.blindbattle.messaging.UserEvent;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.security.AuthUser;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository repository;
    private final OutboxService outbox;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, OutboxService outbox, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.outbox = outbox;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserView> list() { return repository.findAll(Sort.by("id")).stream().map(this::view).toList(); }

    @Transactional(readOnly = true)
    public UserView get(long id) { return view(required(id)); }

    @Transactional
    public UserView create(UserRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) throw new EmailAlreadyUsed(email);
        UserEntity user = repository.save(new UserEntity(request.name().strip(), email, passwordEncoder.encode(request.password())));
        UserEvent event = UserEvent.created(user);
        outbox.record(Destinations.USER_EVENTS, event.eventId(), event.type().name(), event);
        return view(user);
    }

    @Transactional
    public UserView update(long id, AuthUser current, UserUpdateRequest request) {
        checkOwner(id, current);
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailAndIdNot(email, id)) throw new EmailAlreadyUsed(email);
        UserEntity user = required(id);
        user.update(request.name().strip(), email);
        return view(repository.saveAndFlush(user));
    }

    @Transactional
    public void delete(long id, AuthUser current) {
        checkOwner(id, current);
        UserEntity user = required(id);
        UserEvent event = UserEvent.deleted(user);
        repository.delete(user);
        outbox.record(Destinations.USER_EVENTS, event.eventId(), event.type().name(), event);
    }

    public UserView view(UserEntity user) {
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt(), user.getUpdatedAt());
    }

    private void checkOwner(long id, AuthUser current) { if (current.id() != id) throw new NotOwner(); }

    private UserEntity required(long id) { return repository.findById(id).orElseThrow(() -> new UserNotFound(id)); }

    private String normalizeEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}
