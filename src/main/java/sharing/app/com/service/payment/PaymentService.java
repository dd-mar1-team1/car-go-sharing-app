package sharing.app.com.service.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sharing.app.com.dto.payment.CreatePaymentRequestDto;
import sharing.app.com.dto.payment.PaymentDto;

public interface PaymentService {
    Page<PaymentDto> getPaymentsByUser(Long userId, Pageable pageable);

    PaymentDto createPaymentSession(CreatePaymentRequestDto requestDto);

    PaymentDto handleSuccess(String sessionId);

    String handleCancel(String sessionId);
}
