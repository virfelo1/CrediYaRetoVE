package co.com.projectve.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

//@Shema(description = "DTO para registrar nuevos usuarios")
public record UserDTO (
        @NotBlank(message = "Los Nombres son obligatorios")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        String lastName,

        LocalDate dateOfBirth,

        String address,

        String phoneNumber,

        @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$", 
                 message = "El correo no es valido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotNull(message = "El salario es obligatorio")
        @DecimalMin(value = "0", inclusive = true, message = "El salario debe ser mayor que 0")
        @DecimalMax(value = "15000000", inclusive = true, message = "El salario no puede ser mayor que 15'000,000")
        BigDecimal baseSalary
            )
{}
