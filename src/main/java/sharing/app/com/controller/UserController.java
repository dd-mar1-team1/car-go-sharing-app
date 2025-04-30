package sharing.app.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.app.com.dto.user.RoleUpdateRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.model.User;
import sharing.app.com.service.user.UserService;

@Tag(name = "Users management", description = "Manage user authentication and profile")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Update user role", description = "Only managers can update user roles")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/role")
    public UserResponseDto userUpdateRole(Authentication authentication,
                                          @RequestBody RoleUpdateRequestDto requestDto) {
        Long userId = getUserId(authentication);
        return userService.updateUserRole(userId, requestDto);
    }

    @Operation(summary = "Get current user profile",
            description = "Get the profile of the currently authenticated user")
    @GetMapping("/me")
    public UserResponseDto getCurrentUserProfile(Authentication authentication) {
        return userService.getCurrentUserProfile(authentication);
    }

    @Operation(summary = "Update current user profile",
            description = "Update the profile information of the currently authenticated user")
    @PutMapping("/me")
    public UserResponseDto userResponseDto(Authentication authentication, @Valid
                                           @RequestBody UserUpdateRequestDto requestDto) {
        return userService.updateUserProfile(authentication, requestDto);
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
