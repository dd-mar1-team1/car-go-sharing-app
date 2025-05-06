package sharing.app.com.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.mapper.CarMapper;
import sharing.app.com.model.Car;
import sharing.app.com.repository.car.CarRepository;
import sharing.app.com.service.car.CarServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @DisplayName("Create a car with valid request should return CarDto")
    @Test
    void createCar_ValidCreateCarRequestDto_ReturnsCarDto() {
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        Car car = new Car()
                .setModel(requestDto.getModel())
                .setBrand(requestDto.getBrand())
                .setType(requestDto.getType())
                .setInventory(requestDto.getInventory())
                .setDailyFee(requestDto.getDailyFee());

        CarDto carDto = new CarDto()
                .setId(1L)
                .setModel(car.getModel())
                .setBrand(car.getBrand())
                .setType(car.getType())
                .setInventory(car.getInventory())
                .setDailyFee(car.getDailyFee());

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto savedCarDto = carService.createCar(requestDto);

        assertThat(savedCarDto).isEqualTo(carDto);
        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @DisplayName("Get all cars with valid pageable should return car list")
    @Test
    void getAll_ValidPageable_ReturnsAllCars() {
        Car car = new Car()
                .setId(1L)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        CarDto carDto = new CarDto()
                .setId(car.getId())
                .setModel(car.getModel())
                .setBrand(car.getBrand())
                .setType(car.getType())
                .setInventory(car.getInventory())
                .setDailyFee(car.getDailyFee());

        Pageable pageable = PageRequest.of(0, 10);
        List<Car> cars = List.of(car);
        Page<Car> carPage = new PageImpl<>(cars, pageable, cars.size());

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        Page<CarDto> carDtos = carService.getAll(pageable);

        assertThat(carDtos).hasSize(1);
        assertThat(carDtos.getContent()).containsExactly(carDto);

        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @DisplayName("Get car by valid ID should return correct car")
    @Test
    void getCarById_WithValidCarId_ShouldReturnValidCar() {
        Car car = new Car()
                .setId(1L)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        CarDto expected = new CarDto()
                .setId(car.getId())
                .setModel("Model S")
                .setBrand("Tesla");

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expected);

        CarDto actual = carService.getCarById(1L);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @DisplayName("Get car by non-existing ID should throw exception")
    @Test
    void getCarById_WithNonExistingUserId_ShouldThrowException() {
        Long carId = 100L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(
                EntityNotFoundException.class, () -> carService.getCarById(carId)
        );
        String expected = "Can't find car by id: " + carId;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @DisplayName("Delete car by valid ID should delete car")
    @Test
    void deleteById_ValidDeleteCar_DeleteCar() {
        Long carId = 1L;

        doNothing().when(carRepository).deleteById(carId);
        carService.deleteById(carId);

        verify(carRepository).deleteById(carId);
        verifyNoMoreInteractions(carRepository);
    }

    @DisplayName("Update car with valid data should update car")
    @Test
    void updateCar_ValidUpdateCar_UpdateCar() {
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        Car car = new Car()
                .setModel(requestDto.getModel())
                .setBrand(requestDto.getBrand())
                .setType(requestDto.getType())
                .setInventory(requestDto.getInventory())
                .setDailyFee(requestDto.getDailyFee());

        CarDto carDto = new CarDto()
                .setId(1L)
                .setModel(car.getModel())
                .setBrand(car.getBrand())
                .setType(car.getType())
                .setInventory(car.getInventory())
                .setDailyFee(car.getDailyFee());

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateCarFromDto(requestDto, car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto updateCar = carService.updateCar(1L, requestDto);

        assertThat(updateCar). isEqualTo(carDto);
    }

    @DisplayName("Update car with non-existing ID should throw EntityNotFoundException")
    @Test
    void updateCar_WithNonExistingCar_ShouldThrowEntityNotFoundException() {
        Long carId = 100L;
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carService.updateCar(carId, requestDto));
    }
}
