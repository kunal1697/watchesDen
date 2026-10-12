package com.aeon.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIT {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    @Test
    void registerLoginAndFetchProfile() throws Exception {
        String register = "{\"fullName\":\"Grace Hopper\",\"email\":\"grace@watchesden.com\",\"password\":\"Cobol1959\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"GRACE@watchesden.com\",\"password\":\"Cobol1959\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode user = json.readTree(body).get("user");

        mvc.perform(get("/api/users/me").header("X-User-Id", user.get("id").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("grace@watchesden.com"));
    }

    @Test
    void duplicateRegistrationIsConflict() throws Exception {
        String register = "{\"fullName\":\"Dup\",\"email\":\"dup@watchesden.com\",\"password\":\"Passw0rd!\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    void invalidPayloadReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"nope\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@watchesden.com\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededAdminCanLogIn() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@watchesden.com\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void profileWithoutIdentityHeaderIsUnauthorized() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }
}
