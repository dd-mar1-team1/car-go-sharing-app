package sharing.app.com.controller;

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

@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto requestDto) {
        return carService.createCar(requestDto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public CarDto findById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        carService.deleteById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id,
                            @Valid @RequestBody CreateCarRequestDto requestDto) {
        return carService.updateCar(id, requestDto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public Page<CarDto> getAll(Pageable pageable) {
        return carService.getAll(pageable);
    }
}
