package sharing.app.com.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import sharing.app.com.model.Car;

public interface CarRepository extends JpaRepository<Car, Long> {
}
