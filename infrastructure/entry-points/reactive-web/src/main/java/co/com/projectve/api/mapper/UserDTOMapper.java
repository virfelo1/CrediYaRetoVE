package co.com.projectve.api.mapper;

import co.com.projectve.api.dto.UserDTO;
import co.com.projectve.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {
    @Mapping(target = "id", ignore = true)
    User toModel (UserDTO userDTO);
}
