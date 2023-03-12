package com.bootstart.authentication.security.services;

import com.bootstart.authentication.models.Role;
import com.bootstart.authentication.models.User;
import com.bootstart.authentication.payload.request.SignupRequest;
import com.bootstart.authentication.payload.response.MessageResponse;
import com.bootstart.authentication.repository.RoleRepository;
import com.bootstart.authentication.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.bootstart.authentication.models.ERole.ROLE_USER;
import static com.bootstart.authentication.payload.response.MessageResponse.*;

@Service
public class SignupService {

    final UserRepository userRepository;

    final RoleRepository roleRepository;

    final PasswordEncoder encoder;

    public SignupService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public ResponseEntity<?> doSignup(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return returnBadRequest(USERNAME_EXISTS);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return returnBadRequest(MAIL_EXISTS);
        }

        createNewUser(signUpRequest);

        return ResponseEntity.ok(new MessageResponse(USER_CREATION_SUCCESS));
    }

    private static ResponseEntity<MessageResponse> returnBadRequest(String message) {
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse(message));
    }

    private void createNewUser(SignupRequest signUpRequest) {
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ROLE_USER)
                    .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND));
            roles.add(userRole);
        }

        user.setRoles(roles);
        userRepository.save(user);
    }
}