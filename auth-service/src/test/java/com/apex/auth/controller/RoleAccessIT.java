package com.apex.auth.controller;

import com.apex.auth.dto.AuthenticationResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAccessIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private String tenantToken;
    private String landlordToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        tenantToken   = register("tenant@x.com",   Role.TENANT).getAccessToken();
        landlordToken = register("landlord@x.com", Role.LANDLORD).getAccessToken();
        adminToken    = register("admin@x.com",    Role.ADMIN).getAccessToken();
    }

    // ---------- /api/tenant/dashboard ----------

    @Test
    @DisplayName("GET /api/tenant/dashboard — 200 for TENANT, 403 for LANDLORD/ADMIN")
    void tenantDashboard_access() throws Exception {
        mockMvc.perform(get("/api/tenant/dashboard").header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tenant/dashboard").header("Authorization", "Bearer " + landlordToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tenant/dashboard").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/tenant/dashboard — 401 without token")
    void tenantDashboard_noToken() throws Exception {
        mockMvc.perform(get("/api/tenant/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- /api/landlord/dashboard ----------

    @Test
    @DisplayName("GET /api/landlord/dashboard — 200 for LANDLORD, 403 for TENANT/ADMIN")
    void landlordDashboard_access() throws Exception {
        mockMvc.perform(get("/api/landlord/dashboard").header("Authorization", "Bearer " + landlordToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/landlord/dashboard").header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/landlord/dashboard").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // ---------- /api/admin/users ----------

    @Test
    @DisplayName("GET /api/admin/users — 200 for ADMIN, 403 for TENANT/LANDLORD")
    void adminUsers_access() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + landlordToken))
                .andExpect(status().isForbidden());
    }

    // ---------- helper ----------

    private AuthenticationResponse register(String email, Role role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFullName("Test " + role.name());
        req.setRole(role);

        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(json, AuthenticationResponse.class);
    }
}
