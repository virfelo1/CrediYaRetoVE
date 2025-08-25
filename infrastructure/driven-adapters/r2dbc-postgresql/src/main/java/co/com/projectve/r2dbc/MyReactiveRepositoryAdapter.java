package co.com.projectve.r2dbc;

import co.com.projectve.model.user.User;
import co.com.projectve.model.user.gateways.UserRepository;
import co.com.projectve.r2dbc.entity.UserEntity;
import co.com.projectve.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
    User/* change for domain model */,
        UserEntity/* change for adapter model */,
    String,
    MyReactiveRepository
>
implements UserRepository
{
    /*public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model

        super(repository, mapper, d -> mapper.map(d, User.class/* change for domain model));
    }*/

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
    }
    @Override
    public Mono<User> saveUser(User user) {
        return super.save(user);
    }

    @Override
    public Mono<Boolean> emailExist(String email) {
        return repository.existsByEmail(email);
    }
}
