package co.com.projectve.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "DEALER")
                        .pathMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "DEALER")
                        .pathMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                        .pathMatchers("/api/v1/solicitud").hasRole("ADMIN")
                        .anyExchange().authenticated()
                        //.permitAll()

                )
                .httpBasic(httpBasic -> {})
                .build();
    }

    /*@Bean
    public ReactiveUserDetailsService memoryUsers() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails dealer = User.builder()
                .username("dealer")
                .password(passwordEncoder().encode("dealer123"))
                .roles("DEALER")
                .build();

        return new MapReactiveUserDetailsService(admin, dealer);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}