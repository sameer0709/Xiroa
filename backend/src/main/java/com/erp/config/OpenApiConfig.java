package com.erp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI erpOpenAPI() {
                final String schemeName = "bearerAuth";
                return new OpenAPI()
                                .info(new Info()
                                                .title("Xiroa API")
                                                .description(
                                                                "Xiroa — Business ERP for Small Businesses. Inventory, GST billing, CRM, employee management, payroll, AI insights and sales forecasting.")
                                                .version("1.0.0")
                                                .contact(new Contact().name("Xiroa").email("hello@xiroa.in")))
                                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                                .components(new Components().addSecuritySchemes(schemeName,
                                                new SecurityScheme()
                                                                .name(schemeName)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }
}
