package com.project.socialanalytics.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuthentication";
        return new OpenAPI()
                .info(new Info()
                        .title("SocialLens API - AI-Powered Social Media Analytics Framework")
                        .description("REST API supporting noise-resilient trend detection, duplicate filtering, sentiment analysis, emotion extraction, and network centrality.")
                        .version("1.0.0")
                        .contact(new Contact().name("B.Tech CSE Project Team").email("project@sociallens.internal"))
                        .license(new License().name("Academic Project License")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

