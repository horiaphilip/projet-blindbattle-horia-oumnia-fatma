package fr.miage.blindbattle.service;

import fr.miage.blindbattle.domain.QuizEntity;
import fr.miage.blindbattle.domain.QuizTrackEntity;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.domain.UserEntity;
import fr.miage.blindbattle.dto.PageView;
import fr.miage.blindbattle.dto.QuizRequest;
import fr.miage.blindbattle.dto.QuizSummaryView;
import fr.miage.blindbattle.dto.QuizTrackView;
import fr.miage.blindbattle.dto.QuizView;
import fr.miage.blindbattle.repository.QuizRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import fr.miage.blindbattle.repository.UserRepository;
import fr.miage.blindbattle.security.AuthUser;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    private static final int MAX_PAGE_SIZE = 50;

    private final QuizRepository repository;
    private final TrackRepository tracks;
    private final UserRepository users;

    public QuizService(QuizRepository repository, TrackRepository tracks, UserRepository users) {
        this.repository = repository;
        this.tracks = tracks;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageView<QuizSummaryView> mine(AuthUser user, int page, int size) {
        PageRequest request = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<QuizEntity> quizzes = repository.findByOwnerId(user.id(), request);
        return new PageView<>(quizzes.getContent().stream().map(this::summary).toList(), quizzes.getNumber(),
                quizzes.getSize(), quizzes.getTotalElements(), quizzes.getTotalPages());
    }

    @Transactional
    public QuizView create(AuthUser user, QuizRequest request) {
        UserEntity owner = users.findById(user.id()).orElseThrow(() -> new UserNotFound(user.id()));
        QuizEntity quiz = new QuizEntity(owner, request.title().strip(), description(request), request.visibility());
        quiz.replaceTracks(load(request.trackIds()));
        return view(repository.save(quiz));
    }

    @Transactional(readOnly = true)
    public QuizView get(long id, AuthUser userOrNull) {
        QuizEntity quiz = requiredWithTracks(id);
        if (!quiz.isPublic() && (userOrNull == null || !quiz.isOwnedBy(userOrNull.id()))) throw new NotQuizOwner();
        return view(quiz);
    }

    @Transactional
    public QuizView update(long id, AuthUser user, QuizRequest request) {
        QuizEntity quiz = requiredWithTracks(id);
        checkOwner(quiz, user);
        quiz.update(request.title().strip(), description(request), request.visibility());
        quiz.replaceTracks(load(request.trackIds()));
        return view(repository.saveAndFlush(quiz));
    }

    @Transactional
    public void delete(long id, AuthUser user) {
        QuizEntity quiz = repository.findById(id).orElseThrow(() -> new QuizNotFound(id));
        checkOwner(quiz, user);
        repository.delete(quiz);
    }

    @Transactional(readOnly = true)
    public QuizView shared(UUID token) { return view(repository.findWithTracksByShareToken(token).orElseThrow(QuizNotFound::new)); }

    private List<TrackEntity> load(List<Long> trackIds) {
        List<Long> ids = trackIds.stream().distinct().toList();
        Map<Long, TrackEntity> found = tracks.findAllById(ids).stream()
                .collect(Collectors.toMap(TrackEntity::getId, Function.identity()));
        ids.stream().filter(id -> !found.containsKey(id)).findFirst().ifPresent(id -> { throw new TrackNotFound(id); });
        return ids.stream().map(found::get).toList();
    }

    private void checkOwner(QuizEntity quiz, AuthUser user) { if (!quiz.isOwnedBy(user.id())) throw new NotQuizOwner(); }

    private QuizEntity requiredWithTracks(long id) { return repository.findWithTracksById(id).orElseThrow(() -> new QuizNotFound(id)); }

    private String description(QuizRequest request) {
        return request.description() == null || request.description().isBlank() ? null : request.description().strip();
    }

    private QuizSummaryView summary(QuizEntity quiz) {
        return new QuizSummaryView(quiz.getId(), quiz.getTitle(), quiz.getVisibility().name(), quiz.getTrackCount(),
                quiz.getCreatedAt());
    }

    private QuizView view(QuizEntity quiz) {
        List<QuizTrackView> entries = quiz.getTracks().stream().map(this::view).toList();
        int playable = (int) entries.stream().filter(QuizTrackView::hasPreview).count();
        return new QuizView(quiz.getId(), quiz.getTitle(), quiz.getDescription(), quiz.getVisibility().name(),
                quiz.getShareToken(), quiz.getOwner().getId(), quiz.getOwner().getName(), entries.size(), playable,
                quiz.getCreatedAt(), quiz.getUpdatedAt(), entries);
    }

    private QuizTrackView view(QuizTrackEntity entry) {
        TrackEntity track = entry.getTrack();
        return new QuizTrackView(entry.getPosition(), track.getId(), track.getTitle(), track.getArtist(),
                track.getCoverUrl(), track.hasPreview());
    }
}
