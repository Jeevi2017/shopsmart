package com.shopsmart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.shopsmart.service.UserService;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Lazy
    @Autowired
    UserService userService;

    @Lazy
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = SecurityConstants.ROLE_SUPER_ADMIN + " > " + SecurityConstants.ROLE_ADMIN + "\n" +
                               SecurityConstants.ROLE_ADMIN + " > " + SecurityConstants.ROLE_CUSTOMER;
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // NEW: Temporarily allow POST requests to the order endpoint for testing
                        .requestMatchers(HttpMethod.POST, "/api/orders/from-cart/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh-token",
                                "/api/auth/register-admin", "/api/auth/verify-2fa")
                        .permitAll().requestMatchers("/api/customers/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers("/api/admins/**").hasAuthority(SecurityConstants.ROLE_ADMIN)

                        // MODIFIED: Granular control for /api/discounts/**
                        // Allow GET requests for both ADMIN and CUSTOMER
                        .requestMatchers(HttpMethod.GET, "/api/discounts/**")
                            .hasAnyAuthority(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_CUSTOMER)
                        // Allow POST requests for apply-coupon and remove-coupon for CUSTOMER
                        .requestMatchers(HttpMethod.POST, "/api/discounts/apply-coupon/**", "/api/discounts/remove-coupon/**")
                            .hasAnyAuthority(SecurityConstants.ROLE_CUSTOMER)
                        // Keep other methods (POST, PUT, DELETE) for ADMIN only for the general /api/discounts/**
                        // This line should come AFTER the more specific POST rules.
                        .requestMatchers("/api/discounts/**").hasAuthority(SecurityConstants.ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, "/api/admin/users/**").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/{id}").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/{id}").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/{userId}/role").hasAuthority(SecurityConstants.ROLE_SUPER_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/carts/id/{cartId}").hasAuthority(SecurityConstants.ROLE_ADMIN)
                        .requestMatchers("/api/carts/**").hasAnyAuthority(SecurityConstants.ROLE_CUSTOMER, SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_SUPER_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/customers/{customerId}", "/api/customers/email/{email}").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider()).addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
