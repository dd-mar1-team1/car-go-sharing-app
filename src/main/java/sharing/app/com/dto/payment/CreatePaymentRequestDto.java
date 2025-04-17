package sharing.app.com.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import sharing.app.com.model.Payment;

@Data
public class CreatePaymentRequestDto {
    @NotNull
    private Long rentalId;

    @NotNull
    private Payment.Type type;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amountToPay;
}
