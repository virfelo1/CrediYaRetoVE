package co.com.projectve.api.config;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomReactiveUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        logger.debug("Buscando usuario por username: {}", username);

        return userRepository.findByEmail(username)
                .flatMap(this::createUserDetails)
                .doOnSuccess(userDetails ->
                        logger.info("Usuario encontrado: {} con rol: {}", username, userDetails.getAuthorities()))
                .doOnError(error ->
                        logger.warn("Usuario no encontrado: {}", username))
                .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private Mono<UserDetails> createUserDetails(User user) {
        logger.debug("Creando UserDetails para usuario: {} con rol: {}", user.getEmail(), user.getRol());

        return userRepository.getRoleNameById(user.getRol())
                .map(roleName -> {
                    logger.debug("Rol de Spring Security asignado: ROLE_{}", roleName);
                    return org.springframework.security.core.userdetails.User.builder()
                            .username(user.getEmail())
                            .password(user.getPassword())
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName)))
                            .accountExpired(false)
                            .accountLocked(false)
                            .credentialsExpired(false)
                            .disabled(false)
                            .build();
                })
                .doOnError(error -> logger.error("Error al obtener el nombre del rol para el usuario {}: {}", user.getEmail(), error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }
}
