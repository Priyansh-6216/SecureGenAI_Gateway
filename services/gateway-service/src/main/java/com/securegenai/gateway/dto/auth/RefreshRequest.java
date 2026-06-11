package com.securegenai.gateway.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the {@code POST /api/v1/auth/refresh} endpoint.
 * Clients provide the refresh token to obtain a new access token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request")
public class RefreshRequest {

    @NotBlank(message = "Refresh token cannot be blank")
    @Schema(description = "The refresh token issued at login", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
