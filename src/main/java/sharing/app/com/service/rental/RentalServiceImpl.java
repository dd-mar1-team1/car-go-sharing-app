package sharing.app.com.service.rental;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
import sharing.app.com.service.telegram.TelegramNotificationService;

@RequiredArgsConstructor
@Service
public class RentalServiceImpl implements RentalService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final UserRepository userRepository;
    private final TelegramNotificationService telegramService;

    @Override
    public RentalDto createRental(CreateRentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + requestDto.getCarId())
        );
        if (car.getInventory() == 0) {
            throw new IllegalStateException("Car is not available for rental");
        }
        if (requestDto.getRentalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Rental date cannot be in the future");
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        Rental rental = createRentalRecord(requestDto, car);
        rentalRepository.save(rental);
        telegramService.sendMessage("New lease created: " + "Auto: " + car.getModel());
        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalDto> getRentalsByUserIdAndStatus(Long userId,
                                                       boolean isActive,
                                                       Pageable pageable) {
        Page<Rental> rentals = isActive
                ? rentalRepository.findAllByUserIdAndActualReturnDateIsNull(userId, pageable)
                : rentalRepository.findAllByUserIdAndActualReturnDateIsNotNull(userId, pageable);

        return rentals.map(rentalMapper::toDto);
    }

    @Override
    public RentalDto getRentalById(Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental by id: " + id)
        );
        return rentalMapper.toDto(rental);
    }

    @Override
    public RentalDto setReturnDate(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental by id: " + rentalId)
        );
        Car car = carRepository.findById(rental.getCar().getId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: "
                        + rental.getCar().getId())
        );
        if (rental.getActualReturnDate() != null) {
            throw new IllegalArgumentException("The car has already been returned");
        }
        rental.setActualReturnDate(LocalDate.now());
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        rentalRepository.save(rental);
        telegramService.sendMessage("Rental completed: Auto: " + car.getModel() + " returned");
        return rentalMapper.toDto(rental);
    }

    private Rental createRentalRecord(CreateRentalRequestDto requestDto, Car car) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: "
                        + requestDto.getUserId()));
        Rental rental = new Rental();
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(requestDto.getReturnDate());
        rental.setCar(car);
        rental.setUser(user);
        return rental;
    }
}
