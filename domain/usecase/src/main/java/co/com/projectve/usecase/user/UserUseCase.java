package co.com.projectve.usecase.user;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.usecase.user.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class UserUseCase {

    //private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private final UserRepository userRepository;

    public Mono<User> execute(User user) {
        //return validateUser(user)
        return Mono.just(user)
                .flatMap(u -> userRepository.emailExist(u.getEmail()))
                .flatMap(exist -> {
                    if (exist) {
                        return Mono.error(new BusinessException("Correo ya registrado"));
                    }
                    return userRepository.saveUser(user);
                });
    }
}

