package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import fr.miage.blindbattle.domain.GameStatus;
import fr.miage.blindbattle.domain.RoundEntity;
import fr.miage.blindbattle.domain.RoundStatus;
import fr.miage.blindbattle.domain.TrackEntity;
import fr.miage.blindbattle.repository.GameRepository;
import fr.miage.blindbattle.repository.TrackRepository;
import fr.miage.blindbattle.service.CatalogueService;
import fr.miage.blindbattle.service.RoundService;
import java.time.Duration;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:stream", "spring.jpa.hibernate.ddl-auto=create-drop",
        "blindbattle.round.pause=PT0.2S"})
@AutoConfigureMockMvc
class GameStreamTest {
    private static final AtomicInteger USERS = new AtomicInteger();

    @Autowired MockMvc mvc;
    @Autowired TrackRepository tracks;
    @Autowired GameRepository games;
    @Autowired RoundService roundService;
    @Autowired TransactionTemplate transaction;
    @MockitoBean CatalogueService catalogue;

    @BeforeEach void fillCatalogue() {
        if (tracks.count() == 0) {
            IntStream.rangeClosed(1, 5).forEach(i -> tracks.save(
                    new TrackEntity(2000 + i, "Morceau " + i, "Chanteur " + i, i, "Album", null, i * 1000, 30, true)));
        }
        when(catalogue.pool(anyString(), any())).thenAnswer(invocation -> tracks.findAll());
    }

    @Test void screensReceiveLiveEventsWithoutTheAnswer() throws Exception {
        String host = bearer();
        String code = createGame(host);
        MvcResult stream = mvc.perform(get("/games/" + code + "/stream")).andExpect(request().asyncStarted()).andReturn();
        join(code, "Ana");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(content(stream))
                .contains("event:PLAYER_JOINED").contains("\"nickname\":\"Ana\""));
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host)).andExpect(status().isOk());
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(content(stream))
                .contains("event:GAME_STARTED").contains("event:ROUND_STARTED").contains("\"endsAt\""));
        assertThat(content(stream)).doesNotContain("Morceau");
    }

    @Test void roundsFollowEachOtherUntilTheEnd() throws Exception {
        String host = bearer();
        String code = createGame(host);
        MvcResult stream = mvc.perform(get("/games/" + code + "/stream")).andExpect(request().asyncStarted()).andReturn();
        join(code, "Lea");
        mvc.perform(post("/games/" + code + "/start").header("Authorization", host))
            .andExpect(jsonPath("$.currentRound").value(1))
            .andExpect(jsonPath("$.endsAt").exists());
        for (long roundId : roundIds(code)) {
            await().atMost(Duration.ofSeconds(10)).until(() -> roundStatus(roundId) == RoundStatus.PLAYING);
            roundService.end(roundId);
            roundService.end(roundId);
            assertThat(roundStatus(roundId)).isEqualTo(RoundStatus.ENDED);
        }
        await().atMost(Duration.ofSeconds(10)).until(() -> games.findByCode(code).orElseThrow().getStatus() == GameStatus.FINISHED);
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(content(stream))
                .contains("event:ROUND_ENDED").contains("Morceau").contains("\"answered\":false")
                .contains("\"points\":0").contains("event:GAME_FINISHED"));
        mvc.perform(get("/games/" + code + "/state")).andExpect(jsonPath("$.status").value("FINISHED"))
            .andExpect(jsonPath("$.currentRound").value(3));
    }

    @Test void unknownGameHasNoStream() throws Exception {
        mvc.perform(get("/games/ZZZZZZ/stream")).andExpect(status().isNotFound());
    }

    private String content(MvcResult result) throws Exception { return result.getResponse().getContentAsString(); }

    private List<Long> roundIds(String code) {
        return transaction.execute(s -> games.findByCode(code).orElseThrow().getRounds().stream().map(RoundEntity::getId).toList());
    }

    private RoundStatus roundStatus(long roundId) {
        return transaction.execute(s -> games.findAll().stream().flatMap(g -> g.getRounds().stream())
                .filter(r -> r.getId() == roundId).findFirst().orElseThrow().getStatus());
    }

    private String createGame(String host) throws Exception {
        String body = mvc.perform(post("/games").header("Authorization", host).contentType(MediaType.APPLICATION_JSON)
                .content("{\"settings\":{\"answerMode\":\"TYPE_IT\",\"guessTarget\":\"BOTH\",\"roundCount\":3,"
                        + "\"roundSeconds\":10,\"difficulty\":\"EASY\",\"themeId\":\"chart-world\"}}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.code");
    }

    private void join(String code, String nickname) throws Exception {
        mvc.perform(post("/games/" + code + "/players").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"" + nickname + "\"}"))
            .andExpect(status().isCreated());
    }

    private String bearer() throws Exception {
        String email = "hote" + USERS.incrementAndGet() + "@example.com";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hote\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated());
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
