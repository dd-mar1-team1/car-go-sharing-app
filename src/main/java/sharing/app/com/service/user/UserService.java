package sharing.app.com.service.user;

import org.springframework.security.core.Authentication;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.exception.RegistrationException;
import sharing.app.com.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto updateUserRole(Long userId, User.Role newRole);

    UserResponseDto getCurrentUserProfile(Authentication authentication);

    UserResponseDto updateUserProfile(Authentication authentication,
                                      UserUpdateRequestDto requestDto);
}
