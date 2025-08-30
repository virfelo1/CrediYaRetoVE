package co.com.projectve.api;

import co.com.projectve.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Autenticacion", description = "Endpoints para guardar nuevos usuarios en base de datos")
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "registerUser",
                    operation = @Operation(
                            summary = "Guarda un usuario nuevo en base de datos",
                            description = "Verifica que el correo no este registrado para guardarlo en la BD.",
                            operationId = "registerUser",
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = UserDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario guardado con exito",
                                            content = @Content(schema = @Schema(implementation = UserDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación en los datos de la solicitud",
                                            content = @Content(schema = @Schema(implementation = String.class, example = "{\"error\":\"El tipo de documento es obligatorio.\"}"))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/usuarios"), handler::registerUser);
                //.andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase)
                //.and(route(GET("/api/otherusercase/path"), handler::listenGETOtherUseCase));
    }
}
