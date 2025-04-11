package sharing.app.com.dto.user;

import lombok.Data;
import sharing.app.com.model.User;

@Data
public class RoleUpdateRequestDto {
    private User.Role role;
}
