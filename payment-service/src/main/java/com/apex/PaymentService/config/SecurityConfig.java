package com.apex.PaymentService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ✅ Actuator — Docker HEALTHCHECK + NGINX probes
                .requestMatchers("/actuator/**").permitAll()

                // ✅ OpenAPI / Swagger — public during dev
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // ✅ Error endpoint — so exceptions don't leak 401
                .requestMatchers("/error").permitAll()

                // ⚠️ DEV MODE: permit everything else
                // The payment service is called by mpesa-service and other
                // internal services. Switch to .authenticated() once you
                // add a JWT filter (same pattern as auth-service).
                .anyRequest().permitAll()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
