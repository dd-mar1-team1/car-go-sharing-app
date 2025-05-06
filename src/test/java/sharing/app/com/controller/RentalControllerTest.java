package sharing.app.com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sharing.app.com.config.CustomPageImpl;
import sharing.app.com.dto.rental.CreateRentalRequestDto;
import sharing.app.com.dto.rental.RentalDto;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {
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

    @DisplayName("Create rental with valid data should succeed")
    @Sql(scripts = "classpath:database/cars/rental/add-car-and-user-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void createRental_ValidRequestDto_Success() throws Exception {
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto()
                .setCarId(1L)
                .setUserId(2L)
                .setRentalDate(LocalDate.now())
                .setReturnDate(LocalDate.of(2025, 5, 25));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        RentalDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), RentalDto.class);

        assertEquals(1L, actual.getCarId());
        assertEquals(2L, actual.getUserId());
    }

    @DisplayName("Create rental with missing return date should fail with 400 Bad Request")
    @Sql(scripts = "classpath:database/cars/rental/add-car-and-user-id.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void createRental_MissingReturnDate_BadRequest() throws Exception {
        CreateRentalRequestDto invalidRequest = new CreateRentalRequestDto()
                .setCarId(1L)
                .setUserId(2L)
                .setRentalDate(LocalDate.now());

        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Get rentals by user with active status should return one active rental")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "customer@example.com",
            userDetailsServiceBeanName = "userDetailsService")
    @Test
    void getRentalsByUserIdAndStatus_ValidUserAndActiveStatus_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/rentals").param("isActive", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(CustomPageImpl.class, RentalDto.class);
        PageImpl<RentalDto> actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), type
        );
        RentalDto rental = actual.getContent().getFirst();

        assertEquals(1, actual.getContent().size());
        assertNull(rental.getActualReturnDate());
    }

    @DisplayName("Get rental by ID as manager should return rental successfully")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void getRentalById_ValidRentalId_Success() throws Exception {
        Long rentalId = 1L;
        RentalDto expected = new RentalDto()
                .setId(1L)
                .setCarId(1L)
                .setUserId(2L)
                .setRentalDate(LocalDate.of(2025, 4, 25))
                .setReturnDate(LocalDate.of(2025, 4, 30));

        MvcResult result = mockMvc.perform(get("/rentals/{id}", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(content, RentalDto.class);

        assertEquals(expected, actual);
    }

    @DisplayName("Get rental by non-existing ID should return 404 Not Found")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void getRentalById_NonExistingRentalId_NotFound() throws Exception {
        Long nonExistingRentalId = 100L;

        mockMvc.perform(get("/rentals/{id}", nonExistingRentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Get rental by ID with no access should return 403 Forbidden")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "other_customer", roles = {"CUSTOMER"})
    @Test
    void getRentalById_UserWithoutAccess_Forbidden() throws Exception {
        Long rentalId = 1L;

        mockMvc.perform(get("/rentals/{id}", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Return rented car by ID should update return date")
    @Sql(scripts = "classpath:database/cars/rental/add-user-car-and-rentals.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/rental/delete-rental.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void returnCar_ValidRentalId_Success() throws Exception {
        Long rentalId = 1L;
        MvcResult result = mockMvc.perform(post("/rentals/{id}/return", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        RentalDto actual = objectMapper.readValue(content, RentalDto.class);

        assertNotNull(actual.getActualReturnDate());
    }
}
