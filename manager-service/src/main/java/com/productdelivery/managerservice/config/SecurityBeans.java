package com.productdelivery.managerservice.config;

import jakarta.annotation.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
public class SecurityBeans {

    /**
     * The SecurityFilterChain for processing metrics
     * */
    @Bean
    @Priority(0)
    public SecurityFilterChain metricsSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    /**
     * The SecurityFilterChain for processing all non-metrics request
     * */
    @Bean
    @Priority(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
                        .anyRequest().hasRole("MANAGER"))
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService() {
        OidcUserService oidcUserService = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);
            List<GrantedAuthority> authorities =
                    Stream.concat(oidcUser.getAuthorities().stream(),
                            Optional.ofNullable(oidcUser.getClaimAsStringList("groups"))
                                    .orElseGet(List::of)
                                    .stream()
                                    .filter(role -> role.startsWith("ROLE_"))
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast))
                            .toList();

            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
        };
    }
}
