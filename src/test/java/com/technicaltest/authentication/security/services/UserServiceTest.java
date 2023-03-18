package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.UserRoleUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.technicaltest.authentication.models.ERole.*;
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
        UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest();
        userRoleUpdateRequest.setId(1l);
        userRoleUpdateRequest.setUsername("admin");

        MessageResponse messageResponse = userService.updateUserRoles(userRoleUpdateRequest).getBody();

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
        UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest();
        userRoleUpdateRequest.setId(1l);
        userRoleUpdateRequest.setUsername("admin");

        MessageResponse messageResponse = userService.updateUserRoles(userRoleUpdateRequest).getBody();

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
        role.setName(ROLE_USER);

        doReturn(Optional.of(role)).when(roleRepository).findByName(any());

        User user = new User();
        user.setUsername("admin");
        user.setId(1l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        mockSecurityContextHolderUserName("adminUsername", false);

        // Test
        UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest();
        userRoleUpdateRequest.setId(1l);
        userRoleUpdateRequest.setUsername("admin");
        userRoleUpdateRequest.setRoles(List.of(ROLE_USER.getRoleId(), "randomString"));

        MessageResponse messageResponse = userService.updateUserRoles(userRoleUpdateRequest).getBody();

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
        role.setName(ROLE_USER);

        doReturn(Optional.of(role)).when(roleRepository).findByName(any());

        User user = new User();
        user.setId(1l);
        user.setUsername("admin");
        user.setEmail("admin@oldEmail.lu");
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        // Test
        UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest();
        userRoleUpdateRequest.setId(1l);
        userRoleUpdateRequest.setUsername("admin");
        userRoleUpdateRequest.setRoles(List.of(ROLE_USER.getRoleId()));

        MessageResponse messageResponse = userService.updateUserRoles(userRoleUpdateRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_ROLE_UPDATE_SUCCESS);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findByUsername("admin");
        verify(userRepository).save(capturedUser.capture());
        verifyNoMoreInteractions(userRepository);
        assertThat(capturedUser.getValue().getId()).isEqualTo(1L);
        assertThat(capturedUser.getValue().getUsername()).isEqualTo("admin");
        assertThat(capturedUser.getValue().getRoles().stream().map(authority -> authority.getName().getRoleId()).collect(Collectors.toList()))
                .containsOnlyElementsOf(userRoleUpdateRequest.getRoles());
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
    public void passwordChangeCaseNotCurrentConnectedUser() {
        // Setup
        User user = new User();
        user.setUsername("admin");
        user.setId(1l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        mockSecurityContextHolderUserName("admin2", false);

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
    public void passwordChangeCaseNotCurrentConnectedUserButAdmin() {
        // Setup
        User user = new User();
        user.setUsername("admin");
        user.setId(1l);
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findByUsername("admin");

        mockSecurityContextHolderUserName("admin2", true);

        doReturn("encryptedPassword").when(encoder).encode(any());

        // Test
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setId(1L);
        passwordChangeRequest.setUsername("admin");
        passwordChangeRequest.setPassword("password");

        MessageResponse messageResponse = userService.changePassword(passwordChangeRequest).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(PASSWORD_CHANGE_SUCCESS);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findByUsername("admin");
        verify(userRepository).save(capturedUser.capture());
        assertThat(capturedUser.getValue().getPassword()).isEqualTo("encryptedPassword");
        verifyNoMoreInteractions(userRepository);
    }

    private static void mockSecurityContextHolderUserName(String userName, boolean hasAdminRole) {
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        doReturn(userName).when(userDetails).getUsername();
        SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority(ROLE_ADMIN.getRoleId());
        if (hasAdminRole) {
        doReturn(List.of(adminAuthority)).when(userDetails).getAuthorities();
        }

        Authentication authentication = mock(Authentication.class);
        doReturn(userDetails).when(authentication).getPrincipal();

        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
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

        mockSecurityContextHolderUserName("admin", false);

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

    @Test
    public void deleteUserCaseUserNotFound() {
        // Setup
        Optional<User> optionalUser = Optional.empty();
        doReturn(optionalUser).when(userRepository).findById(1L);

        // Test
        MessageResponse messageResponse = userService.deleteUser(1L).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_NOT_FOUND);
        verify(userRepository).findById(1l);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void userDeletionCaseCurrentConnectedUser() {
        // Setup
        User user = new User();
        user.setId(1l);
        user.setUsername("admin");
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findById(1L);

        mockSecurityContextHolderUserName("admin", false);

        // Test
        MessageResponse messageResponse = userService.deleteUser(1L).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(CAN_NOT_DELETE_OWN_ACCOUNT);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void userDeletionCaseSuccess() {
        // Setup
        User user = new User();
        user.setId(1l);
        user.setUsername("admin");
        Optional<User> optionalUser = Optional.of(user);
        doReturn(optionalUser).when(userRepository).findById(1L);

        mockSecurityContextHolderUserName("admin2", false);

        // Test
        MessageResponse messageResponse = userService.deleteUser(1L).getBody();

        // Assertions
        assertThat(messageResponse.getMessage()).isEqualTo(USER_DELETION_SUCCESS);
        ArgumentCaptor<User> capturedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findById(1L);
        verify(userRepository).delete(capturedUser.capture());
        assertThat(capturedUser.getValue().getUsername()).isEqualTo("admin");
        verifyNoMoreInteractions(userRepository);
    }
}