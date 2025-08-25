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

    public Mono<User> execute(User user){
        return validate(user)
                .flatMap(u -> userRepository.emailExist(u.getEmail())
                        .flatMap(exist ->{
                            if (exist) return Mono.error(new IllegalArgumentException("Correo ya registrado"));
                            return userRepository.saveUser(u);
                                }));
    }

    private Mono<User> validate(User user) {
        if (user.getFirstName() == null || user.getFirstName().isBlank()){
            return Mono.error(new IllegalArgumentException("los Nombres son obligatorios"));
        }

        if (user.getLastName() == null || user.getLastName().isBlank()){
            return Mono.error(new IllegalArgumentException("Los apellidos son obligatorios"));
        }

        if (user.getEmail() == null || user.getEmail().isBlank()){
            return Mono.error(new IllegalArgumentException("El email es obligatorio"));
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()){
            return Mono.error(new IllegalArgumentException("Email no valido"));
        }

        if (user.getBaseSalary() == null || user.getEmail().isBlank()){
            return Mono.error(new IllegalArgumentException("El salario es obligatorio"));
        }

        if (user.getBaseSalary().compareTo(minSalary) <= 0 || user.getBaseSalary().compareTo(maxSalary) > 0) {
            return Mono.error(new IllegalArgumentException("El salario base debe ser un valor numérico entre 0 y 15'000,000"));
        }

        return Mono.just(user);
    }

}

