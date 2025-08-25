package co.com.projectve.r2dbc;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.r2dbc.entity.UserEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        MyReactiveRepository
        >
        implements UserRepository {

    private final TransactionalOperator transactionalOperator;

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<User> saveUser(User user) {
        return super.save(user)
                .as(transactionalOperator::transactional); // Aquí garantizas la atomicidad
    }

    @Override
    public Mono<Boolean> emailExist(String email) {
        return repository.existsByEmail(email);
    }
}
