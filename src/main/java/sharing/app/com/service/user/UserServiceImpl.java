package sharing.app.com.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sharing.app.com.dto.user.RoleUpdateRequestDto;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.exception.RegistrationException;
import sharing.app.com.mapper.UserMapper;
import sharing.app.com.model.User;
import sharing.app.com.repository.user.UserRepository;
import sharing.app.com.security.JwtUtil;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Can't register user, email already exists");
        }
        User user = userMapper.toModel(requestDto);
        user.setRole(User.Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponseDto updateUserRole(Long userId, RoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by id " + userId));
        user.setRole(requestDto.getRole());
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponseDto getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: "
                        + username));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponseDto updateUserProfile(Authentication authentication,
                                             UserUpdateRequestDto requestDto) {
        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Can't find user by email "
                        + username));

        if (requestDto.getEmail() != null && !requestDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(requestDto.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(requestDto.getEmail());
        }

        if (requestDto.getFirstName() != null) {
            user.setFirstName(requestDto.getFirstName());
        }

        if (requestDto.getLastName() != null) {
            user.setLastName(requestDto.getLastName());
        }

        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    private Long getUserIdFromToken(String token) {
        String username = jwtUtil.getUsername(token);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: "
                        + username));
        return user.getId();
    }
}
