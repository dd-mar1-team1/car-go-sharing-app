package sharing.app.com.dto.car;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import sharing.app.com.model.Car;

@Data
public class CreateCarRequestDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @NotNull
    private Car.Type type;
    @Min(value = 0)
    private int inventory;
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal dailyFee;
}
