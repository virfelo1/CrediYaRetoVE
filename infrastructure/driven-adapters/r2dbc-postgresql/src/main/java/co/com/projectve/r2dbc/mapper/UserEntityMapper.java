package co.com.projectve.r2dbc.mapper;

import co.com.projectve.model.user.User;
import co.com.projectve.r2dbc.dto.UserListDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    /**
     * Convierte un objeto de dominio User a un DTO de lista de usuario.
     * @param user El objeto de dominio a mapear.
     * @return El DTO de lista de usuario.
     */
    UserListDTO toDto(User user);
}