package sharing.app.com.repository.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sharing.app.com.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
