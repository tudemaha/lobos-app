package id.my.tudemaha.lobos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final Set<String> PUBLIC_OPERATIONS = Set.of("registerUser", "login");

    @Bean
    public OpenAPI customOpenAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Lobos API Specification")
                        .version("v1.0.0")
                        .description("REST API documentation for Lobos")
                        .license(new License().name("Apache 2.0")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }

    @Bean
    public OperationCustomizer publicEndpointsOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (PUBLIC_OPERATIONS.contains(handlerMethod.getMethod().getName())) {
                operation.setSecurity(Collections.emptyList());
            }
            return operation;
        };
    }
}
