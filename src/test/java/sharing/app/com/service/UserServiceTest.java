package sharing.app.com.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import sharing.app.com.dto.user.RoleUpdateRequestDto;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.exception.RegistrationException;
import sharing.app.com.mapper.UserMapper;
import sharing.app.com.model.User;
import sharing.app.com.repository.user.UserRepository;
import sharing.app.com.service.user.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @DisplayName("Should return UserResponseDto when registration request is valid")
    @Test
    void register_ValidRegistrationRequest_ReturnsUserResponseDto() throws RegistrationException {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRepeatPassword("NewSecure123");

        User user = new User()
                .setEmail(requestDto.getEmail())
                .setFirstName(requestDto.getFirstName())
                .setLastName(requestDto.getLastName())
                .setPassword(requestDto.getPassword())
                .setRole(User.Role.CUSTOMER);

        UserResponseDto responseDto = new UserResponseDto()
                .setId(1L)
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());

        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto actual = userService.register(requestDto);

        assertEquals(responseDto, actual);
    }

    @DisplayName("Should throw RegistrationException when email already exists")
    @Test
    void register_EmailAlreadyExists_ThrowsRegistrationException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto()
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRepeatPassword("NewSecure123");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        assertThrows(RegistrationException.class, () -> userService.register(requestDto));
    }

    @DisplayName("Should update user role and return updated UserResponseDto for valid user ID")
    @Test
    void updateUserRole_ValidUserIdAndRequestDto_ReturnsUpdatedUserResponseDto() {
        RoleUpdateRequestDto roleUpdateRequestDto = new RoleUpdateRequestDto();
        roleUpdateRequestDto.setRole(User.Role.MANAGER);

        User user = new User()
                .setId(1L)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.MANAGER);

        UserResponseDto responseDto = new UserResponseDto()
                .setId(1L)
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUserRole(1L, roleUpdateRequestDto);

        assertNotNull(result);
        assertEquals(User.Role.MANAGER, user.getRole());
    }

    @DisplayName("Should throw EntityNotFoundException when user ID does not exist")
    @Test
    void updateUserRole_NonExistingUserId_ThrowsEntityNotFoundException() {
        Long nonExistingUserId = 100L;
        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto();
        requestDto.setRole(User.Role.MANAGER);

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(nonExistingUserId, requestDto));

        assertEquals("Can't find user by id " + nonExistingUserId, exception.getMessage());
    }

    @DisplayName("Should return UserResponseDto for authenticated user")
    @Test
    void getCurrentUserProfile_ValidAuthentication_ReturnsUserResponseDto() {
        String email = "johnny.dough@example.com";
        User user = new User()
                .setId(1L)
                .setEmail(email)
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.MANAGER);

        UserResponseDto responseDto = new UserResponseDto()
                .setId(1L)
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponseDto actual = userService.getCurrentUserProfile(authentication);

        assertNotNull(actual);
        assertEquals(responseDto, actual);
    }

    @DisplayName("Should throw EntityNotFoundException when user email is not found")
    @Test
    void getCurrentUserProfile_EmailNotFound_ThrowsEntityNotFoundException() {
        String email = "johnny.dough@example.com";
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.getCurrentUserProfile(authentication));
    }

    @DisplayName("Should update user profile and return updated UserResponseDto for valid request")
    @Test
    void updateUserProfile_ValidRequestDto_UpdatesUserAndReturnsUserResponseDto() {
        User user = new User()
                .setId(1L)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.MANAGER);

        UserUpdateRequestDto requestDto = new UserUpdateRequestDto()
                .setEmail("tom.dough@example.com")
                .setFirstName("Tom")
                .setLastName("Dough")
                .setPassword("Qwerty123")
                .setRepeatPassword("Qwerty123");

        User updatedUser = new User()
                .setId(1L)
                .setEmail("tom.dough@example.com")
                .setFirstName("Tom")
                .setLastName("Dough")
                .setPassword("encodedPassword")
                .setRole(User.Role.MANAGER);

        UserResponseDto responseDto = new UserResponseDto()
                .setId(1L)
                .setEmail("tom.dough@example.com")
                .setFirstName("Tom")
                .setLastName("Dough");

        when(authentication.getName()).thenReturn("johnny.dough@example.com");
        when(userRepository.findByEmail("johnny.dough@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("tom.dough@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Qwerty123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(responseDto);

        UserResponseDto actual = userService.updateUserProfile(authentication, requestDto);

        assertNotNull(actual);
        assertEquals(responseDto, actual);
    }

    @DisplayName("Should throw EntityNotFoundException when authenticated user is not found")
    @Test
    void updateUserProfile_UserNotFound_ThrowsEntityNotFoundException() {
        String email = "johnny.dough@example.com";
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto()
                .setEmail("tom.dough@example.com")
                .setFirstName("Tom")
                .setLastName("Dough")
                .setPassword("Qwerty123")
                .setRepeatPassword("Qwerty123");

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserProfile(authentication, requestDto));
        assertTrue(exception.getMessage().contains("Can't find user by email"));
    }
}
