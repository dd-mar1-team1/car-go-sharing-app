package sharing.app.com.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import sharing.app.com.dto.rental.CreateRentalRequestDto;
import sharing.app.com.dto.rental.RentalDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.mapper.RentalMapper;
import sharing.app.com.model.Car;
import sharing.app.com.model.Rental;
import sharing.app.com.model.User;
import sharing.app.com.repository.car.CarRepository;
import sharing.app.com.repository.rental.RentalRepository;
import sharing.app.com.repository.user.UserRepository;
import sharing.app.com.service.rental.RentalServiceImpl;
import sharing.app.com.service.telegram.TelegramNotificationService;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramNotificationService telegramService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @DisplayName("Rental creation: should create a new rental and send a notification")
    @Test
    void createRental_ValidRequest_CreatesRentalAndSendsNotification() {
        User user = new User()
                .setId(1L)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.CUSTOMER);

        Car car = new Car()
                .setId(1L)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto()
                .setCarId(car.getId())
                .setUserId(user.getId())
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car)
                .setUser(user);

        RentalDto rentalDto = new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setCarId(rental.getCar().getId())
                .setUserId(rental.getUser().getId());

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        doNothing().when(telegramService)
                .sendMessage("New lease created: " + "Auto: " + car.getModel());
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalDto);

        RentalDto actual = rentalService.createRental(requestDto);

        assertEquals(rentalDto, actual);
    }

    @DisplayName("Rental creation: should throw EntityNotFoundException if car not found")
    @Test
    void createRental_CarNotFound_ThrowsEntityNotFoundException() {
        Long carId = 100L;
        User user = new User()
                .setId(1L);

        Car car = new Car()
                .setId(carId);

        CreateRentalRequestDto requestDto = new CreateRentalRequestDto()
                .setCarId(car.getId())
                .setUserId(user.getId())
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5));

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.createRental(requestDto));
    }

    @DisplayName("Getting active rentals: should return a list of the user's active rentals")
    @Test
    void getRentalsByUserIdAndStatus_ActiveRentals_ReturnsActiveRentals() {
        Long userId = 1L;
        Long carId = 1L;

        User user = new User()
                .setId(userId)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.CUSTOMER);

        Car car = new Car()
                .setId(carId)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car)
                .setUser(user);

        RentalDto rentalDto = new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setCarId(rental.getCar().getId())
                .setUserId(rental.getUser().getId());

        Pageable pageable = PageRequest.of(0, 10);
        List<Rental> rentals = List.of(rental);
        Page<Rental> rentalPage = new PageImpl<>(rentals, pageable, rentals.size());

        when(rentalRepository.findAllByUserIdAndActualReturnDateIsNull(userId, pageable))
                .thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        Page<RentalDto> actual = rentalService.getRentalsByUserIdAndStatus(userId, true, pageable);

        assertThat(actual).hasSize(1);
        assertThat(actual.getContent()).containsExactly(rentalDto);
        assertThat(actual.getContent().getFirst()).isEqualTo(rentalDto);
    }

    @DisplayName("Get inactive rentals: should return a list of the user's completed rentals")
    @Test
    void getRentalsByUserIdAndStatus_InactiveRentals_ReturnsInactiveRentals() {
        Long userId = 1L;
        Long carId = 1L;

        User user = new User()
                .setId(userId)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.CUSTOMER);

        Car car = new Car()
                .setId(carId)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car)
                .setUser(user);

        RentalDto rentalDto = new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setCarId(rental.getCar().getId())
                .setUserId(rental.getUser().getId());

        Pageable pageable = PageRequest.of(0, 10);
        List<Rental> rentals = List.of(rental);
        Page<Rental> rentalPage = new PageImpl<>(rentals, pageable, rentals.size());

        when(rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(userId, pageable))
                .thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        Page<RentalDto> actual = rentalService.getRentalsByUserIdAndStatus(userId, false, pageable);

        assertThat(actual).hasSize(1);
        assertThat(actual.getContent()).containsExactly(rentalDto);
        assertThat(actual.getContent().getFirst()).isEqualTo(rentalDto);
    }

    @DisplayName("Getting a rental by ID: should return RentalDto if found")
    @Test
    void getRentalById_ExistingId_ReturnsRentalDto() {
        User user = new User()
                .setId(1L);

        Car car = new Car()
                .setId(1L);

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car)
                .setUser(user);

        RentalDto expected = new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setCarId(rental.getCar().getId())
                .setUserId(rental.getUser().getId());

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalDto actual = rentalService.getRentalById(1L);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @DisplayName("Getting a rental by ID: should throw EntityNotFoundException if not found")
    @Test
    void getRentalById_NonExistingId_ThrowsEntityNotFoundException() {
        Long rentalId = 100L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.getRentalById(rentalId));

        String expected = "Can't find rental by id: " + rentalId;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
    }

    @DisplayName("Rental End: Should update return date and send notification")
    @Test
    void setReturnDate_ValidRental_UpdatesReturnDateAndSendsNotification() {
        Long userId = 1L;
        Long carId = 1L;

        User user = new User()
                .setId(userId)
                .setEmail("johnny.dough@example.com")
                .setFirstName("Johnny")
                .setLastName("Dough")
                .setPassword("NewSecure123")
                .setRole(User.Role.CUSTOMER);

        Car car = new Car()
                .setId(carId)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car)
                .setUser(user);

        RentalDto expected = new RentalDto()
                .setId(1L)
                .setRentalDate(rental.getRentalDate())
                .setReturnDate(rental.getReturnDate())
                .setCarId(rental.getCar().getId())
                .setUserId(rental.getUser().getId());

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.save(car)).thenReturn(car);
        when(rentalRepository.save(rental)).thenReturn(rental);
        doNothing().when(telegramService)
                .sendMessage("Rental completed: Auto: " + car.getModel() + " returned");
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalDto actual = rentalService.setReturnDate(1L);

        assertEquals(expected, actual);
        assertNotNull(rental.getActualReturnDate());
        assertEquals(LocalDate.now(), rental.getActualReturnDate());
    }

    @DisplayName("Rental termination: should throw an error if rental not found")
    @Test
    void setReturnDate_RentalNotFound_ThrowsEntityNotFoundException() {
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rentalService.setReturnDate(rentalId));

        assertEquals("Can't find rental by id: " + rentalId, exception.getMessage());
    }
}
