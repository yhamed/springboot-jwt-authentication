package com.bootstart.authentication.security.services;

import com.bootstart.authentication.payload.request.LoginRequest;
import com.bootstart.authentication.payload.response.JwtResponse;
import com.bootstart.authentication.payload.response.MessageResponse;
import com.bootstart.authentication.repository.RoleRepository;
import com.bootstart.authentication.repository.UserRepository;
import com.bootstart.authentication.security.jwt.JwtUtils;
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

import static com.bootstart.authentication.models.ERole.ROLE_USER;
import static com.bootstart.authentication.payload.response.MessageResponse.ACCOUNT_SUSPENDED;

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

    private Authentication getAuthentication(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    public ResponseEntity<?> doAuthenticate(LoginRequest loginRequest) {

        Authentication authentication = getAuthentication(loginRequest);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = initRoles(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwtUtils.generateJwtToken(authentication),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    private static List<String> initRoles(UserDetailsImpl userDetails) {
        return userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
    }
}
