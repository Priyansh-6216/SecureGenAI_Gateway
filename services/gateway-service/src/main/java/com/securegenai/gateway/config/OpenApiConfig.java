package com.securegenai.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration for the Gateway Service.
 * Swagger UI is available at /swagger-ui.html when the service is running.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecureGenAI Gateway API")
                        .description("Enterprise AI Security Firewall — centralized gateway for all AI requests. "
                                + "Provides PII detection, prompt masking, risk scoring, and policy enforcement "
                                + "before prompts reach LLM providers.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Priyansh Suthar")
                                .email("priyansh@securegenai.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://securegenai.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ));
    }
}
