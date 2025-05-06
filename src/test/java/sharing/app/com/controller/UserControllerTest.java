package sharing.app.com.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sharing.app.com.dto.user.RoleUpdateRequestDto;
import sharing.app.com.dto.user.UserResponseDto;
import sharing.app.com.dto.user.UserUpdateRequestDto;
import sharing.app.com.model.User;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
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

    @DisplayName("Update user role as manager should succeed")
    @Sql(scripts = "classpath:database/cars/users/add-one-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void userUpdateRole_AsManager_ShouldUpdateRoleSuccessfully() throws Exception {
        Long userId = 1L;

        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto()
                .setRole(User.Role.CUSTOMER);
        MvcResult result = mockMvc.perform(put("/users/{id}/role", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(content, UserResponseDto.class);

        assertNotNull(actual);
        assertEquals("user@example.com", actual.getEmail());
        assertEquals(User.Role.CUSTOMER, actual.getRole());
    }

    @DisplayName("Update user role as customer should be forbidden")
    @Sql(scripts = "classpath:database/cars/users/add-user-customer.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void updateRole_AsCustomer_ShouldReturnForbidden() throws Exception {
        Long id = 2L;

        RoleUpdateRequestDto requestDto = new RoleUpdateRequestDto()
                .setRole(User.Role.MANAGER);
        MvcResult result = mockMvc.perform(put("/users/{id}/role", id)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @DisplayName("Get current user profile should return user details")
    @Sql(scripts = "classpath:database/cars/users/add-one-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void getCurrentUserProfile_AuthenticatedUser_ShouldReturnProfile() throws Exception {
        User user = new User()
                .setId(1L)
                .setEmail("user@example.com")
                .setFirstName("Test")
                .setLastName("User")
                .setPassword("qwerty123")
                .setRole(User.Role.CUSTOMER);
        MvcResult result = mockMvc.perform(get("/users/me")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(content, UserResponseDto.class);

        assertEquals("user@example.com", actual.getEmail());
        assertEquals("Test", actual.getFirstName());
    }

    @DisplayName("Update user profile with valid data should succeed")
    @Sql(scripts = "classpath:database/cars/users/add-one-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void updateProfile_ValidData_ShouldUpdateSuccessfully() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto()
                .setEmail("example@example.com")
                .setFirstName("Alan")
                .setLastName("Wake")
                .setPassword("qwerty123")
                .setRepeatPassword("qwerty123");
        MvcResult result = mockMvc.perform(put("/users/me")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        UserResponseDto actual = objectMapper.readValue(content, UserResponseDto.class);

        assertEquals("example@example.com", actual.getEmail());
        assertEquals("Alan", actual.getFirstName());
    }

    @DisplayName("Update user profile with invalid email should return 400 Bad Request")
    @Sql(scripts = "classpath:database/cars/users/add-one-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/cars/users/delete-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    @Test
    void updateProfile_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto()
                .setEmail(" ")
                .setFirstName("Alan")
                .setLastName("Wake")
                .setPassword("qwerty123")
                .setRepeatPassword("qwerty123");

        MvcResult result = mockMvc.perform(put("/users/me")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
