package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import fr.miage.blindbattle.domain.GameStatus;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.GameResultRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import fr.miage.blindbattle.service.CatalogueService;
import fr.miage.blindbattle.service.GameResultService;
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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:results", "spring.jpa.hibernate.ddl-auto=create-drop",
        "blindbattle.round.pause=PT0.2S"})
@AutoConfigureMockMvc
class GameResultsTest {
    private static final AtomicInteger USERS = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TrackRepository tracks;
    @Autowired GameRepository games;
    @Autowired GameResultRepository results;
    @Autowired GameResultService resultService;
    @Autowired TransactionTemplate transaction;
    @MockitoBean CatalogueService catalogue;

    @BeforeEach void fillCatalogue() {
        if (tracks.count() == 0) {
            IntStream.rangeClosed(1, 6).forEach(i -> tracks.save(
                    new TrackEntity(4000 + i, "Tube " + i, "Groupe " + i, i, "Album", null, i * 1000, 30, true)));
        }
        when(catalogue.pool(anyString(), any())).thenAnswer(invocation -> tracks.findAll());
    }

    @Test void finishedGameIsSplitIntoPlayerResultsAndStatistics() throws Exception {
        String host = bearer("Animateur");
        String ana = bearer("Ana");
        long anaId = ((Number) JsonPath.read(mvc.perform(get("/users/me").header("Authorization", ana))
                .andReturn().getResponse().getContentAsString(), "$.id")).longValue();
        String code = createGame(host);
        String anaToken = join(code, "Ana", ana);
        String guestToken = join(code, "Invite", null);
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());

        long previous = -1;
        for (int i = 0; i < 3; i++) {
            long last = previous;
            await().atMost(Duration.ofSeconds(8)).until(() -> playingRound(code) != null && playingRound(code)[0] != last);
            long[] round = playingRound(code);
            answer(code, anaToken, (int) round[1]);
            answer(code, guestToken, (int) ((round[1] + 1) % 4));
            previous = round[0];
        }

        await().atMost(Duration.ofSeconds(10)).until(() -> games.findByCode(code).orElseThrow().getStatus() == GameStatus.FINISHED);
        await().atMost(Duration.ofSeconds(10)).until(() -> results.findPodium(code).size() == 2);

        mvc.perform(get("/games/" + code + "/results"))
            .andExpect(jsonPath("$[0].nickname").value("Ana"))
            .andExpect(jsonPath("$[0].rank").value(1))
            .andExpect(jsonPath("$[0].correctAnswers").value(3))
            .andExpect(jsonPath("$[1].nickname").value("Invite"))
            .andExpect(jsonPath("$[1].finalScore").value(0));
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> mvc.perform(get("/users/" + anaId + "/stats"))
            .andExpect(jsonPath("$.gamesPlayed").value(1))
            .andExpect(jsonPath("$.bestScore").value(greaterThanOrEqualTo(1500)))
            .andExpect(jsonPath("$.badges[*].code").value(hasItems("FIRST_GAME", "WINNER"))));
        mvc.perform(get("/users/" + anaId + "/history"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].gameCode").value(code))
            .andExpect(jsonPath("$.content[0].playerCount").value(2))
            .andExpect(jsonPath("$.content[0].totalRounds").value(3));
        mvc.perform(get("/leaderboard"))
            .andExpect(jsonPath("$.content[?(@.name == 'Ana')].wins").value(hasItems(1)));

        resultService.split(code, UUID.randomUUID()).forEach(resultService::record);
        assertThat(results.findPodium(code)).hasSize(2);
        mvc.perform(get("/users/" + anaId + "/stats")).andExpect(jsonPath("$.gamesPlayed").value(1));
    }

    @Test void unknownGameHasNoResults() throws Exception {
        mvc.perform(get("/games/ZZZZZZ/results")).andExpect(status().isNotFound());
        mvc.perform(get("/users/999999/history")).andExpect(status().isNotFound());
    }

    private long[] playingRound(String code) {
        return transaction.execute(s -> games.findByCode(code).orElseThrow().getRounds().stream()
                .filter(r -> r.getStatus() == RoundStatus.PLAYING)
                .map(r -> new long[] {r.getId(), correctIndex(r)}).findFirst().orElse(null));
    }

    private long correctIndex(RoundEntity round) { return round.getChoiceTrackIds().indexOf(round.getTrack().getId()); }

    private void answer(String code, String playerToken, int choiceIndex) throws Exception {
        mvc.perform(post("/games/" + code + "/answers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerToken\":\"" + playerToken + "\",\"choiceIndex\":" + choiceIndex + "}"))
            .andExpect(status().isAccepted());
    }

    private String createGame(String host) throws Exception {
        String body = mvc.perform(post("/games").header("Authorization", host).contentType(MediaType.APPLICATION_JSON)
                .content("{\"settings\":{\"answerMode\":\"QUICK_CHOICE\",\"guessTarget\":\"TITLE\",\"roundCount\":3,"
                        + "\"roundSeconds\":20,\"difficulty\":\"EASY\",\"themeId\":\"chart-world\"}}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.code");
    }

    private String join(String code, String nickname, String bearer) throws Exception {
        var request = post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"" + nickname + "\"}");
        if (bearer != null) request.header("Authorization", bearer);
        String body = mvc.perform(request).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.playerToken");
    }

    private String bearer(String name) throws Exception {
        String email = "resultat" + USERS.incrementAndGet() + "@example.com";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
