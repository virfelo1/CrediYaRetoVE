package co.com.projectve.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {
//private  final UseCase useCase;
//private  final UseCase2 useCase2;

    private final RegistrarUsuarioUseCase useCase;

    public Mono<ServerResponse> registrarUsuario(ServerRequest request) {
        return request.bodyToMono(User.class)
                .flatMap(useCase::execute)
                .flatMap(user -> ServerResponse.ok().bodyValue(user))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }
}
