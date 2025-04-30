package sharing.app.com.dto.car;

import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;
import sharing.app.com.model.Car;

@Accessors(chain = true)
@Data
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private Car.Type type;
    private int inventory;
    private BigDecimal dailyFee;
}
