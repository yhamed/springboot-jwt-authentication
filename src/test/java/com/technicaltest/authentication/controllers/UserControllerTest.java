package com.technicaltest.authentication.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.technicaltest.authentication.UserAuthenticationSkeletonApplication;
import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.LoginRequest;
import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.SignupRequest;
import com.technicaltest.authentication.payload.request.UserRoleUpdateRequest;
import com.technicaltest.authentication.payload.response.JwtResponse;
import com.technicaltest.authentication.payload.response.MessageResponse;
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
import java.util.Set;

import static com.technicaltest.authentication.models.ERole.ROLE_ADMIN;
import static com.technicaltest.authentication.models.ERole.ROLE_USER;
import static com.technicaltest.authentication.payload.response.MessageResponse.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
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
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Order(1)
    @Test
    public void userControllerAsAdmin_IntegrationTests() throws Exception {
        // Setup
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(ROLE_USER);

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName(ROLE_ADMIN);

        User admin = new User("admin", "admin@email.lu", null);
        admin.setId(1L);
        admin.setRoles(Set.of(adminRole, userRole));

        User user = new User("user", "user@email.lu", null);
        user.setId(2L);
        user.setRoles(Set.of(userRole));

        // Register the admin and simple user accounts
        signUp("admin", "password", "admin@email.lu");
        signUp("user", "password", "user@email.lu");

        // Setup - SignUp for the first time and SignIn to recover a token
        JwtResponse response = signIn(convertToJson(new LoginRequest("admin", "password")));

        String bearerHeaderValue = response.getTokenType() + " " + response.getAccessToken();

        checkCreatedUsers(List.of(admin, user), 2, bearerHeaderValue);

        // Test - Admin can change his password
        checkUserCanChangePassword(bearerHeaderValue, new PasswordChangeRequest(admin.getId(), admin.getUsername(), "1234567"));

        // Test - Admin can change other users passwords
        checkUserCanChangePassword(bearerHeaderValue, new PasswordChangeRequest(user.getId(), user.getUsername(), "1234567"));

        // Test - delete user
        checkUserDeletion(user.getId(), bearerHeaderValue);

        // Test - check that user has been suspended
        checkCreatedUsers(List.of(admin), 1, bearerHeaderValue);
    }

    @Order(2)
    @Test
    public void userControllerAsUser_IntegrationTests() throws Exception {
        // Setup
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(ROLE_USER);

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName(ROLE_ADMIN);

        User admin = new User("admin", "admin@email.lu", null);
        admin.setId(1L);
        admin.setRoles(Set.of(adminRole, userRole));

        User user = new User("user", "user@email.lu", null);
        user.setId(2L);
        user.setRoles(Set.of(userRole));

        // Register the admin and simple user accounts
        signUp("admin", "password", "admin@email.lu");
        signUp("user", "password", "user@email.lu");

        // Setup - SignUp for the first time and SignIn to recover a token
        JwtResponse response = signIn(convertToJson(new LoginRequest("user", "password")));

        String bearerHeaderValue = response.getTokenType() + " " + response.getAccessToken();

        // Test - User should be Forbidden access to Fetch ALL
        checkUserFetchAll(bearerHeaderValue);

        // Test - User should be Forbidden access to Role update
        checkUserRoleUpdate(bearerHeaderValue, new UserRoleUpdateRequest(1L, "admin", emptyList()));

        // Test - User should be Forbidden access to Role update
        checkSimpleUserAccessToDeleteResource(admin.getId(), bearerHeaderValue);

        // Test - User can not change admin password
        checkForbiddenPasswordChange(bearerHeaderValue, new PasswordChangeRequest(admin.getId(), admin.getUsername(), "1234567"));

        // Test - User can change his own password
        checkUserCanChangePassword(bearerHeaderValue, new PasswordChangeRequest(user.getId(), user.getUsername(), "1234567"));
    }

    @Order(3)
    @Test
    public void userControllerAsAdminRoleUpdateAndSuspendUser_Scenario_IntegrationTests() throws Exception {
        // Setup
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(ROLE_USER);

        Role adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName(ROLE_ADMIN);

        User admin = new User("admin", "admin@email.lu", null);
        admin.setId(1L);
        admin.setRoles(Set.of(adminRole, userRole));

        User user = new User("user", "user@email.lu", null);
        user.setId(2L);
        user.setRoles(Set.of(userRole));

        // Register the admin and simple user accounts
        signUp("admin", "password", "admin@email.lu");
        signUp("user", "password", "user@email.lu");

        // Setup - SignUp for the first time and SignIn to recover a token
        JwtResponse response = signIn(convertToJson(new LoginRequest("admin", "password")));

        String bearerHeaderValue = response.getTokenType() + " " + response.getAccessToken();

        // Test - check update Roles - case Admin can't change his own grants
        checkUpdateOwnRolesReturnBadRequest(bearerHeaderValue, new UserRoleUpdateRequest(admin.getId(), admin.getUsername(), List.of("ROLE_USER")));

        // Test -  check update Roles - Suspend User isSuccess
        checkUpdateRolesIsOK(bearerHeaderValue, new UserRoleUpdateRequest(user.getId(), user.getUsername(), emptyList()));

        // Test - check that user has been suspended
        user.setRoles(emptySet());
        checkCreatedUsers(List.of(admin, user), 2, bearerHeaderValue);
        checkUserIsSuspended(convertToJson(new LoginRequest("user", "password")));
    }

    private void checkUserIsSuspended(String requestBody) throws Exception {
        String result = doSignIn(requestBody)
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
        assertThat(parseJsonResponse(result, MessageResponse.class).getMessage()).isEqualTo(ACCOUNT_SUSPENDED);
    }

    private void checkSimpleUserAccessToDeleteResource(Long id, String bearerHeaderValue) throws Exception {
        doDelete(id, bearerHeaderValue).andExpect(status().isForbidden());
    }

    private void checkUserFetchAll(String bearerHeaderValue) throws Exception {
        doFetchUsers(bearerHeaderValue).andExpect(status().isForbidden());
    }

    private void checkUserRoleUpdate(String bearerHeaderValue, UserRoleUpdateRequest userRoleUpdateRequest) throws Exception {
        doUpdateRoles(bearerHeaderValue, userRoleUpdateRequest).andExpect(status().isForbidden());
    }

    private void checkUserDeletion(Long id, String bearerHeaderValue) throws Exception {
        String result = doDelete(id, bearerHeaderValue).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(parseJsonResponse(result, MessageResponse.class).getMessage()).isEqualTo(USER_DELETION_SUCCESS);
    }

    private void checkCreatedUsers(List<User> expectedUSers, Integer expectedSize, String bearerHeaderValue) throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.get("/api/user/fetch")
                        .header("Authorization", bearerHeaderValue)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        // Assertions - check created Users
        User[] users = parseJsonResponse(result, User[].class);
        assertThat(users).isNotEmpty();
        assertThat(users.length).isEqualTo(expectedSize);
        assertThat(users).containsOnlyElementsOf(expectedUSers);
    }

    private void checkForbiddenPasswordChange(String bearerHeaderValue, PasswordChangeRequest passwordChangeRequest) throws Exception {
        doChangePassword(bearerHeaderValue, passwordChangeRequest)
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
    }

    private void checkUserCanChangePassword(String bearerHeaderValue, PasswordChangeRequest passwordChangeRequest) throws Exception {
        String passwordChangeResult = doChangePassword(bearerHeaderValue, passwordChangeRequest)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(parseJsonResponse(passwordChangeResult, MessageResponse.class).getMessage()).isEqualTo(PASSWORD_CHANGE_SUCCESS);
    }

    private void checkUpdateOwnRolesReturnBadRequest(String bearerHeaderValue, UserRoleUpdateRequest userRoleUpdateRequest) throws Exception {
        String updateRolesResult = doUpdateRoles(bearerHeaderValue, userRoleUpdateRequest)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(parseJsonResponse(updateRolesResult, MessageResponse.class).getMessage()).isEqualTo(CAN_NOT_REVOKE_OWN_AUTHORITY);
    }

    private void checkUpdateRolesIsOK(String bearerHeaderValue, UserRoleUpdateRequest userRoleUpdateRequest) throws Exception {
        String updateRolesResult = doUpdateRoles(bearerHeaderValue, userRoleUpdateRequest)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(parseJsonResponse(updateRolesResult, MessageResponse.class).getMessage()).isEqualTo(USER_ROLE_UPDATE_SUCCESS);
    }

    private ResultActions doFetchUsers(String bearerHeaderValue) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get("/api/user/fetch")
                .header("Authorization", bearerHeaderValue)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions doDelete(Long id, String bearerHeaderValue) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete("/api/user/delete/" + id)
                .header("Authorization", bearerHeaderValue)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions doChangePassword(String bearerHeaderValue, PasswordChangeRequest passwordChangeRequest) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.patch("/api/user/password")
                .header("Authorization", bearerHeaderValue)
                .content(convertToJson(passwordChangeRequest))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions doUpdateRoles(String bearerHeaderValue, UserRoleUpdateRequest userRoleUpdateRequest) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.put("/api/user/role")
                .header("Authorization", bearerHeaderValue)
                .content(convertToJson(userRoleUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private void signUp(String username, String password, String email) throws Exception {
        String result = doSignUp(convertToJson(new SignupRequest(username, email, password)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Assertions  - Verify created first user has Admin role
        MessageResponse firstAdminSignupResponse = parseJsonResponse(result, MessageResponse.class);
        assertThat(firstAdminSignupResponse.getMessage()).isEqualTo(USER_CREATION_SUCCESS);
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

    private ResultActions doSignUp(String body) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions doSignIn(String requestBody) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));
    }

    private JwtResponse signIn(String requestBody) throws Exception {
        return parseJsonResponse(doSignIn(requestBody)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), JwtResponse.class);
    }
}