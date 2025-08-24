package co.com.projectve.model.user.gateways;

import co.com.projectve.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<Boolean> emailExist(String email);
}
