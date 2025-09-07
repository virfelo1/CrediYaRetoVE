package co.com.projectve.r2dbc;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.r2dbc.entity.RolEntity;
import co.com.projectve.r2dbc.entity.UserEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import jakarta.annotation.PostConstruct;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Objects;


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
    private final RolRepository rolRepository; // Declare the new repository


    public MyReactiveRepositoryAdapter(
            MyReactiveRepository repository,
            ObjectMapper mapper,
            TransactionalOperator transactionalOperator,
            RolRepository rolRepository) { // Inject RolRepository in the constructor
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.transactionalOperator = transactionalOperator;
        this.rolRepository = rolRepository; // Assign the injected repository
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

    @Override
    public Mono<User> findByEmail(String email) {
        logger.debug("Buscando usuario por email: {}", email);
        return repository.findByEmail(email)
                .map(entity -> mapper.map(entity, User.class))
                // CAMBIO CLAVE: Comprobación explícita de nulidad en el doOnSuccess
                .doOnSuccess(user -> {
                    if (user != null) {
                        logger.info("Usuario encontrado con ID: {} y email: {}", user.getId(), user.getEmail());
                    } else {
                        logger.info("No se encontró usuario con el email: {}", email);
                    }
                })
                .doOnError(error -> logger.warn("Error al buscar usuario por email {}: {}", email, error.getMessage()));
    }

    @Override
    public Mono<String> getRoleNameById(Integer roleId) {
        return rolRepository.findById(roleId.shortValue())
                .map(RolEntity::getNameRol)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found")));
    }

    @Override
    public Flux<User> listUser() {
        return super.findAll();
    }


}
