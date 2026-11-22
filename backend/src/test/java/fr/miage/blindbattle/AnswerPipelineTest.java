package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.AnswerRepository;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.PlayerRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import fr.miage.blindbattle.service.CatalogueService;
import java.time.Duration;
import java.util.UUID;
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
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:answers", "spring.jpa.hibernate.ddl-auto=create-drop",
        "blindbattle.round.pause=PT0.2S"})
@AutoConfigureMockMvc
class AnswerPipelineTest {
    private static final AtomicInteger USERS = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TrackRepository tracks;
    @Autowired GameRepository games;
    @Autowired PlayerRepository players;
    @Autowired AnswerRepository answers;
    @Autowired TransactionTemplate transaction;
    @MockitoBean CatalogueService catalogue;

    record CurrentRound(long id, int correctIndex, String title, RoundStatus status) {}

    @BeforeEach void fillCatalogue() {
        if (tracks.count() == 0) {
            IntStream.rangeClosed(1, 6).forEach(i -> tracks.save(
                    new TrackEntity(3000 + i, "Chanson numero " + i, "Interprete " + i, i, "Album", null, i * 1000, 30, true)));
        }
        when(catalogue.pool(anyString(), any())).thenAnswer(invocation -> tracks.findAll());
    }

    @Test void roundClosesAsSoonAsEveryoneAnswered() throws Exception {
        String host = bearer();
        String code = createGame(host, "QUICK_CHOICE", "TITLE");
        String ana = join(code, "Ana");
        String tom = join(code, "Tom");
        start(code, host);
        CurrentRound round = current(code);
        answer(code, ana, "{\"choiceIndex\":" + round.correctIndex() + "}", null);
        answer(code, tom, "{\"choiceIndex\":" + ((round.correctIndex() + 1) % 4) + "}", null);
        await().atMost(Duration.ofSeconds(8)).until(() -> roundStatus(round.id()) == RoundStatus.ENDED);
        assertThat(players.findByToken(UUID.fromString(ana)).orElseThrow().getScore()).isBetween(500, 1000);
        assertThat(players.findByToken(UUID.fromString(tom)).orElseThrow().getScore()).isZero();
        await().atMost(Duration.ofSeconds(5)).until(() -> current(code).id() != round.id());
    }

    @Test void retriesAndSecondAnswersCountOnce() throws Exception {
        String host = bearer();
        String code = createGame(host, "QUICK_CHOICE", "TITLE");
        String ana = join(code, "Ana");
        String tom = join(code, "Tom");
        start(code, host);
        CurrentRound round = current(code);
        String key = UUID.randomUUID().toString();
        answer(code, ana, "{\"choiceIndex\":" + round.correctIndex() + "}", key);
        answer(code, ana, "{\"choiceIndex\":" + round.correctIndex() + "}", key);
        answer(code, ana, "{\"choiceIndex\":" + ((round.correctIndex() + 2) % 4) + "}", null);
        answer(code, tom, "{\"choiceIndex\":" + round.correctIndex() + "}", null);
        await().atMost(Duration.ofSeconds(8)).until(() -> roundStatus(round.id()) == RoundStatus.ENDED);
        assertThat(answers.countByRoundId(round.id())).isEqualTo(2);
        int anaScore = players.findByToken(UUID.fromString(ana)).orElseThrow().getScore();
        assertThat(anaScore).isBetween(500, 1000);
    }

    @Test void typedAnswerWithATypoIsAccepted() throws Exception {
        String host = bearer();
        String code = createGame(host, "TYPE_IT", "TITLE");
        String lea = join(code, "Lea");
        start(code, host);
        CurrentRound round = current(code);
        answer(code, lea, "{\"text\":\"" + round.title().replace("numero", "numro") + "\"}", null);
        await().atMost(Duration.ofSeconds(8)).until(() -> roundStatus(round.id()) == RoundStatus.ENDED);
        assertThat(players.findByToken(UUID.fromString(lea)).orElseThrow().getScore()).isPositive();
    }

    @Test void rejectsInvalidSubmissions() throws Exception {
        String host = bearer();
        String code = createGame(host, "QUICK_CHOICE", "TITLE");
        String ana = join(code, "Ana");
        mvc.perform(post("/games/" + code + "/answers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerToken\":\"" + ana + "\",\"choiceIndex\":1}"))
            .andExpect(status().isConflict());
        start(code, host);
        mvc.perform(post("/games/" + code + "/answers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerToken\":\"" + UUID.randomUUID() + "\",\"choiceIndex\":1}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/games/" + code + "/answers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerToken\":\"" + ana + "\",\"choiceIndex\":7}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.choiceIndex").exists());
    }

    private void answer(String code, String playerToken, String fields, String key) throws Exception {
        String body = "{\"playerToken\":\"" + playerToken + "\"," + fields.substring(1);
        var request = post("/games/" + code + "/answers").contentType(MediaType.APPLICATION_JSON).content(body);
        if (key != null) request.header("Idempotency-Key", key);
        mvc.perform(request).andExpect(status().isAccepted()).andExpect(jsonPath("$.eventId").exists());
    }

    private CurrentRound current(String code) {
        return transaction.execute(s -> {
            RoundEntity round = games.findByCode(code).orElseThrow().getRounds().stream()
                    .filter(r -> r.getStatus() == RoundStatus.PLAYING).findFirst().orElseThrow();
            return new CurrentRound(round.getId(), round.getChoiceTrackIds().indexOf(round.getTrack().getId()),
                    round.getTrack().getTitle(), round.getStatus());
        });
    }

    private RoundStatus roundStatus(long roundId) {
        return transaction.execute(s -> games.findAll().stream().flatMap(g -> g.getRounds().stream())
                .filter(r -> r.getId() == roundId).findFirst().orElseThrow().getStatus());
    }

    private void start(String code, String host) throws Exception {
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
    }

    private String createGame(String host, String mode, String target) throws Exception {
        String body = mvc.perform(post("/games").header("Authorization", host).contentType(MediaType.APPLICATION_JSON)
                .content("{\"settings\":{\"answerMode\":\"" + mode + "\",\"guessTarget\":\"" + target + "\",\"roundCount\":3,"
                        + "\"roundSeconds\":20,\"difficulty\":\"EASY\",\"themeId\":\"chart-world\"}}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.code");
    }

    private String join(String code, String nickname) throws Exception {
        String body = mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"" + nickname + "\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.playerToken");
    }

    private String bearer() throws Exception {
        String email = "animateur" + USERS.incrementAndGet() + "@example.com";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Animateur\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
