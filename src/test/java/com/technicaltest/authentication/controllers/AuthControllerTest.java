package com.technicaltest.authentication.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.technicaltest.authentication.UserAuthenticationSkeletonApplication;
import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.LoginRequest;
import com.technicaltest.authentication.payload.request.SignupRequest;
import com.technicaltest.authentication.payload.response.JwtResponse;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import com.technicaltest.authentication.security.jwt.JwtUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.technicaltest.authentication.models.ERole.ROLE_ADMIN;
import static com.technicaltest.authentication.models.ERole.ROLE_USER;
import static com.technicaltest.authentication.payload.response.MessageResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserAuthenticationSkeletonApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application.test.properties")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql_scripts/initTestSchema.sql")
@ActiveProfiles({"test"})
@TestMethodOrder(OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Order(1)
    @Test
    public void authController_IntegrationTests() throws Exception {
        // Setup
        Optional<Role> userRole = roleRepository.findByName(ROLE_USER);
        Optional<Role> adminRole = roleRepository.findByName(ROLE_ADMIN);
        assertThat(userRole.isPresent()).isTrue();
        assertThat(adminRole.isPresent()).isTrue();

        String adminUsername = "admin";
        String adminPassword = "adminPassword";
        String adminEmail = "admin@admin.lu";
        Set<Role> expectedAdminRoles = Set.of(userRole.get(), adminRole.get());

        String username = "username";
        String password = "password";
        String email = "username@user.lu";
        Set<Role> expectedUserRoles = Set.of(userRole.get());

        // Test - SignIn return unauthorized because user was not created yet
        signInShouldFailCaseWrongCredentials(adminUsername, adminPassword);

        // Test - First time signup success should have the User and Admin roles
        firstSignupShouldBeAdminTest(adminUsername, adminPassword, adminEmail, expectedAdminRoles);

        // Test - Second signed up success should have only the User role
        secondSignupShouldOnlyHaveUserRole(username, password, email, expectedUserRoles);

        // Test - Signup error in case username already exists
        signupShouldFailCaseUsernameAlreadyExists(username);

        // Test - Signup error in case email already exists
        signupShouldFailCaseEmailAlreadyExists(email);

        // Test - Verify wrong credentials use case
        signInShouldFailCaseWrongCredentials(adminUsername, "wrongPassword");

        // Test
        signInIsOkWithValidJwtResponse(adminUsername, adminPassword, adminEmail, roleSetToStringList(expectedAdminRoles));

        // Test
        signInIsOkWithValidJwtResponse(username, password, email, roleSetToStringList(expectedUserRoles));
    }

    private static List<String> roleSetToStringList(Set<Role> expectedAdminRoles) {
        return expectedAdminRoles.stream().map(role -> role.getName().getRoleId()).collect(Collectors.toList());
    }

    private void signInIsOkWithValidJwtResponse(String username, String password, String email, List<String> roles) throws Exception {
        String result = doSignIn(convertToJson(new LoginRequest(username, password)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Assertions
        JwtResponse signInResponse = parseJsonResponse(result, JwtResponse.class);
        assertAll(
                () -> {
                    assertThat(signInResponse.getId()).isNotNull();
                    assertThat(signInResponse.getUsername()).isEqualTo(username);
                    assertThat(signInResponse.getEmail()).isEqualTo(email);
                    assertThat(signInResponse.getTokenType()).isEqualTo("Bearer");
                    assertThat(signInResponse.getRoles())
                            .containsOnlyElementsOf(roles);
                    assertThat(signInResponse.getAccessToken()).isNotBlank();
                    assertThat(jwtUtils.validateJwtToken(signInResponse.getAccessToken())).isTrue();
                    assertThat(jwtUtils.getUserNameFromJwtToken(signInResponse.getAccessToken())).isEqualTo(username);
                });
    }

    private void signInShouldFailCaseWrongCredentials(String username, String password) throws Exception {
        doSignIn(convertToJson(new LoginRequest(username, password)))
                // Assertions - Returns a 401 unauthorized error
                .andExpect(status().isUnauthorized());
    }

    private void signupShouldFailCaseEmailAlreadyExists(String email) throws Exception {
        String emailAlreadyExistsError = mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertToJson(new SignupRequest("randomUsername", email, "password"))))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        // Assertions - Verify error message
        Assertions.assertThat(parseJsonResponse(emailAlreadyExistsError, MessageResponse.class).getMessage()).isEqualTo(MAIL_EXISTS);
    }

    private void signupShouldFailCaseUsernameAlreadyExists(String username) throws Exception {
        String usernameAlreadyExistsError = mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertToJson(new SignupRequest(username, "random@email.lu", "password"))))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        // Assertions - Verify error message
        assertThat(parseJsonResponse(usernameAlreadyExistsError, MessageResponse.class).getMessage()).isEqualTo(USERNAME_EXISTS);
    }

    private void secondSignupShouldOnlyHaveUserRole(String username, String password, String email, Set<Role> roles) throws Exception {
        String secondRegisteredUserResponse = doSignUp(convertToJson(new SignupRequest(username, email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Assertions - Verify Second created user only has User role
        assertAll(
                () -> {
                    MessageResponse responseSecondSignup = parseJsonResponse(secondRegisteredUserResponse, MessageResponse.class);
                    assertThat(responseSecondSignup.getMessage()).isEqualTo(USER_CREATION_SUCCESS);
                    assertSignedUpUser(username, email, roles);
                });
    }

    private void firstSignupShouldBeAdminTest(String username, String password, String email, Set<Role> roles) throws Exception {
        String result = doSignUp(convertToJson(new SignupRequest(username, email, password)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Assertions  - Verify created first user has Admin role
        MessageResponse firstAdminSignupResponse = parseJsonResponse(result, MessageResponse.class);
        assertAll(
                () -> {
                    assertThat(firstAdminSignupResponse.getMessage()).isEqualTo(USER_CREATION_SUCCESS);
                    assertSignedUpUser(username, email, roles);
                });
    }

    private void assertSignedUpUser(String username, String email, Set<Role> roles) {
        Optional<User> user = userRepository.findByUsername(username);
        assertThat(user.isPresent()).isTrue();
        assertThat(user.get().getUsername()).isEqualTo(username);
        assertThat(user.get().getEmail()).isEqualTo(email);
        assertThat(user.get().getRoles()).containsOnlyElementsOf(roles);
    }

    private ResultActions doSignIn(String requestBody) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private ResultActions doSignUp(String body) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String convertToJson(Object objectToConvert) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(objectToConvert);
    }

    private static <T> T parseJsonResponse(String result, Class<T> type) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result, type);
    }
}