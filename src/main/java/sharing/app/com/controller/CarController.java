package sharing.app.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;
import sharing.app.com.service.car.CarService;

@Tag(name = "Cars management", description = "Manage car inventory")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @Operation(summary = "Add a new car",
            description = "Add a new car to the inventory. Only managers can add cars.")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.createCar(requestDto);
    }

    @Operation(summary = "Get car details",
            description = "Get detailed information about a specific car by ID.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public CarDto findById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @Operation(summary = "Delete a car",
            description = "Delete a car from the inventory. Only managers can delete cars.")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }

    @Operation(summary = "Update car information",
            description = "Update the car details, including inventory. "
                    + "Only managers can update car information.")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id,
                            @Valid @RequestBody CreateCarRequestDto requestDto) {
        return carService.updateCar(id, requestDto);
    }

    @Operation(summary = "Get all cars", description = "Get a paginated list of all cars.")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public Page<CarDto> getAll(Pageable pageable) {
        return carService.getAll(pageable);
    }
}
