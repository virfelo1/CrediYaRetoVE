package co.com.projectve.usecase.user;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UserUseCase {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private final UserRepository userRepository;
    BigDecimal minSalary = BigDecimal.ZERO;
    BigDecimal maxSalary = new BigDecimal("15000000");

    public Mono<User> execute(User user) {
        return validateUser(user)
                .flatMap(u -> userRepository.emailExist(u.getEmail()))
                .flatMap(exist -> {
                    if (exist) {
                        return Mono.error(new IllegalArgumentException("Correo ya registrado"));
                    }
                    return userRepository.saveUser(user);
                });
    }

    private Mono<User> validateUser(User user) {
        return Mono.just(user)
                .filter(u -> u.getFirstName() != null && !u.getFirstName().isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Los Nombres son obligatorios")))
                .filter(u -> u.getLastName() != null && !u.getLastName().isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Los apellidos son obligatorios")))
                .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El email es obligatorio")))
                .filter(u -> EMAIL_PATTERN.matcher(u.getEmail()).matches())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El email no es valido")))
                .filter(u -> u.getBaseSalary() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El salario es obligatorio")))
                .filter(u -> u.getBaseSalary().compareTo(minSalary) > 0 && u.getBaseSalary().compareTo(maxSalary) <= 0)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El salario base debe ser un valor numerico entre 0 y 15'000,000")));
    }
}

