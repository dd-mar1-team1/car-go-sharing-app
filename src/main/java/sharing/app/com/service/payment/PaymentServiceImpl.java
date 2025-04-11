package sharing.app.com.service.payment;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sharing.app.com.config.StripeConfig;
import sharing.app.com.dto.payment.CreatePaymentRequestDto;
import sharing.app.com.dto.payment.PaymentDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.mapper.PaymentMapper;
import sharing.app.com.model.Payment;
import sharing.app.com.model.Rental;
import sharing.app.com.repository.payment.PaymentRepository;
import sharing.app.com.repository.rental.RentalRepository;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final StripeConfig stripeConfig;

    @Override
    public Page<PaymentDto> getPaymentsByUser(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAllByRentalUserId(userId, pageable);
        return payments.map(paymentMapper::toDto);
    }

    @Override
    public PaymentDto createPaymentSession(CreatePaymentRequestDto requestDto) {
        Rental rental = rentalRepository.findById(requestDto.getRentalId()).orElseThrow(
                () -> new EntityNotFoundException("No rental found for id: "
                        + requestDto.getRentalId())
        );

        if (rental.getActualReturnDate() != null) {
            throw new IllegalArgumentException("Rental completed");
        }

        BigDecimal amountToPay = null;

        if (requestDto.getType() == Payment.Type.PAYMENT) {
            amountToPay = calculateRentalPrice(rental);
        } else if (requestDto.getAmountToPay() == null
                || requestDto.getAmountToPay().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fine amount must be provided and greater than 0.");
        } else {
            amountToPay = requestDto.getAmountToPay();
        }

        Long amountCents = amountToPay.multiply(BigDecimal.valueOf(100)).longValue();

        Session session = stripeConfig.createCheckoutSession("Car Rental Payment",
                amountCents,
                "http://localhost:8080/payments/success?session_id={CHECKOUT_SESSION_ID}",
                "http://localhost:8080/payments/cancel?session_id={CHECKOUT_SESSION_ID}");

        Payment payment = createPaymentFromSession(requestDto, rental, amountToPay, session);

        paymentRepository.save(payment);

        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto handleSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found")
        );
        payment.setStatus(Payment.Status.PAID);
        paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public String handleCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Payment not found")
        );
        return "Payment was cancelled.";
    }

    private BigDecimal calculateRentalPrice(Rental rental) {
        if (rental.getRentalDate() == null) {
            throw new IllegalArgumentException("Rental date is required.");
        }

        LocalDate startDate = rental.getRentalDate();
        LocalDate endDate = rental.getActualReturnDate() != null
                ? rental.getActualReturnDate()
                : rental.getReturnDate();

        if (endDate == null) {
            throw new IllegalArgumentException("Return date is required to calculate price.");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            days = 1;
        }

        BigDecimal dailyFee = rental.getCar().getDailyFee();
        return dailyFee.multiply(BigDecimal.valueOf(days));
    }

    private Payment createPaymentFromSession(CreatePaymentRequestDto requestDto,
                                             Rental rental,
                                             BigDecimal amountToPay,
                                             Session session) {
        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(requestDto.getType());
        payment.setRental(rental);
        payment.setAmountToPay(amountToPay);
        payment.setSessionId(session.getId());
        try {
            payment.setSessionUrl(new URL(session.getUrl()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return payment;
    }
}
