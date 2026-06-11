package com.securegenai.gateway.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the {@code POST /api/v1/auth/login} endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Username", example = "analyst")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password", example = "analyst123")
    private String password;
}
