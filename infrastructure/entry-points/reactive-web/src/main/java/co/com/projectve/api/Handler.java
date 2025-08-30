package co.com.projectve.api;

import co.com.projectve.api.dto.UserDTO;
import co.com.projectve.api.mapper.UserDTOMapper;
import co.com.projectve.model.user.User;
import co.com.projectve.usecase.user.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class Handler {
//private  final UseCase useCase;
//private  final UseCase2 useCase2;

    private final UserUseCase useCase;
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;
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

    public Mono<ServerResponse> registerUser(ServerRequest request) {
        logger.info("Iniciando proceso de registro de usuario");
        logger.debug("Request recibida: {}", request);
        
        return request.bodyToMono(UserDTO.class)
                .doOnSubscribe(subscription -> logger.debug("Iniciando suscripción para procesar DTO"))
                .doOnNext(dto -> {
                    logger.info("DTO recibido para usuario: {}", dto.email());
                    logger.debug("Detalles completos del DTO: firstName={}, lastName={}, email={}, baseSalary={}", 
                               dto.firstName(), dto.lastName(), dto.email(), dto.baseSalary());
                })
                .flatMap(dto -> {
                    logger.debug("Iniciando validación del DTO para email: {}", dto.email());
                    
                    // Validación del DTO usando Bean Validation
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

                    // Mapeo y ejecución del caso de uso
                    User model = userDTOMapper.toModel(dto);
                    logger.debug("DTO mapeado a modelo User con ID: {}", model.getId());
                    
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
}
