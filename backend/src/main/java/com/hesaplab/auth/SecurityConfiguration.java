package com.hesaplab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    private final ObjectMapper mapper;
    public SecurityConfiguration(ObjectMapper mapper) { this.mapper = mapper; }

    private void json(HttpServletResponse response, int status, Object value) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getOutputStream(), value);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(Customizer.withDefaults())
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/demo/**").denyAll()
                        .requestMatchers("/api/recipients/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/transfers", "/api/transfers/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/accounts", "/api/accounts/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> json(response, 401,
                                Map.of("message", "Giriş yapmanız gerekiyor.")))
                        .accessDeniedHandler((request, response, exception) -> json(response, 403,
                                Map.of("message", "Bu isteğe izin verilmiyor."))))
                .formLogin(form -> form.loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> json(response, 200,
                                ((AppUserDetails) authentication.getPrincipal()).user()))
                        .failureHandler((request, response, exception) -> json(response, 401,
                                Map.of("message", "Kullanıcı adı veya parola hatalı."))))
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true).clearAuthentication(true)
                        .deleteCookies("HESAPLAB_SESSION")
                        .logoutSuccessHandler((request, response, authentication) -> json(response, 200,
                                Map.of("message", "Çıkış yapıldı."))))
                .build();
    }
}
