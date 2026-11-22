package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.TrackRepository;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:quizzes", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class QuizApiTest {
    private static final AtomicInteger USERS = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TrackRepository tracks;

    long first;
    long second;
    long third;
    long silent;
    String ana;
    String tom;

    @BeforeEach void setUp() throws Exception {
        first = track(1001, "Alors on danse", "Stromae", true);
        second = track(1002, "Dernière danse", "Indila", true);
        third = track(1003, "Djadja", "Aya Nakamura", true);
        silent = track(1004, "Sans extrait", "Inconnu", false);
        ana = signUpAndLogIn("Ana");
        tom = signUpAndLogIn("Tom");
    }

    @Test void createQuiz() throws Exception {
        String location = mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Années 2010", "PRIVATE", first, second, third)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Années 2010"))
            .andExpect(jsonPath("$.ownerName").value("Ana"))
            .andExpect(jsonPath("$.shareToken").isString())
            .andExpect(jsonPath("$.trackCount").value(3))
            .andExpect(jsonPath("$.tracks[0].position").value(1))
            .andExpect(jsonPath("$.tracks[0].trackId").value(first))
            .andExpect(jsonPath("$.tracks[2].position").value(3))
            .andExpect(jsonPath("$.tracks[2].title").value("Djadja"))
            .andReturn().getResponse().getHeader("Location");
        assertThat(location).matches("/api/quizzes/\\d+");
    }

    @Test void keepFirstOccurrenceOfDuplicateTracks() throws Exception {
        mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Doublons", "PRIVATE", first, second, first)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trackCount").value(2))
            .andExpect(jsonPath("$.tracks[0].trackId").value(first))
            .andExpect(jsonPath("$.tracks[1].trackId").value(second));
    }

    @Test void listOnlyMyQuizzes() throws Exception {
        create(ana, quiz("Quiz d'Ana 1", "PRIVATE", first));
        create(ana, quiz("Quiz d'Ana 2", "PUBLIC", first, second));
        create(tom, quiz("Quiz de Tom", "PUBLIC", third));
        mvc.perform(get("/quizzes").header("Authorization", ana))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Quiz d'Ana 2"))
            .andExpect(jsonPath("$.content[0].trackCount").value(2))
            .andExpect(jsonPath("$.content[1].title").value("Quiz d'Ana 1"));
        mvc.perform(get("/quizzes?page=0&size=1").header("Authorization", ana))
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test void ownerUpdates() throws Exception {
        long id = create(ana, quiz("Avant", "PRIVATE", first, second));
        mvc.perform(put("/quizzes/" + id).header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Après", "PUBLIC", third, first)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Après"))
            .andExpect(jsonPath("$.visibility").value("PUBLIC"))
            .andExpect(jsonPath("$.trackCount").value(2))
            .andExpect(jsonPath("$.tracks[0].trackId").value(third))
            .andExpect(jsonPath("$.tracks[0].position").value(1))
            .andExpect(jsonPath("$.tracks[1].trackId").value(first))
            .andExpect(jsonPath("$.tracks[1].position").value(2))
            .andExpect(jsonPath("$.updatedAt").isString());
        mvc.perform(get("/quizzes/" + id).header("Authorization", ana))
            .andExpect(jsonPath("$.tracks[0].trackId").value(third))
            .andExpect(jsonPath("$.tracks[1].trackId").value(first));
    }

    @Test void otherUserCannotUpdate() throws Exception {
        long id = create(ana, quiz("Le mien", "PUBLIC", first));
        mvc.perform(put("/quizzes/" + id).header("Authorization", tom).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Volé", "PUBLIC", first)))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/quizzes/" + id).header("Authorization", tom)).andExpect(status().isForbidden());
    }

    @Test void otherUserCannotReadPrivate() throws Exception {
        long id = create(ana, quiz("Secret", "PRIVATE", first));
        mvc.perform(get("/quizzes/" + id).header("Authorization", tom)).andExpect(status().isForbidden());
    }

    @Test void anyoneReadsPublic() throws Exception {
        long id = create(ana, quiz("Public", "PUBLIC", first));
        mvc.perform(get("/quizzes/" + id).header("Authorization", tom))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Public"));
    }

    @Test void sharedLinkWorksWithoutToken() throws Exception {
        String body = mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Partagé", "PRIVATE", first, second)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.shareToken");
        mvc.perform(get("/quizzes/shared/" + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Partagé"))
            .andExpect(jsonPath("$.trackCount").value(2));
        mvc.perform(get("/quizzes/shared/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test void ownerDeletes() throws Exception {
        long id = create(ana, quiz("À supprimer", "PRIVATE", first, second));
        mvc.perform(delete("/quizzes/" + id).header("Authorization", ana)).andExpect(status().isNoContent());
        mvc.perform(get("/quizzes/" + id).header("Authorization", ana)).andExpect(status().isNotFound());
    }

    @Test void rejectInvalidQuiz() throws Exception {
        mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz(" ", "PRIVATE", first)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").value("Le titre est obligatoire"));
    }

    @Test void unknownTrack() throws Exception {
        mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Fantôme", "PRIVATE", first, 999999)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Morceau 999999 introuvable"));
    }

    @Test void trackWithoutPreviewIsFlagged() throws Exception {
        mvc.perform(post("/quizzes").header("Authorization", ana).contentType(MediaType.APPLICATION_JSON)
                .content(quiz("Avec un muet", "PRIVATE", first, silent)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trackCount").value(2))
            .andExpect(jsonPath("$.playableTrackCount").value(1))
            .andExpect(jsonPath("$.tracks[1].hasPreview").value(false));
    }

    @Test void needsToken() throws Exception {
        mvc.perform(post("/quizzes").contentType(MediaType.APPLICATION_JSON).content(quiz("Anonyme", "PUBLIC", first)))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/quizzes")).andExpect(status().isUnauthorized());
    }

    private long track(long deezerId, String title, String artist, boolean hasPreview) {
        return tracks.findByDeezerId(deezerId)
                .orElseGet(() -> tracks.save(new TrackEntity(deezerId, title, artist, deezerId, "Album", null, 1, 180, hasPreview)))
                .getId();
    }

    private String quiz(String title, String visibility, long... trackIds) {
        String ids = String.join(",", Arrays.stream(trackIds).mapToObj(String::valueOf).toList());
        return "{\"title\":\"" + title + "\",\"description\":\"Un quiz\",\"visibility\":\"" + visibility
                + "\",\"trackIds\":[" + ids + "]}";
    }

    private long create(String token, String body) throws Exception {
        String response = mvc.perform(post("/quizzes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private String signUpAndLogIn(String name) throws Exception {
        String email = name.toLowerCase() + ".quiz" + USERS.incrementAndGet() + "@example.com";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
