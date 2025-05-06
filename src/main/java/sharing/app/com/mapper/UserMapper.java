package sharing.app.com.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sharing.app.com.config.MapperConfig;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.model.User;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(source = "role", target = "role")
    UserResponseDto toUserResponse(User user);

    User toModel(UserRegistrationRequestDto requestDto);
}
