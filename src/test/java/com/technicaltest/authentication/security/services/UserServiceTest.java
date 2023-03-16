package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.models.ERole;
import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.UserUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.technicaltest.authentication.payload.response.MessageResponse.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void updateUserCaseUserNotFound() {
        // Setup
        Optional<User> optionalUser = Optional.empty();
        doReturn(optionalUser).when(userRepository).findByUsername(any());

        // Test
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(1l);
        userUpdateRequest.setUsername("admin");

        MessageResponse messageResponse = userService.updateUser(userUpdateRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_NOT_FOUND);
        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUserCaseUserNotFoundWrongId() {
        // Setup
        User user = new User();
        user.setUsername("admin");
        user.setId(2l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        // Test
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(1l);
        userUpdateRequest.setUsername("admin");

        MessageResponse messageResponse = userService.updateUser(userUpdateRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_NOT_FOUND);
        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUserCaseRandomStringAsRole() {
        // Setup
        Role role = new Role();
        role.setId(1);
        role.setName(ERole.ROLE_USER);

        doReturn(Optional.of(role)).when(roleRepository).findByName(any());

        User user = new User();
        user.setUsername("admin");
        user.setId(1l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        // Test
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(1l);
        userUpdateRequest.setUsername("admin");
        userUpdateRequest.setRoles(List.of(ERole.ROLE_USER.getRoleId(), "randomString"));

        MessageResponse messageResponse = userService.updateUser(userUpdateRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(ROLE_NOT_FOUND);
        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUserSuccess() {
        // Setup
        Role role = new Role();
        role.setId(1);
        role.setName(ERole.ROLE_USER);

        doReturn(Optional.of(role)).when(roleRepository).findByName(any());

        User user = new User();
        user.setId(1l);
        user.setUsername("admin");
        user.setEmail("admin@oldEmail.lu");
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        // Test
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(1l);
        userUpdateRequest.setUsername("admin");
        userUpdateRequest.setEmail("admin@newmail.lu");
        userUpdateRequest.setRoles(List.of(ERole.ROLE_USER.getRoleId()));

        MessageResponse messageResponse = userService.updateUser(userUpdateRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_UPDATE_SUCCESS);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findByUsername("admin");
        verify(userRepository).save(capturedUser.capture());
        verifyNoMoreInteractions(userRepository);
        assertThat(capturedUser.getValue().getId()).isEqualTo(1L);
        assertThat(capturedUser.getValue().getUsername()).isEqualTo("admin");
        assertThat(capturedUser.getValue().getEmail()).isEqualTo("admin@newmail.lu");
        assertThat(capturedUser.getValue().getRoles().stream().map(authority -> authority.getName().getRoleId()).collect(Collectors.toList()))
                .containsOnlyElementsOf(userUpdateRequest.getRoles());
    }


    @Test
    public void passwordChangeCaseUserNotFound() {
        // Setup
        Optional<User> optionalUser = Optional.empty();
        doReturn(optionalUser).when(userRepository).findByUsername(any());

        // Test
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setId(1L);
        passwordChangeRequest.setUsername("admin");
        passwordChangeRequest.setPassword("password");

        MessageResponse messageResponse = userService.changePassword(passwordChangeRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_NOT_FOUND);
        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void passwordChangeCaseUserNotFoundWrongId() {
        // Setup
        User user = new User();
        user.setUsername("admin");
        user.setId(2l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        // Test
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setId(1L);
        passwordChangeRequest.setUsername("admin");
        passwordChangeRequest.setPassword("password");

        MessageResponse messageResponse = userService.changePassword(passwordChangeRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_NOT_FOUND);
        verify(userRepository).findByUsername("admin");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void passwordChangeCaseSuccess() {
        // Setup
        User user = new User();
        user.setId(1l);
        user.setUsername("admin");
        user.setPassword("oldPassword");
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        doReturn("encryptedPassword").when(encoder).encode(any());

        // Test
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(1L, "admin", "password");

        MessageResponse messageResponse = userService.changePassword(passwordChangeRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(PASSWORD_CHANGE_SUCCESS);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findByUsername("admin");
        verify(userRepository).save(capturedUser.capture());
        assertThat(capturedUser.getValue().getPassword()).isEqualTo("encryptedPassword");
        verifyNoMoreInteractions(userRepository);
    }

}