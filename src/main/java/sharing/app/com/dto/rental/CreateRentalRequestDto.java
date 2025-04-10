package sharing.app.com.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;
    
    @NotNull
    private Long userId;
    
    @NotNull
    private LocalDate rentalDate;
    
    private LocalDate returnDate;
}
