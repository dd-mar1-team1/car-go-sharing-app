package sharing.app.com.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;
    
    @NotNull
    private Long userId;
    
    @NotNull
    private LocalDate rentalDate;

    @NotNull
    private LocalDate returnDate;
}
