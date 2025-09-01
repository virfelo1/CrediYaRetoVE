package co.com.projectve.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginDTO (
    @Schema(description = "Correo electrónico único del usuario", example = "juan.garcia@email.com", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
         message = "El usuario deber ser un correo")
    @NotBlank(message = "El usuario es obligatorio")
    String username,

    @NotBlank(message = "Se requiere contraseña")
    String password
){}
