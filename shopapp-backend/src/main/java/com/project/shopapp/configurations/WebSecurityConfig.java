package com.project.shopapp.configurations;

import com.project.shopapp.filters.JwtTokenFilter;
import com.project.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;

    // Thêm property để xác định chế độ test
    @Value("${app.test.mode:false}")
    private boolean testMode;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers(
                                    String.format("%s/users/register", apiPrefix),
                                    String.format("%s/users/login", apiPrefix),
                                    String.format("%s/orders/get-orders-by-keyword",
                                            apiPrefix))
                            .permitAll()
                            .requestMatchers(GET,
                                    String.format("%s/roles**", apiPrefix))
                            .permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/categories**", apiPrefix))
                            .permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/categories/**", apiPrefix))
                            .permitAll()

                            .requestMatchers(POST,
                                    String.format("%s/categories", apiPrefix),
                                    String.format("%s/categories/**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(PUT,
                                    String.format("%s/categories/**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(DELETE,
                                    String.format("%s/categories/**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(GET,
                                    String.format("%s/products**", apiPrefix))
                            .permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/products/**", apiPrefix))
                            .permitAll()

                            .requestMatchers(GET,
                                    String.format("%s/products/images/*",
                                            apiPrefix))
                            .permitAll()

                            .requestMatchers(POST,
                                    String.format("%s/products**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(PUT,
                                    String.format("%s/products/**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(DELETE,
                                    String.format("%s/products/**", apiPrefix))
                            .hasAnyRole(Role.ADMIN)

                            .requestMatchers(POST,
                                    String.format("%s/orders/**", apiPrefix))
                            .hasAnyRole(Role.USER)

                            .requestMatchers(PUT,
                                    String.format("%s/orders/**", apiPrefix))
                            .hasRole(Role.ADMIN)

                            .requestMatchers(DELETE,
                                    String.format("%s/orders/**", apiPrefix))
                            .hasRole(Role.ADMIN)

                            .requestMatchers(POST,
                                    String.format("%s/order_details/**", apiPrefix))
                            .hasAnyRole(Role.USER)

                            .requestMatchers(GET,
                                    String.format("%s/order_details/**", apiPrefix))
                            .permitAll()

                            .requestMatchers(PUT,
                                    String.format("%s/order_details/**", apiPrefix))
                            .hasRole(Role.ADMIN)

                            .requestMatchers(DELETE,
                                    String.format("%s/order_details/**", apiPrefix))
                            .hasRole(Role.ADMIN)

                            .requestMatchers(GET,
                                    String.format("%s/healthcheck/**", apiPrefix))
                            .permitAll()

                            .anyRequest().authenticated();

                })
                .csrf(AbstractHttpConfigurer::disable);
        if (!testMode) {
            http.securityMatcher(String.valueOf(EndpointRequest.toAnyEndpoint()));
        }
        return http.build();
    }
}
