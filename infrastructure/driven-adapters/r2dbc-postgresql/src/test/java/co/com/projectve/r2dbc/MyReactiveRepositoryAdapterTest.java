package co.com.projectve.r2dbc;

import co.com.projectve.model.user.User;
import co.com.projectve.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @InjectMocks
    MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    MyReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private UserEntity buildUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId("1");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userEntity.setAddress("Test Address 123");
        userEntity.setPhoneNumber("1234567890");
        userEntity.setEmail("test@example.com");
        userEntity.setBaseSalary(new BigDecimal("5000000.00"));
        return userEntity;
    }

    private User buildUser() {
        return User.builder()
                .id("1")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Test Address 123")
                .phoneNumber("1234567890")
                .email("test@example.com")
                .baseSalary(new BigDecimal("5000000.00"))
                .build();
    }

    @Test
    void mustFindValueById() {
        UserEntity userEntity = buildUserEntity();
        User user = buildUser();

        when(repository.findById("1")).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.findById("1");

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        UserEntity userEntity = buildUserEntity();
        User user = buildUser();

        when(repository.findAll()).thenReturn(Flux.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Flux<User> result = repositoryAdapter.findAll();

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getFirstName().equals("Test"))
                .verifyComplete();
    }

    @Test
    void mustFindByExample() {
        UserEntity userEntity = buildUserEntity();
        User user = buildUser();

        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Flux<User> result = repositoryAdapter.findByExample(user);

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getEmail().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        UserEntity userEntity = buildUserEntity();
        User user = buildUser();

        when(mapper.map(any(User.class), any())).thenReturn(userEntity);
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.save(user);

        StepVerifier.create(result)
                .expectNextMatches(value -> value.getFirstName().equals("Test"))
                .verifyComplete();
    }
}