package fr.miage.blindbattle;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:auth", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class AuthApiTest {
    @Autowired MockMvc mvc;

    @Test void loginReturnsTokenAndUser() throws Exception {
        signUp("Ana", "ana.auth@example.com");
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"Ana.Auth@example.com\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.user.email").value("ana.auth@example.com"))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test void rejectWrongPasswordAndUnknownEmailTheSameWay() throws Exception {
        signUp("Tom", "tom.auth@example.com");
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tom.auth@example.com\",\"password\":\"mauvais-mot\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"personne@example.com\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect"));
    }

    @Test void meNeedsToken() throws Exception {
        signUp("Lea", "lea.auth@example.com");
        mvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/me").header("Authorization", "Bearer pas-un-jeton")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/me").header("Authorization", bearer("lea.auth@example.com")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Lea"));
    }

    @Test void cannotModifySomeoneElse() throws Exception {
        long victim = signUp("Zoe", "zoe.auth@example.com");
        signUp("Max", "max.auth@example.com");
        String max = bearer("max.auth@example.com");
        mvc.perform(put("/users/" + victim).header("Authorization", max).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Pirate\",\"email\":\"pirate@example.com\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/users/" + victim).header("Authorization", max)).andExpect(status().isForbidden());
        mvc.perform(delete("/users/" + victim)).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/" + victim)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Zoe"));
    }

    @Test void ownerCanDelete() throws Exception {
        long id = signUp("Eva", "eva.auth@example.com");
        mvc.perform(delete("/users/" + id).header("Authorization", bearer("eva.auth@example.com")))
            .andExpect(status().isNoContent());
    }

    private long signUp(String name, String email) throws Exception {
        String body = mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String bearer(String email) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
