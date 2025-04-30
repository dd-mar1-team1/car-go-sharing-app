package sharing.app.com.dto.user;

import lombok.Data;
import lombok.experimental.Accessors;
import sharing.app.com.model.User;

@Accessors(chain = true)
@Data
public class RoleUpdateRequestDto {
    private User.Role role;
}
