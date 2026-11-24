package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import fr.miage.blindbattle.service.CatalogueService;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:games", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class GameApiTest {
    private static final AtomicInteger USERS = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TrackRepository tracks;
    @Autowired GameRepository games;
    @Autowired TransactionTemplate transaction;
    @MockitoBean CatalogueService catalogue;

    @BeforeEach void fillCatalogue() {
        if (tracks.count() == 0) {
            IntStream.rangeClosed(1, 5).forEach(i -> tracks.save(
                    new TrackEntity(1000 + i, "Titre " + i, "Artiste " + i, i, "Album", null, i * 1000, 30, true)));
        }
        when(catalogue.pool(anyString(), any())).thenAnswer(invocation -> tracks.findAll());
        when(catalogue.freshPreview(anyLong())).thenAnswer(invocation -> "https://cdn.example/" + invocation.getArgument(0) + ".mp3");
    }

    @Test void hostCreatesGame() throws Exception {
        String host = bearer("Ana");
        mvc.perform(post("/games").header("Authorization", host).contentType(MediaType.APPLICATION_JSON).content(settings(3)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", startsWith("/api/games/")))
            .andExpect(jsonPath("$.code").value(matchesPattern("[A-HJ-NP-Z2-9]{6}")))
            .andExpect(jsonPath("$.status").value("LOBBY"))
            .andExpect(jsonPath("$.hostName").value("Ana"))
            .andExpect(jsonPath("$.settings.answerMode").value("QUICK_CHOICE"))
            .andExpect(jsonPath("$.players").isEmpty());
    }

    @Test void createNeedsToken() throws Exception {
        mvc.perform(post("/games").contentType(MediaType.APPLICATION_JSON).content(settings(3)))
            .andExpect(status().isUnauthorized());
    }

    @Test void rejectInvalidSettings() throws Exception {
        mvc.perform(post("/games").header("Authorization", bearer("Tom")).contentType(MediaType.APPLICATION_JSON)
                .content(settings(50)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors['settings.roundCount']").value("20 manches maximum"));
    }

    @Test void guestAndAccountJoin() throws Exception {
        String code = createGame(bearer("Lea"), 3);
        mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Invité\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.playerToken").isString());
        mvc.perform(post("/games/" + code + "/players").header("Authorization", bearer("Max"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Max\"}"))
            .andExpect(status().isCreated());
        mvc.perform(get("/games/" + code))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players.length()").value(2))
            .andExpect(jsonPath("$.players[0].guest").value(true))
            .andExpect(jsonPath("$.players[1].guest").value(false));
    }

    @Test void rejectDuplicateNickname() throws Exception {
        String code = createGame(bearer("Zoe"), 3);
        join(code, "Sam");
        mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"SAM\"}"))
            .andExpect(status().isConflict());
    }

    @Test void onlyHostCanStart() throws Exception {
        String code = createGame(bearer("Eva"), 3);
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", bearer("Intrus")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/games/" + code + "/start")).andExpect(status().isUnauthorized());
    }

    @Test void cannotStartWithoutPlayers() throws Exception {
        String host = bearer("Seul");
        String code = createGame(host, 3);
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isConflict());
    }

    @Test void startCreatesRoundsAndClosesTheLobby() throws Exception {
        String host = bearer("Hote");
        String code = createGame(host, 3);
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PLAYING"))
            .andExpect(jsonPath("$.totalRounds").value(3))
            .andExpect(jsonPath("$.playerCount").value(1))
            .andExpect(jsonPath("$.serverTime").exists());
        mvc.perform(get("/games/" + code + "/state")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PLAYING"));
        mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Retard\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isConflict());
    }

    @Test void quickChoiceRoundsGetFourChoicesWithTheAnswer() throws Exception {
        String host = bearer("Choix");
        String code = createGame(host, 3);
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
        transaction.executeWithoutResult(status -> {
            for (RoundEntity round : games.findByCode(code).orElseThrow().getRounds()) {
                assertThat(round.getChoiceTrackIds()).hasSize(4).doesNotHaveDuplicates().contains(round.getTrack().getId());
            }
        });
    }

    @Test void rejectGameWithMoreRoundsThanTracks() throws Exception {
        String host = bearer("Gourmand");
        String code = createGame(host, 10);
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("5 disponibles")));
    }

    @Test void unknownGame() throws Exception {
        mvc.perform(get("/games/ZZZZZZ")).andExpect(status().isNotFound());
        mvc.perform(post("/games/ZZZZZZ/players").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"Ana\"}"))
            .andExpect(status().isNotFound());
    }

    @Test void hostDeletingAccountRemovesStartedGame() throws Exception {
        String host = bearer("Partant");
        String code = createGame(host, 3);
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
        String me = mvc.perform(get("/users/me").header("Authorization", host)).andReturn().getResponse().getContentAsString();
        mvc.perform(delete("/users/" + JsonPath.read(me, "$.id")).header("Authorization", host)).andExpect(status().isNoContent());
        mvc.perform(get("/games/" + code)).andExpect(status().isNotFound());
    }

    @Test void rejectGameWithoutThemeOrQuiz() throws Exception {
        mvc.perform(post("/games").header("Authorization", bearer("Vide")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"settings\":{\"answerMode\":\"QUICK_CHOICE\",\"guessTarget\":\"TITLE\",\"roundCount\":3,"
                        + "\"roundSeconds\":20,\"difficulty\":\"EASY\"}}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Choisissez un thème ou un quiz"));
    }

    @Test void quizGamePlaysTheQuizTracksInOrder() throws Exception {
        String host = bearer("Createur");
        List<Long> ids = trackIds();
        List<Long> order = List.of(ids.get(3), ids.get(0), ids.get(4), ids.get(1));
        long quizId = createQuiz(host, "PRIVATE", order);
        String code = createGame(host, quizSettings(quizId, 3));
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
        transaction.executeWithoutResult(status -> assertThat(games.findByCode(code).orElseThrow().getRounds())
                .extracting(round -> round.getTrack().getId()).containsExactlyElementsOf(order.subList(0, 3)));
    }

    @Test void privateQuizOfAnotherUserCannotBePlayed() throws Exception {
        long privateQuiz = createQuiz(bearer("Prive"), "PRIVATE", trackIds().subList(0, 4));
        long publicQuiz = createQuiz(bearer("Public"), "PUBLIC", trackIds().subList(0, 4));
        String other = bearer("Autre");
        mvc.perform(post("/games").header("Authorization", other).contentType(MediaType.APPLICATION_JSON)
                .content(quizSettings(privateQuiz, 3)))
            .andExpect(status().isForbidden());
        mvc.perform(post("/games").header("Authorization", other).contentType(MediaType.APPLICATION_JSON)
                .content(quizSettings(publicQuiz, 3)))
            .andExpect(status().isCreated());
        mvc.perform(post("/games").header("Authorization", other).contentType(MediaType.APPLICATION_JSON)
                .content(quizSettings(999999, 3)))
            .andExpect(status().isNotFound());
    }

    @Test void quizShorterThanRoundCountCannotStart() throws Exception {
        String host = bearer("Court");
        String code = createGame(host, quizSettings(createQuiz(host, "PRIVATE", trackIds().subList(0, 4)), 5));
        join(code, "Joueur");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("4 disponibles")));
    }

    @Test void onlyHostGetsTheCurrentPreview() throws Exception {
        String host = bearer("Platine");
        String code = createGame(host, 3);
        join(code, "Joueur");
        mvc.perform(get("/games/" + code + "/rounds/current/preview").header("Authorization", host))
            .andExpect(status().isConflict());
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
        long deezerId = transaction.execute(status -> games.findByCode(code).orElseThrow().getRounds().getFirst()
                .getTrack().getDeezerId());
        mvc.perform(get("/games/" + code + "/rounds/current/preview").header("Authorization", host))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roundNumber").value(1))
            .andExpect(jsonPath("$.previewUrl").value("https://cdn.example/" + deezerId + ".mp3"));
        mvc.perform(get("/games/" + code + "/rounds/current/preview").header("Authorization", bearer("Curieux")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/games/" + code + "/rounds/current/preview")).andExpect(status().isForbidden());
    }

    private List<Long> trackIds() {
        return tracks.findAll().stream().map(TrackEntity::getId).sorted().toList();
    }

    private long createQuiz(String owner, String visibility, List<Long> trackIds) throws Exception {
        String ids = String.join(",", trackIds.stream().map(String::valueOf).toList());
        String body = mvc.perform(post("/quizzes").header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Mon quiz\",\"visibility\":\"" + visibility + "\",\"trackIds\":[" + ids + "]}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String quizSettings(long quizId, int roundCount) {
        return "{\"settings\":{\"answerMode\":\"QUICK_CHOICE\",\"guessTarget\":\"TITLE\",\"roundCount\":" + roundCount
                + ",\"roundSeconds\":20,\"difficulty\":\"EASY\",\"quizId\":" + quizId + "}}";
    }

    private String settings(int roundCount) {
        return "{\"settings\":{\"answerMode\":\"QUICK_CHOICE\",\"guessTarget\":\"TITLE\",\"roundCount\":" + roundCount
                + ",\"roundSeconds\":20,\"difficulty\":\"EASY\",\"themeId\":\"chart-world\"}}";
    }

    private String createGame(String host, int roundCount) throws Exception { return createGame(host, settings(roundCount)); }

    private String createGame(String host, String settings) throws Exception {
        String body = mvc.perform(post("/games").header("Authorization", host).contentType(MediaType.APPLICATION_JSON)
                .content(settings))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.code");
    }

    private void join(String code, String nickname) throws Exception {
        mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"" + nickname + "\"}"))
            .andExpect(status().isCreated());
    }

    private String bearer(String name) throws Exception {
        String email = "joueur" + USERS.incrementAndGet() + "@example.com";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
