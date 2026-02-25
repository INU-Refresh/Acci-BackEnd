package refresh.acci.global.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import refresh.acci.domain.auth.application.handler.OAuthFailureHandler;
import refresh.acci.domain.auth.application.handler.OAuthSuccessHandler;
import refresh.acci.domain.auth.oauth.CustomOAuth2UserService;
import refresh.acci.global.security.jwt.JwtAccessDeniedHandler;
import refresh.acci.global.security.jwt.JwtAuthenticationEntryPoint;
import refresh.acci.global.security.jwt.JwtAuthorizationFilter;
import refresh.acci.global.security.oauth.CookieOAuthAuthorizationRequestRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthSuccessHandler oauthSuccessHandler;
    private final OAuthFailureHandler oauthFailureHandler;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CookieOAuthAuthorizationRequestRepository cookieOAuthAuthorizationRequestRepository;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebExpressionAuthorizationManager prometheusIpAuthorizationManager() {
        return new WebExpressionAuthorizationManager("hasIpAddress('172.16.0.0/12')");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://acci-ai.site",
                "https://www.acci-ai.site",
                "https://api.acci-ai.site",
                "https://local.acci-ai.site:3001"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        //모든 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(MONITORING_ENDPOINTS).access(prometheusIpAuthorizationManager())
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(cookieOAuthAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(rediriction -> rediriction.baseUri("/api/v1/auth/oauth2/callback/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oauthSuccessHandler)
                        .failureHandler(oauthFailureHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    private static final String[] PUBLIC_ENDPOINTS = {
            //Swagger
            "/swagger-ui/**",
            "/v3/api-docs/**",

            //로그인 및 인증
            "/oauth2/**",
            "/api/v1/auth/oauth2/callback/**",
            "/api/v1/auth/token",
            "/api/v1/auth/refresh",

            //사고 영상 분석
            "/api/v1/analyses/loading",

            //Actuator(로드밸런서용)
            "/actuator/health"
    };

    private static final String[] AUTHENTICATED_ENDPOINTS = {
            //로그인 및 인증
            "/api/v1/auth/logout",
            "/api/v1/users/**",

            //사고 영상 분석
            "/api/v1/analyses/**",

            //수리비 견적
            "/api/v1/repair-estimates/**"
    };

    private static final String[] MONITORING_ENDPOINTS = {
            "/actuator/prometheus",
            "/actuator/metrics"
    };
}
