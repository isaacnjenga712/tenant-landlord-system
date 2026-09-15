package com.apex.auth.controller;

import com.apex.auth.dto.LoginRequest;
import com.apex.auth.dto.RegisterRequest;
import com.apex.auth.entity.Role;
import com.apex.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    // ---------- REGISTER ----------

    @Test
    @DisplayName("POST /api/auth/register — 201 with tokens for TENANT")
    void register_tenant_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("tenant@example.com");
        req.setPassword("password123");
        req.setFullName("Test Tenant");
        req.setRole(Role.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("TENANT"))
                .andExpect(jsonPath("$.email").value("tenant@example.com"));

        assertThat(userRepository.findByEmail("tenant@example.com")).isPresent();
    }

    @Test
    @DisplayName("POST /api/auth/register — 400 when email is invalid")
    void register_invalidEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("password123");
        req.setFullName("Bad Email");
        req.setRole(Role.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register — 400 when email already exists")
    void register_duplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@example.com");
        req.setPassword("password123");
        req.setFullName("Dup");
        req.setRole(Role.TENANT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------- LOGIN ----------

    @Test
    @DisplayName("POST /api/auth/login — 200 with valid credentials")
    void login_success() throws Exception {
        registerUser("login@example.com", "password123", Role.TENANT);

        LoginRequest req = new LoginRequest();
        req.setEmail("login@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    @DisplayName("POST /api/auth/login — 401 with wrong password")
    void login_wrongPassword() throws Exception {
        registerUser("login2@example.com", "password123", Role.TENANT);

        LoginRequest req = new LoginRequest();
        req.setEmail("login2@example.com");
        req.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ---------- REFRESH ----------

    @Test
    @DisplayName("POST /api/auth/refresh — 200 with new access + refresh tokens")
    void refresh_success() throws Exception {
        String refreshToken = registerUser("refresh@example.com", "password123", Role.TENANT)
                .getRefreshToken();

        String body = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/refresh — 400 with invalid refresh token")
    void refresh_invalidToken() throws Exception {
        String body = "{\"refreshToken\":\"garbage\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/refresh — old refresh token is revoked after rotation")
    void refresh_oldTokenRevokedAfterRotation() throws Exception {
        String oldRefresh = registerUser("rotate@example.com", "password123", Role.TENANT)
                .getRefreshToken();

        // First refresh — succeeds
        String body = "{\"refreshToken\":\"" + oldRefresh + "\"}";
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Second refresh with the SAME old token — should fail (revoked)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- helpers ----------

    private com.apex.auth.dto.AuthenticationResponse registerUser(String email, String password, Role role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword(password);
        req.setFullName("Test " + role.name());
        req.setRole(role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                com.apex.auth.dto.AuthenticationResponse.class);
    }
}
