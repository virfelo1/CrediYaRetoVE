package co.com.projectve.api;

import co.com.projectve.api.config.LoginAttemptService;
import co.com.projectve.api.dto.LoginDTO;
import co.com.projectve.api.dto.UserDTO;
import co.com.projectve.api.mapper.UserDTOMapper;
import co.com.projectve.model.user.User;
import co.com.projectve.usecase.user.UserUseCase;
import co.com.projectve.usecase.user.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.com.projectve.api.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase useCase;
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    @Operation(summary = "Registro de usuarios",
            description = "Guarda Solo usuarios nuevos, se requiere un correo electrónico único; si intentas usar uno que ya está registrado, no se completará el proceso.",
            tags = {"Autenticacion"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario guardado con exito",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"El nombre es obligatorio.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })

    public Mono<ServerResponse> registerUser(ServerRequest serverRequest) {
        logger.info("Iniciando proceso de registro de usuario");
        logger.debug("Request recibida: {}", serverRequest);

        return serverRequest.bodyToMono(UserDTO.class)
                .doOnSubscribe(subscription -> logger.debug("Iniciando suscripción para procesar DTO"))
                .doOnNext(dto -> {
                    logger.info("DTO recibido para usuario: {}", dto.email());
                    logger.debug("Detalles completos del DTO: firstName={}, lastName={}, email={}, baseSalary={}",
                            dto.firstName(), dto.lastName(), dto.email(), dto.baseSalary());
                })
                .flatMap(dto -> {
                    logger.debug("Iniciando validación del DTO para email: {}", dto.email());

                    Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        logger.warn("Validación fallida para email {}: {} violaciones encontradas",
                                dto.email(), violations.size());
                        violations.forEach(violation ->
                                logger.warn("Violación: {} - {}", violation.getPropertyPath(), violation.getMessage()));
                        throw new ConstraintViolationException(violations);
                    }

                    logger.info("Validación exitosa para email: {}", dto.email());
                    logger.debug("DTO validado correctamente, procediendo con mapeo");

                    User model = userDTOMapper.toModel(dto);
                    logger.debug("DTO mapeado a modelo User con ID: {}", model.getId());

                    String encryptedPassword = passwordEncoder.encode(model.getPassword());
                    model.setPassword(encryptedPassword);
                    logger.info("Contraseña encriptada para usuario: {}", model.getEmail());

                    return useCase.execute(model);
                })
                .doOnNext(savedUser ->
                        logger.info("Usuario guardado exitosamente con ID: {} y email: {}",
                                savedUser.getId(), savedUser.getEmail()))
                .doOnError(error -> {
                    if (error instanceof ConstraintViolationException) {
                        logger.error("Error de validación en el registro: {}", error.getMessage());
                    } else {
                        logger.error("Error inesperado durante el registro: {}", error.getMessage(), error);
                    }
                })
                .flatMap(response -> {
                    logger.debug("Preparando respuesta exitosa para usuario con ID: {}", response.getId());
                    return ServerResponse.ok().bodyValue(response);
                })
                .doOnSuccess(success -> logger.info("Respuesta enviada exitosamente"))
                .doOnError(error -> logger.error("Error al enviar respuesta: {}", error.getMessage()));
    }

    @Operation(summary = "Login de usuarios",
            description = "Autentica usuarios registrados usando su correo electrónico y contraseña. Si el usuario no existe, retorna un error.",
            tags = {"Autenticacion"},
            requestBody = @RequestBody(
                    content = @Content(schema = @Schema(implementation = LoginDTO.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login exitoso, token JWT generado",
                            content = @Content(schema = @Schema(implementation = String.class, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))),
                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"Datos de login inválidos\"}"))),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                            content = @Content(schema = @Schema(implementation = Map.class, example = "{\"error\":\"Usuario no se encuentra registrado\"}"))),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            })

    public Mono<ServerResponse> loginUser(ServerRequest serverRequest) {
        logger.info("Iniciando proceso de login de usuario");

        return serverRequest.bodyToMono(LoginDTO.class)
                .doOnNext(dto -> {
                    logger.info("LoginDTO recibido para usuario: {}", dto.username());
                    logger.debug("Detalles del login: username={}, password={}", dto.username(), "***");
                })
                .flatMap(dto -> {
                    logger.debug("Iniciando validación del LoginDTO para username: {}", dto.username());

                    Set<ConstraintViolation<LoginDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        logger.warn("Validación fallida para username {}: {} violaciones encontradas",
                                dto.username(), violations.size());
                        violations.forEach(violation ->
                                logger.warn("Violación: {} - {}", violation.getPropertyPath(), violation.getMessage()));
                        throw new ConstraintViolationException(violations);
                    }

                    logger.info("Validación exitosa para username: {}", dto.username());

                    if (loginAttemptService.isBlocked(dto.username())) {
                        logger.warn("Usuario bloqueado por demasiados intentos de login fallidos: {}", dto.username());
                        return Mono.error(new BusinessException("Demasiados intentos de login. Intenta de nuevo en 15 minutos"));
                    }

                    return useCase.findByEmail(dto.username())
                            .switchIfEmpty(Mono.error(new BusinessException("Usuario no se encuentra registrado")))
                            .doOnNext(user -> {
                                logger.info("Usuario encontrado para login con ID: {} y email: {}",
                                        user.getId(), user.getEmail());
                                logger.info("Usuario con id_rol: {}", user.getRol());
                            })
                            .flatMap(user -> {
                                String storedPassword = user.getPassword();
                                String providedPassword = dto.password();

                                logger.debug("Validando contraseña para usuario: {}", user.getEmail());

                                if (passwordEncoder.matches(providedPassword, storedPassword)) {
                                    logger.info("Usuario logueado con éxito: {}", user.getEmail());
                                    loginAttemptService.loginSucceeded(dto.username());

                                    // Nuevo flujo para obtener el nombre del rol y generar el token
                                    return useCase.getRoleNameById(user.getRol())
                                            .flatMap(roleName -> {
                                                List<String> userRoles = List.of(roleName);
                                                String token = jwtUtil.create(user.getEmail(), userRoles);

                                                logger.info("Token JWT generado exitosamente para usuario: {} con roles: {}", user.getEmail(), userRoles);

                                                return ServerResponse.ok()
                                                        .contentType(MediaType.TEXT_PLAIN)
                                                        .bodyValue(token);
                                            });
                                } else {
                                    logger.warn("Contraseña incorrecta para usuario: {}", user.getEmail());
                                    loginAttemptService.loginFailed(dto.username());
                                    return Mono.error(new RuntimeException("Contraseña incorrecta"));
                                }
                            });
                })
                .doOnSuccess(success -> logger.info("Respuesta de login enviada exitosamente"))
                .onErrorResume(UnsupportedMediaTypeStatusException.class, error -> {
                    logger.warn("Error de Content-Type en login: {}", error.getMessage());
                    return ServerResponse.status(415)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "Content-Type debe ser application/json"));
                })
                .onErrorResume(ConstraintViolationException.class, error -> {
                    logger.warn("Error de validación en login: {}", error.getMessage());

                    ConstraintViolationException cve = (ConstraintViolationException) error;
                    String errorMessage = cve.getConstraintViolations().stream()
                            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                            .findFirst()
                            .orElse("Datos de login inválidos");

                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", errorMessage));
                })
                .onErrorResume(BusinessException.class, error -> {
                    logger.warn("Error de negocio en login: {}", error.getMessage());
                    if ("Demasiados intentos de login. Intenta de nuevo en 15 minutos".equals(error.getMessage())) {
                        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", error.getMessage()));
                    } else {
                        return ServerResponse.status(404)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", error.getMessage()));
                    }
                })
                .onErrorResume(RuntimeException.class, error -> {
                    if ("Contraseña incorrecta".equals(error.getMessage())) {
                        logger.warn("Error de contraseña incorrecta en login: {}", error.getMessage());
                        return ServerResponse.status(401)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", "Contraseña incorrecta"));
                    } else {
                        logger.error("Error inesperado en login: {}", error.getMessage());
                        return ServerResponse.status(500)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error: ", "Error interno del servidor"));
                    }
                });
    }
}
