package sharing.app.com.service.rental;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sharing.app.com.dto.rental.CreateRentalRequestDto;
import sharing.app.com.dto.rental.RentalDto;

public interface RentalService {
    RentalDto createRental(CreateRentalRequestDto requestDto);

    Page<RentalDto> getRentalsByUserIdAndStatus(Long userId, boolean isActive, Pageable pageable);

    RentalDto getRentalById(Long id);

    RentalDto setReturnDate(Long rentalId);
}
