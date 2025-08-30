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
        return request.bodyToMono(UserDTO.class)
                .flatMap(dto -> {
                    // Validación del DTO usando Bean Validation
                    Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        throw new ConstraintViolationException(violations);
                    }

                    // Mapeo y ejecución del caso de uso
                    User model = userDTOMapper.toModel(dto);
                    return useCase.execute(model);
                })
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
