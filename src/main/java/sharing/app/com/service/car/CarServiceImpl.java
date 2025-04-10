package sharing.app.com.service.car;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.mapper.CarMapper;
import sharing.app.com.model.Car;
import sharing.app.com.repository.car.CarRepository;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarMapper carMapper;
    private final CarRepository carRepository;

    @Override
    public CarDto createCar(CreateCarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public Page<CarDto> getAll(Pageable pageable) {
        return carRepository.findAll(pageable).map(carMapper::toDto);
    }

    @Override
    public CarDto getCarById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + id)
        );
        return carMapper.toDto(car);
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }

    @Override
    public CarDto updateCar(Long id, CreateCarRequestDto requestDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + id)
        );
        carMapper.updateCarFromDto(requestDto, car);
        return carMapper.toDto(car);
    }
}
