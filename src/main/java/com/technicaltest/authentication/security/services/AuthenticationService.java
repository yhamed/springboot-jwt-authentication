package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.payload.request.LoginRequest;
import com.technicaltest.authentication.payload.response.JwtResponse;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import com.technicaltest.authentication.security.jwt.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.technicaltest.authentication.models.ERole.ROLE_USER;
import static com.technicaltest.authentication.payload.response.MessageResponse.ACCOUNT_SUSPENDED;

@Service
public class AuthenticationService {

    final AuthenticationManager authenticationManager;

    final UserRepository userRepository;

    final RoleRepository roleRepository;

    final PasswordEncoder encoder;

    final JwtUtils jwtUtils;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    public ResponseEntity<?> doAuthenticate(LoginRequest loginRequest) {

        Authentication authentication = getAuthentication(loginRequest);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = initRoles(userDetails);

        if (isSuspendedUser(userDetails)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(ACCOUNT_SUSPENDED));
        }

        return ResponseEntity.ok(new JwtResponse(jwtUtils.generateJwtToken(authentication),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    private Authentication getAuthentication(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    private static List<String> initRoles(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
    }

    private static boolean isSuspendedUser(UserDetailsImpl userDetails) {
        return !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_USER.getRoleId()));
    }
}
