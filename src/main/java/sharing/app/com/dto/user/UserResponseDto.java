package sharing.app.com.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;
import sharing.app.com.model.User;

@Accessors(chain = true)
@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
}
