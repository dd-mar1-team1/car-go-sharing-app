package sharing.app.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sharing.app.com.dto.payment.CreatePaymentRequestDto;
import sharing.app.com.dto.payment.PaymentDto;
import sharing.app.com.service.payment.PaymentService;

@Tag(name = "Payments management", description = "Manage car rental payments")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Get payments by user",
            description = "Get all payments for a user by user ID.")
    @GetMapping
    public Page<PaymentDto> getPaymentByUser(@RequestParam Long userId, Pageable pageable) {
        return paymentService.getPaymentsByUser(userId, pageable);
    }

    @Operation(summary = "Create a payment session",
            description = "Create a payment session using Stripe for car rental.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PaymentDto createPaymentSession(@RequestBody @Valid CreatePaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(requestDto);
    }

    @Operation(summary = "Handle Stripe payment success",
            description = "Redirect endpoint to handle successful payments from Stripe.")
    @GetMapping("/success")
    public PaymentDto handleSuccess(@RequestParam String sessionId) {
        return paymentService.handleSuccess(sessionId);
    }

    @Operation(summary = "Handle Stripe payment cancellation",
            description = "Redirect endpoint to handle payment cancellations from Stripe.")
    @GetMapping("/cancel")
    public String handleCancel(@RequestParam String sessionId) {
        return paymentService.handleCancel(sessionId);
    }
}
