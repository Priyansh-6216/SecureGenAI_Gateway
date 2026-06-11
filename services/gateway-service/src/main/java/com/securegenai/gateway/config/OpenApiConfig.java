package com.securegenai.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for the Gateway Service.
 *
 * <p>Swagger UI is available at /swagger-ui.html when the service is running.
 * The {@code bearerAuth} security scheme enables the "Authorize" button in Swagger UI,
 * allowing developers to paste a JWT and test protected endpoints directly.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecureGenAI Gateway API")
                        .description("Enterprise AI Security Firewall — centralized gateway for all AI requests. "
                                + "Provides PII detection, prompt masking, risk scoring, and policy enforcement "
                                + "before prompts reach LLM providers. "
                                + "**Day 3:** JWT Authentication & Role-Based Authorization.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Priyansh Suthar")
                                .email("priyansh@securegenai.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://securegenai.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                // Global security requirement — all endpoints show the lock icon in Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                // Define the JWT Bearer Auth scheme
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token obtained from POST /api/v1/auth/login")
                        ));
    }
}
