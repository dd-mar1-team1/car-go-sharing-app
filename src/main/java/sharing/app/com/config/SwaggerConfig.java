package sharing.app.com.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String API = "Bookstore API";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "API for bookstore management";
    private static final String BEARERAUTH = "BearerAuth";
    private static final String BEARER = "bearer";
    private static final String JWT = "JWT";

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(API)
                        .version(VERSION)
                        .description(DESCRIPTION))
                .addSecurityItem(new SecurityRequirement().addList(BEARERAUTH))
                .schemaRequirement(BEARERAUTH, new SecurityScheme()
                        .name(BEARERAUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER)
                        .bearerFormat(JWT));
    }
}
