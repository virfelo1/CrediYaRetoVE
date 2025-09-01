package co.com.projectve.api.config;

import co.com.projectve.model.user.User;
import co.com.projectve.usecase.user.UserUseCase;
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
    private final UserUseCase userUseCase;

    public CustomReactiveUserDetailsService(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        logger.debug("Buscando usuario por username: {}", username);
        
        return userUseCase.findByEmail(username)
                .map(this::createUserDetails)
                .doOnSuccess(userDetails -> 
                    logger.info("Usuario encontrado: {} con rol: {}", username, userDetails.getAuthorities()))
                .doOnError(error -> 
                    logger.warn("Usuario no encontrado: {}", username))
                .onErrorMap(error -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private UserDetails createUserDetails(User user) {
        logger.debug("Creando UserDetails para usuario: {} con rol: {}", user.getEmail(), user.getRol());
        
        // Mapear el rol numérico a un rol de Spring Security
        String role = mapRoleIdToRoleName(user.getRol());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Ya está encriptada
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private String mapRoleIdToRoleName(Integer roleId) {
        switch (roleId) {
            case 1:
                return "ADMIN";
            case 2:
                return "ASESOR";
            case 3:
                return "CLIENTE";
            default:
                logger.warn("Rol desconocido: {}, asignando CLIENTE por defecto", roleId);
                return "CLIENTE";
        }
    }
}
