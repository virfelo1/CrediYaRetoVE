package co.com.projectve.r2dbc;


import co.com.projectve.r2dbc.entity.RolEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends R2dbcRepository<RolEntity, Short> {
}
