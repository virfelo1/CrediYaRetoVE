package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO para registrar nuevos usuarios en el sistema")
public record UserDTO (
        @Schema(description = "Nombres del usuario", example = "Juan Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Los Nombres son obligatorios")
        String firstName,

        @Schema(description = "Apellidos del usuario", example = "García López", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Los apellidos son obligatorios")
        String lastName,

        @Schema(description = "Fecha de nacimiento del usuario", example = "1990-05-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate dateOfBirth,

        @Schema(description = "Dirección de residencia del usuario", example = "Calle 123 #45-67, Bogotá", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String address,

        @Schema(description = "Número de teléfono del usuario", example = "+57 300 123 4567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String phoneNumber,

        @Schema(description = "Correo electrónico único del usuario", example = "juan.garcia@email.com", requiredMode = Schema.RequiredMode.REQUIRED, format = "email")
        @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", 
                 message = "El correo no es valido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @Schema(description = "Salario base del usuario en pesos colombianos", example = "2500000", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "15000000")
        @NotNull(message = "El salario es obligatorio")
        @DecimalMin(value = "0", inclusive = true, message = "El salario debe ser mayor que 0")
        @DecimalMax(value = "15000000", inclusive = true, message = "El salario no puede ser mayor que 15'000,000")
        BigDecimal baseSalary
            )
{}
