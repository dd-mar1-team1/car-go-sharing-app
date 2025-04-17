package sharing.app.com.dto.payment;

import java.math.BigDecimal;
import java.net.URL;
import lombok.Data;
import sharing.app.com.model.Payment;

@Data
public class PaymentDto {
    private Long id;
    private Payment.Status status;
    private Payment.Type type;
    private Long rentalId;
    private URL sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
