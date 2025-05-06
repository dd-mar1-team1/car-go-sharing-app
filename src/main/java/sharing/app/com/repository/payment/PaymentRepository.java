package sharing.app.com.repository.payment;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sharing.app.com.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByRentalUserId(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);
}
