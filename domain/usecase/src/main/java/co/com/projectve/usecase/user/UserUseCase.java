package co.com.projectve.usecase.user;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.usecase.user.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserUseCase.class);

    public Mono<User> execute(User user) {
        logger.info("Iniciando caso de uso para usuario con email: {}", user.getEmail());
        logger.debug("Detalles del usuario: firstName={}, lastName={}, baseSalary={}", 
                   user.getFirstName(), user.getLastName(), user.getBaseSalary());
        
        return Mono.just(user)
                .doOnNext(u -> logger.debug("Usuario preparado para validación de email: {}", u.getEmail()))
                .flatMap(u -> {
                    logger.debug("Verificando existencia del email: {}", u.getEmail());
                    return userRepository.emailExist(u.getEmail());
                })
                .doOnNext(exists -> {
                    if (exists) {
                        logger.warn("Email ya registrado: {}", user.getEmail());
                    } else {
                        logger.info("Email disponible para registro: {}", user.getEmail());
                    }
                })
                .flatMap(exist -> {
                    if (exist) {
                        logger.error("Operación cancelada: Email {} ya está registrado en el sistema", user.getEmail());
                        return Mono.error(new BusinessException("Correo ya registrado"));
                    }
                    
                    // Asignar rol de cliente (Integer=3) antes de guardar
                    user.setRol((Integer) 3);
                    logger.info("Asignando id_rol de cliente (byte=3) al usuario: {}", user.getEmail());
                    
                    logger.info("Procediendo con el guardado del usuario: {}", user.getEmail());
                    logger.debug("Iniciando operación de persistencia para usuario: {} con rol: {}", user.getEmail(), user.getRol());
                    
                    return userRepository.saveUser(user);
                })
                .doOnNext(savedUser -> {
                    logger.info("Usuario guardado exitosamente en el caso de uso - ID: {}, Email: {}", 
                               savedUser.getId(), savedUser.getEmail());
                    logger.debug("Detalles del usuario guardado: firstName={}, lastName={}, baseSalary={}", 
                               savedUser.getFirstName(), savedUser.getLastName(), savedUser.getBaseSalary());
                })
                .doOnError(error -> {
                    if (error instanceof BusinessException) {
                        logger.warn("Error de negocio en el caso de uso: {}", error.getMessage());
                    } else {
                        logger.error("Error inesperado en el caso de uso para email {}: {}", 
                                   user.getEmail(), error.getMessage(), error);
                    }
                })
                .doOnSuccess(success -> logger.info("Caso de uso completado exitosamente para email: {}", user.getEmail()))
                .doOnError(error -> logger.error("Caso de uso falló para email {}: {}", user.getEmail(), error.getMessage()));
    }

    public Mono<User> findByEmail(String email) {
        logger.info("Iniciando búsqueda de usuario por email: {}", email);
        
        return userRepository.findByEmail(email)
                .doOnNext(user -> logger.info("Usuario encontrado con ID: {} y email: {}", user.getId(), user.getEmail()))
                .doOnError(error -> {
                    logger.error("Error al buscar usuario por email {}: {}", email, error.getMessage(), error);
                })
                .doOnSuccess(user -> logger.info("Búsqueda completada exitosamente para email: {}", email))
                .doOnError(error -> logger.error("Búsqueda falló para email {}: {}", email, error.getMessage()));
    }

    public Mono<String> getRoleNameById(Integer roleId) {
        return userRepository.getRoleNameById(roleId);
    }

}

