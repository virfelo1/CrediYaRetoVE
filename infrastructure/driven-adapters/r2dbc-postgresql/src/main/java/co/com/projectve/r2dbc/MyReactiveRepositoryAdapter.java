package co.com.projectve.r2dbc;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.r2dbc.entity.UserEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;




@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Integer,
        MyReactiveRepository
        >
        implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);
    private final TransactionalOperator transactionalOperator;


    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<User> saveUser(User user) {
        logger.info("Iniciando operación de guardado para usuario con email: {}", user.getEmail());
        return super.save(user)
                .doOnSubscribe(subscription -> logger.trace("Suscripción iniciada para guardar usuario"))
                .doOnSuccess(savedUser -> logger.info("Usuario guardado exitosamente con ID: {}", savedUser.getId()))
                .doOnError(error -> logger.error("Error al guardar usuario: {}", error.getMessage(), error))
                .as(transactionalOperator::transactional); // atomicidad
    }

    @PostConstruct
    public void testLog() {
        logger.info("Log4j2 está funcionando correctamente en consola");
    }

    @Override
    public Mono<Boolean> emailExist(String email) {
        logger.debug("Verificando existencia de email: {}", email);
        return repository.existsByEmail(email)
                .doOnSuccess(exists -> logger.info("Resultado de existencia para {}: {}", email, exists))
                .doOnError(error -> logger.warn("Error al verificar existencia de email {}: {}", email, error.getMessage()));
    }
}
