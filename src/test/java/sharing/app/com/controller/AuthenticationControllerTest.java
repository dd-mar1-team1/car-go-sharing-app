package sharing.app.com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sharing.app.com.dto.user.UserLoginRequestDto;
import sharing.app.com.dto.user.UserLoginResponseDto;
import sharing.app.com.dto.user.UserRegistrationRequestDto;
import sharing.app.com.dto.user.UserResponseDto;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Login: valid credentials should return a JWT token")
    @Sql(scripts = "classpath:database/cars/users/add-one-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto(
                "user@example.com", "qwerty123"
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UserLoginResponseDto response = objectMapper.readValue(responseBody,
                UserLoginResponseDto.class);

        assertNotNull(response.token());
    }

    @DisplayName("Registration: valid data should create new user and return response")
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void register_ValidRequest_ShouldCreateUserAndReturnResponseDto() throws Exception {
        UserRegistrationRequestDto expected = new UserRegistrationRequestDto()
                .setEmail("newuser@example.com")
                .setFirstName("New")
                .setLastName("User")
                .setPassword("newpassword123")
                .setRepeatPassword("newpassword123");

        MvcResult result = mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expected)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(responseBody,
                UserResponseDto.class);

        assertEquals("newuser@example.com", actual.getEmail());
    }
}
