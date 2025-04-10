package sharing.app.com.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sharing.app.com.dto.rental.CreateRentalRequestDto;
import sharing.app.com.dto.rental.RentalDto;
import sharing.app.com.model.User;
import sharing.app.com.service.rental.RentalService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(requestDto);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('CUSTOMER')")
    @GetMapping
    public Page<RentalDto> getRentalByUserId(Authentication authentication,
                                             @RequestParam boolean isActive, Pageable pageable) {
        Long userId = getUserId(authentication);
        return rentalService.getRentalsByUserIdAndStatus(userId, isActive, pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{id}")
    public RentalDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/return")
    public RentalDto returnCar(@PathVariable Long id) {
        return rentalService.setReturnDate(id);
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
