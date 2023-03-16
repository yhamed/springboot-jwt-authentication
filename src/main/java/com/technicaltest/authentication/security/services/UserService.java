package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.models.ERole;
import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.UserUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    final UserRepository userRepository;

    final RoleRepository roleRepository;

    final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public ResponseEntity<MessageResponse> updateUser(UserUpdateRequest userUpdateRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(userUpdateRequest.getUsername());
        if (hasValidCredentials(optionalUser, userUpdateRequest.getId())) {
            return badRequest(MessageResponse.USER_NOT_FOUND);
        }

        User user = optionalUser.get();

        user.setEmail(userUpdateRequest.getEmail());
        user.setPassword(encoder.encode(userUpdateRequest.getPassword()));

        Set<Role> roles = new HashSet<>();

        try {
            userUpdateRequest.getRoles().stream().forEach(role -> {
                roles.add(roleRepository.findByName(ERole.valueOf(role))
                        .orElseThrow(() -> new RuntimeException(MessageResponse.ROLE_NOT_FOUND)));
            });
            user.setRoles(roles);
        } catch (RuntimeException runtimeException) {
            return badRequest(MessageResponse.ROLE_NOT_FOUND);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(MessageResponse.USER_UPDATE_SUCCESS));
    }

    public ResponseEntity<MessageResponse> changePassword(PasswordChangeRequest passwordChangeRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(passwordChangeRequest.getUsername());
        if (hasValidCredentials(optionalUser, passwordChangeRequest.getId())) {
            return badRequest(MessageResponse.USER_NOT_FOUND);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!userDetails.getUsername().equalsIgnoreCase(passwordChangeRequest.getUsername())) {
            return badRequest(MessageResponse.USER_NOT_FOUND);
        }


        User user = optionalUser.get();
        user.setPassword(encoder.encode(passwordChangeRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(MessageResponse.PASSWORD_CHANGE_SUCCESS));
    }

    private static boolean hasValidCredentials(Optional<User> optionalUser, Long id) {
        return !optionalUser.isPresent() || !id.equals(optionalUser.get().getId());
    }

    private static ResponseEntity<MessageResponse> badRequest(String message) {
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse(message));
    }

}
