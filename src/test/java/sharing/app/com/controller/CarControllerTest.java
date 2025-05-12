package sharing.app.com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sharing.app.com.config.CustomPageImpl;
import sharing.app.com.dto.car.CarDto;
import sharing.app.com.dto.car.CreateCarRequestDto;
import sharing.app.com.model.Car;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
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

    @DisplayName("Create Car: valid car data should return created car")
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void createCar_ValidRequestDto_Success() throws Exception {
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Type.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        CarDto expected = new CarDto()
                .setId(1L)
                .setModel(requestDto.getModel())
                .setBrand(requestDto.getBrand())
                .setType(requestDto.getType())
                .setInventory(requestDto.getInventory())
                .setDailyFee(requestDto.getDailyFee());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CarDto.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @DisplayName("Create Car: invalid car data should return 400 Bad Request")
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void createCar_InvalidRequestDto_ShouldReturnBadRequest() throws Exception {
        CreateCarRequestDto invalidRequest = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("")
                .setType(Car.Type.SEDAN)
                .setInventory(-2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @DisplayName("Find Car: existing car by ID should return car details")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void findById_GivenCarById_ShouldReturnCar() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cars/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        CarDto actual = objectMapper.readValue(content, CarDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
    }

    @DisplayName("Find Car: non-existent ID should return 404 Not Found")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void findById_CarByInvalidId_ShouldReturnNotFound() throws Exception {
        Long invalidId = 100L;

        mockMvc.perform(MockMvcRequestBuilders.get("/cars/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Delete Car: existing car should be deleted successfully")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void delete_DeleteCarById_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/cars/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @DisplayName("Update Car: valid data should update and return the car")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void updateCar_ValidUpdateCar_ShouldReturnUpdateCar() throws Exception {
        Long id = 1L;
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("BMW")
                .setBrand("5 Series")
                .setType(Car.Type.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(35.00));

        CarDto expected = new CarDto()
                .setId(1L)
                .setModel("BMW")
                .setBrand("5 Series")
                .setType(Car.Type.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(35.00));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/cars/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expected)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        CarDto actual = objectMapper.readValue(content, CarDto.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @DisplayName("Update Car: invalid data should return 400 Bad Request")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void updateCar_InvalidData_ShouldReturnBadRequest() throws Exception {
        CreateCarRequestDto invalidRequest = new CreateCarRequestDto()
                .setModel("Model S")
                .setBrand("")
                .setType(Car.Type.SEDAN)
                .setInventory(-2)
                .setDailyFee(BigDecimal.valueOf(25.00));

        CarDto result = new CarDto()
                .setId(1L)
                .setModel(invalidRequest.getModel())
                .setBrand(invalidRequest.getBrand())
                .setType(invalidRequest.getType())
                .setInventory(invalidRequest.getInventory())
                .setDailyFee(invalidRequest.getDailyFee());

        String jsonRequest = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(MockMvcRequestBuilders.put("/cars/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Update Car: non-existent ID should return 404 Not Found")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    void updateCar_NonExistentId_ShouldReturnNotFound() throws Exception {
        Long id = 100L;
        CreateCarRequestDto requestDto = new CreateCarRequestDto()
                .setModel("BMW")
                .setBrand("5 Series")
                .setType(Car.Type.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(35.00));

        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(MockMvcRequestBuilders.put("/cars/{id}", id)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Get All Cars: should return list of cars with pagination")
    @Sql(scripts = "classpath:database/cars/add-one-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/delete-all-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Test
    void getAll_GivenCarInCatalog_ShouldReturnAllCars() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andReturn();
        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(CustomPageImpl.class, CarDto.class);
        PageImpl<CarDto> actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), type);

        assertNotNull(actual);
        assertFalse(actual.getContent().isEmpty());
    }
}
