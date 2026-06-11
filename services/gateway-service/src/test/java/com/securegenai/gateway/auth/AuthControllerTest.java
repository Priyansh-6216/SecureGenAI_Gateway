package com.securegenai.gateway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securegenai.gateway.dto.auth.LoginRequest;
import com.securegenai.gateway.dto.auth.RefreshRequest;
import com.securegenai.gateway.security.SecurityConfig;
import com.securegenai.gateway.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * Uses a full Spring context with MockMvc to test the complete security filter chain.
 *
 * Tests cover:
 * - Successful login for all three demo users
 * - Failed login with bad credentials
 * - Token refresh flow
 * - Access to protected endpoint with/without JWT
 * - Role-based access: EMPLOYEE cannot call /api/v1/validate (SECURITY_ANALYST only)
 * - /api/v1/auth/me returns current user info
 * - ADMIN can access /api/v1/admin/users, ANALYST cannot
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── Login Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Admin login returns 200 with access and refresh tokens")
    void adminLoginReturns200() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("Analyst login returns 200 with SECURITY_ANALYST role")
    void analystLoginReturns200() throws Exception {
        LoginRequest request = new LoginRequest("analyst", "analyst123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SECURITY_ANALYST"))
                .andExpect(jsonPath("$.username").value("analyst"));
    }

    @Test
    @DisplayName("Employee login returns 200 with EMPLOYEE role")
    void employeeLoginReturns200() throws Exception {
        LoginRequest request = new LoginRequest("employee", "emp123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.username").value("employee"));
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with unknown user returns 401")
    void loginWithUnknownUserReturns401() throws Exception {
        LoginRequest request = new LoginRequest("nobody", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with blank username returns 400")
    void loginWithBlankUsernameReturns400() throws Exception {
        LoginRequest request = new LoginRequest("", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─── Token Refresh ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid refresh token returns new token pair")
    void validRefreshTokenReturnsNewPair() throws Exception {
        // First login to get a refresh token
        String refreshToken = loginAndGetRefreshToken("admin", "admin123");

        RefreshRequest request = new RefreshRequest(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("Invalid refresh token returns 401")
    void invalidRefreshTokenReturns401() throws Exception {
        RefreshRequest request = new RefreshRequest("this.is.not.a.valid.token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ─── Protected Endpoint Access ────────────────────────────────────────────

    @Test
    @DisplayName("Accessing /api/v1/prompts without JWT returns 401")
    void accessingPromptsWithoutJwtReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hello world\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accessing /api/v1/validate without JWT returns 401")
    void accessingValidateWithoutJwtReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Analyst can access /api/v1/validate with valid JWT")
    void analystCanAccessValidateWithJwt() throws Exception {
        String accessToken = loginAndGetAccessToken("analyst", "analyst123");

        mockMvc.perform(post("/api/v1/validate")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hello world, safe prompt\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Employee cannot access /api/v1/validate (requires SECURITY_ANALYST)")
    void employeeCannotAccessValidate() throws Exception {
        String accessToken = loginAndGetAccessToken("employee", "emp123");

        mockMvc.perform(post("/api/v1/validate")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hello world\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Employee can access /api/v1/prompts")
    void employeeCanAccessPrompts() throws Exception {
        String accessToken = loginAndGetAccessToken("employee", "emp123");

        mockMvc.perform(post("/api/v1/prompts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"Hello, this is a safe message\",\"provider\":\"openai\"}"))
                .andExpect(status().isOk());
    }

    // ─── Role-Based Admin Access ──────────────────────────────────────────────

    @Test
    @DisplayName("Admin can access /api/v1/admin/users")
    void adminCanAccessAdminUsers() throws Exception {
        String accessToken = loginAndGetAccessToken("admin", "admin123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    @DisplayName("Analyst cannot access /api/v1/admin/users (ADMIN only)")
    void analystCannotAccessAdminUsers() throws Exception {
        String accessToken = loginAndGetAccessToken("analyst", "analyst123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Employee cannot access /api/v1/admin/stats (ADMIN only)")
    void employeeCannotAccessAdminStats() throws Exception {
        String accessToken = loginAndGetAccessToken("employee", "emp123");

        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    // ─── /me Endpoint ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("/me returns current user info for analyst")
    void meReturnsCurrentUserInfo() throws Exception {
        String accessToken = loginAndGetAccessToken("analyst", "analyst123");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("analyst"))
                .andExpect(jsonPath("$.role").value("SECURITY_ANALYST"));
    }

    @Test
    @DisplayName("/me returns 401 without token")
    void meReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Public Endpoints ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Health endpoint is publicly accessible (no JWT)")
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String loginAndGetAccessToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String loginAndGetRefreshToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("refreshToken").asText();
    }
}
