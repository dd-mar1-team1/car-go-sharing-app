package sharing.app.com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sharing.app.com.config.CustomPageImpl;
import sharing.app.com.dto.payment.CreatePaymentRequestDto;
import sharing.app.com.dto.payment.PaymentDto;
import sharing.app.com.model.Payment;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Get Payments: valid user ID should return payment list")
    @Sql(scripts = "classpath:database/cars/payment/add-payment.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void getPaymentsByUser_ValidUserId_ReturnsPaymentList() throws Exception {
        MvcResult result = mockMvc.perform(get("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(CustomPageImpl.class,
                        PaymentDto.class);

        PageImpl<PaymentDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), type
        );

        assertEquals(1, actual.getContent().size());
    }

    @DisplayName("Create Payment: valid request should return payment session")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "customer@example.com",
            userDetailsServiceBeanName = "userDetailsService")
    @Test
    void createPaymentSession_ValidRequest_ReturnsPaymentDto() throws Exception {
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(1L)
                .setType(Payment.Type.PAYMENT)
                .setAmountToPay(BigDecimal.valueOf(750.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentDto.class);

        assertEquals(requestDto.getRentalId(), actual.getRentalId());
        assertEquals(requestDto.getType(), actual.getType());
    }

    @DisplayName("Create Payment: invalid amount should return 400 Bad Request")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "customer@example.com",
            userDetailsServiceBeanName = "userDetailsService")
    @Test
    void createPaymentSession_InvalidAmountToPay_ThrowsBadRequest() throws Exception {
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto()
                .setRentalId(1L)
                .setType(Payment.Type.PAYMENT)
                .setAmountToPay(BigDecimal.valueOf(-200));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Handle Payment Success: valid session ID should update and return payment")
    @Sql(scripts = "classpath:database/cars/payment/add-payment.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void handleSuccess_ValidSessionId_ReturnsUpdatedPayment() throws Exception {
        MvcResult result = mockMvc.perform(get("/payments/success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", "sess_123456"))
                .andExpect(status().isOk())
                .andReturn();

        PaymentDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentDto.class);

        assertEquals(Payment.Status.PAID, actual.getStatus());
    }

    @DisplayName("Handle Payment Success: invalid session ID should return 404 Not Found")
    @Sql(scripts = "classpath:database/cars/payment/add-payment.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void handleSuccess_InvalidSessionId_ThrowsNotFound() throws Exception {
        mockMvc.perform(get("/payments/success")
                .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", "sess_654321"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @DisplayName("Handle Payment Cancel: valid session ID should return cancellation message")
    @Sql(scripts = "classpath:database/cars/payment/add-payment.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/payment/delete-payment.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void handleCancel_ValidSessionId_ReturnsCancellationMessage() throws Exception {
        MvcResult result = mockMvc.perform(get("/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sessionId", "sess_123456"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertEquals("Payment was cancelled.", responseBody);
    }
}
