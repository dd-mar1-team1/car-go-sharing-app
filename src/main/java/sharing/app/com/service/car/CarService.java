package sharing.app.com.service.car;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;

public interface CarService {
    CarDto createCar(CreateCarRequestDto requestDto);

    Page<CarDto> getAll(Pageable pageable);

    CarDto getCarById(Long id);

    void deleteById(Long id);

    CarDto updateCar(Long id, CreateCarRequestDto requestDto);
}
