package sharing.app.com.service.user;

import org.springframework.security.core.Authentication;
import sharing.app.com.dto.user.RoleUpdateRequestDto;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto updateUserRole(Long userId, RoleUpdateRequestDto requestDto);

    UserResponseDto getCurrentUserProfile(Authentication authentication);

    UserResponseDto updateUserProfile(Authentication authentication,
                                      UserUpdateRequestDto requestDto);
}
