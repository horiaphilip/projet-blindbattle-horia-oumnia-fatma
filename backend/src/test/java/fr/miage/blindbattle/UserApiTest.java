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

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:users", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class UserApiTest {
    @Autowired MockMvc mvc;

    @Test void createReadUpdateDelete() throws Exception {
        String location = mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ana\",\"email\":\"Ana@Example.com\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andReturn().getResponse().getHeader("Location");
        String path = location.replace("/api", "");
        String token = "Bearer " + login("ana@example.com");
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Ana"));
        mvc.perform(put(path).header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Lea\",\"email\":\"lea@example.com\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Lea"));
        mvc.perform(get("/users")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.name == 'Lea')]").exists());
        mvc.perform(delete(path).header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @Test void rejectInvalidRequest() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"email\":\"pas-un-email\",\"password\":\"court\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test void rejectDuplicateEmail() throws Exception {
        String body = "{\"name\":\"Tom\",\"email\":\"tom@example.com\",\"password\":\"motdepasse\"}";
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"motdepasse\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
