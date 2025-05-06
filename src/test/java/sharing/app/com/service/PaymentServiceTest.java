package sharing.app.com.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sharing.app.com.config.StripeConfig;
import sharing.app.com.dto.payment.CreatePaymentRequestDto;
import sharing.app.com.dto.payment.PaymentDto;
import sharing.app.com.exception.EntityNotFoundException;
import sharing.app.com.mapper.PaymentMapper;
import sharing.app.com.model.Car;
import sharing.app.com.model.Payment;
import sharing.app.com.model.Rental;
import sharing.app.com.repository.payment.PaymentRepository;
import sharing.app.com.repository.rental.RentalRepository;
import sharing.app.com.service.payment.PaymentServiceImpl;
import sharing.app.com.service.telegram.TelegramNotificationService;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private TelegramNotificationService telegramService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private Session session;

    @Mock
    private StripeConfig stripeConfig;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @DisplayName("Receiving user payments: should return a page with PaymentDto")
    @Test
    void getPaymentsByUser_ValidUserId_ReturnsPageOfPaymentDtos() throws MalformedURLException {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        Rental rental = new Rental();
        rental.setId(10L);

        Payment payment = new Payment()
                .setId(100L)
                .setStatus(Payment.Status.PENDING)
                .setType(Payment.Type.PAYMENT)
                .setRental(rental)
                .setSessionId("sess_123")
                .setSessionUrl(new URL("http://session1.com"))
                .setAmountToPay(BigDecimal.valueOf(100));

        PaymentDto paymentDto = new PaymentDto()
                .setId(payment.getId())
                .setStatus(payment.getStatus())
                .setType(payment.getType())
                .setRentalId(payment.getRental().getId())
                .setSessionId(payment.getSessionId())
                .setSessionUrl(payment.getSessionUrl())
                .setAmountToPay(payment.getAmountToPay());

        List<Payment> payments = List.of(payment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, 2);

        when(paymentRepository
                .findAllByRentalUserId(userId, pageable))
                .thenReturn(paymentPage);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        Page<PaymentDto> result = paymentService.getPaymentsByUser(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalPages());
    }

    @DisplayName("Creating a payment session: should create a new session and return PaymentDto")
    @Test
    void createPaymentSession_ValidPaymentTypeRequest_CreatesPaymentSessionAndReturnsDto()
            throws MalformedURLException {
        Car car = new Car()
                .setDailyFee(BigDecimal.valueOf(50));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car);

        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(rental.getId())
                .setType(Payment.Type.PAYMENT);

        Payment savedPayment = new Payment()
                .setId(1L)
                .setStatus(Payment.Status.PENDING)
                .setType(Payment.Type.PAYMENT)
                .setRental(rental)
                .setAmountToPay(BigDecimal.valueOf(250))
                .setSessionId("sess_123")
                .setSessionUrl(new URL("http://mock-session-url.com"));

        PaymentDto expectedDto = new PaymentDto()
                .setId(1L)
                .setStatus(Payment.Status.PENDING)
                .setType(Payment.Type.PAYMENT)
                .setRentalId(rental.getId())
                .setSessionId("sess_123")
                .setSessionUrl(new URL("http://mock-session-url.com"))
                .setAmountToPay(BigDecimal.valueOf(250));

        when(session.getId()).thenReturn("sess_123");
        when(session.getUrl()).thenReturn("http://mock-session-url.com");
        when(rentalRepository
                .findById(rental.getId()))
                .thenReturn(Optional.of(rental));
        when(stripeConfig.createCheckoutSession(
                anyString(),
                anyLong(),
                anyString(),
                anyString()
        )).thenReturn(session);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expectedDto);

        PaymentDto actual = paymentService.createPaymentSession(requestDto);

        assertEquals(expectedDto, actual);
    }

    @DisplayName("Creating a payment session: should throw "
            + "EntityNotFoundException if lease not found")
    @Test
    void createPaymentSession_RentalNotFound_ThrowsEntityNotFoundException() {
        Long rentalId = 999L;
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(rentalId)
                .setType(Payment.Type.PAYMENT);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                paymentService.createPaymentSession(requestDto));
    }

    @DisplayName("Creating a payment session: "
            + "should throw IllegalArgumentException if the car has already been returned")
    @Test
    void createPaymentSession_RentalAlreadyReturned_ThrowsIllegalArgumentException() {
        Car car = new Car()
                .setDailyFee(BigDecimal.valueOf(50));
        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setActualReturnDate(LocalDate.of(2025, 4, 4))
                .setCar(car);

        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(rental.getId())
                .setType(Payment.Type.PAYMENT);

        when(rentalRepository.findById(rental.getId())).thenReturn(Optional.of(rental));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.createPaymentSession(requestDto));
    }

    @DisplayName("Successful payment: should update payment status and send notification")
    @Test
    void handleSuccess_ValidSessionId_UpdatesStatusAndSendsNotification()
            throws MalformedURLException {
        String sessionId = "sess_123";

        Car car = new Car()
                .setDailyFee(BigDecimal.valueOf(50));

        Rental rental = new Rental()
                .setId(1L)
                .setRentalDate(LocalDate.of(2025, 4, 1))
                .setReturnDate(LocalDate.of(2025, 4, 5))
                .setCar(car);

        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(rental.getId())
                .setType(Payment.Type.PAYMENT);

        Payment savedPayment = new Payment()
                .setId(1L)
                .setStatus(Payment.Status.PENDING)
                .setType(Payment.Type.PAYMENT)
                .setRental(rental)
                .setAmountToPay(BigDecimal.valueOf(250))
                .setSessionId(sessionId)
                .setSessionUrl(new URL("http://mock-session-url.com"));

        PaymentDto expectedDto = new PaymentDto()
                .setId(1L)
                .setStatus(Payment.Status.PAID)
                .setType(Payment.Type.PAYMENT)
                .setRentalId(rental.getId())
                .setSessionId(sessionId)
                .setSessionUrl(new URL("http://mock-session-url.com"))
                .setAmountToPay(BigDecimal.valueOf(250));

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(savedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        doNothing().when(telegramService).sendMessage("Successful payment ");
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(expectedDto);

        PaymentDto actual = paymentService.handleSuccess(sessionId);

        assertEquals(expectedDto, actual);
        assertEquals(Payment.Status.PAID, savedPayment.getStatus());
        verify(telegramService).sendMessage("Successful payment ");
    }

    @DisplayName("Payment cancellation: should return a cancellation message")
    @Test
    void handleCancel_ValidSessionId_ReturnsCancelMessage() {
        String sessionId = "sess_123";
        Payment payment = new Payment()
                .setSessionId(sessionId);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        String actual = paymentService.handleCancel(sessionId);

        assertEquals("Payment was cancelled.", actual);
    }
}
