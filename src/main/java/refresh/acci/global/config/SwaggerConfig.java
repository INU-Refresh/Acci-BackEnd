package refresh.acci.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // API 기본 정보
        Info info = new Info()
                .title("Acci")
                .description("Acci - AI 기반 교통사고 과실비율 분석 및 사고 대응 도움 서비스")
                .version("1.0.0");

        // 서버 정보
        Server server = new Server()
                .url("https://api.acci-ai.site")
                .description("배포 서버");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버");

        // 보안 스키마 (JWT Bearer Token)
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 스키마 (Cookie)
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("accessToken");

        // 보안 요구 사항 (전역 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth")
                .addList("cookieAuth");

        // OpenAPI 객체 구성
        return new OpenAPI()
                .info(info)
                .servers(List.of(server, localServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                        .addSecuritySchemes("cookieAuth", cookieAuth)
                )
                .addSecurityItem(securityRequirement);
    }
}