package sharing.app.com.repository.rental;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sharing.app.com.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findAllByUserIdAndActualReturnDateIsNull(Long userId, Pageable pageable);

    Page<Rental> findAllByUserIdAndActualReturnDateIsNotNull(Long userId, Pageable pageable);
}
