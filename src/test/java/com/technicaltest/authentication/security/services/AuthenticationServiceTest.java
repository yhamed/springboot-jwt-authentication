package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.payload.request.LoginRequest;
import com.technicaltest.authentication.payload.response.JwtResponse;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import com.technicaltest.authentication.security.jwt.JwtUtils;
import com.technicaltest.authentication.models.ERole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void doAuthenticate() {
        // Setup
        Set<? extends GrantedAuthority> grantedAuthorities = Set.of(new SimpleGrantedAuthority(ERole.ROLE_USER.getRoleId()), new SimpleGrantedAuthority(ERole.ROLE_ADMIN.getRoleId()));
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        doReturn(grantedAuthorities).when(userDetails).getAuthorities();
        doReturn(1l).when(userDetails).getId();
        doReturn("username").when(userDetails).getUsername();
        doReturn("email@mail.lu").when(userDetails).getEmail();

        Authentication authentication = mock(Authentication.class);
        doReturn(userDetails).when(authentication).getPrincipal();
        doReturn(authentication).when(authenticationManager).authenticate(any());

        // Test
        ResponseEntity<?> response = authenticationService.doAuthenticate(new LoginRequest("username", "password"));

        // Assertions
        assertThat(response.getBody().getClass()).isEqualTo(JwtResponse.class);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertThat(jwtResponse.getRoles()).containsOnlyElementsOf(grantedAuthorities.stream().map(item -> item.getAuthority())
                .collect(Collectors.toList()));
        assertThat(jwtResponse.getId()).isEqualTo(1l);
        assertThat(jwtResponse.getUsername()).isEqualTo("username");
        assertThat(jwtResponse.getEmail()).isEqualTo("email@mail.lu");
    }

    @Test
    public void doAuthenticateCaseUserIsSuspended() {
        // Setup
        Set<? extends GrantedAuthority> grantedAuthorities = Set.of(new SimpleGrantedAuthority(ERole.ROLE_ADMIN.getRoleId()));
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        doReturn(grantedAuthorities).when(userDetails).getAuthorities();

        Authentication authentication = mock(Authentication.class);
        doReturn(userDetails).when(authentication).getPrincipal();
        doReturn(authentication).when(authenticationManager).authenticate(any());

        // Test
        ResponseEntity<?> response = authenticationService.doAuthenticate(new LoginRequest("username", "password"));

        // Assertions
        assertThat(response.getBody().getClass()).isEqualTo(MessageResponse.class);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertThat(messageResponse.getMessage()).isEqualTo(MessageResponse.ACCOUNT_SUSPENDED);
    }

}