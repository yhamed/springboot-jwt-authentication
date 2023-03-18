package com.technicaltest.authentication.security.services;

import com.technicaltest.authentication.models.Role;
import com.technicaltest.authentication.models.User;
import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.UserRoleUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.repository.RoleRepository;
import com.technicaltest.authentication.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.technicaltest.authentication.models.ERole.*;
import static com.technicaltest.authentication.payload.response.MessageResponse.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

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

    public ResponseEntity<MessageResponse> updateUserRoles(UserRoleUpdateRequest userRoleUpdateRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(userRoleUpdateRequest.getUsername());
        if (hasValidCredentials(optionalUser, userRoleUpdateRequest.getId())) {
            return badRequest(USER_NOT_FOUND);
        }

        UserDetailsImpl userDetails = getConnectedUSer();
        if (userDetails.getUsername().equals(optionalUser.get().getUsername())) {
            return badRequest(CAN_NOT_REVOKE_OWN_AUTHORITY);
        }

        User user = optionalUser.get();
        Set<Role> roles = new HashSet<>();

        try {
            userRoleUpdateRequest.getRoles().stream().forEach(role -> {
                roles.add(roleRepository.findByName(valueOf(role))
                        .orElseThrow(() -> new RuntimeException(ROLE_NOT_FOUND)));
            });
            user.setRoles(roles);
        } catch (RuntimeException runtimeException) {
            return badRequest(ROLE_NOT_FOUND);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse(USER_ROLE_UPDATE_SUCCESS));
    }

    private static UserDetailsImpl getConnectedUSer() {
        return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ResponseEntity<MessageResponse> changePassword(PasswordChangeRequest passwordChangeRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(passwordChangeRequest.getUsername());
        if (hasValidCredentials(optionalUser, passwordChangeRequest.getId())) {
            return new ResponseEntity(new MessageResponse(USER_NOT_FOUND), FORBIDDEN);
        }

        if (isCurrentUser(passwordChangeRequest.getUsername()) || isAdmin()) {
            User user = optionalUser.get();
            user.setPassword(encoder.encode(passwordChangeRequest.getPassword()));

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(PASSWORD_CHANGE_SUCCESS));
        }
        return new ResponseEntity(new MessageResponse(USER_NOT_FOUND), FORBIDDEN);
    }

    public ResponseEntity<MessageResponse> deleteUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        UserDetailsImpl userDetails = getConnectedUSer();
        if (optionalUser.isPresent()) {
            if (userDetails.getUsername().equals(optionalUser.get().getUsername())) {
                return badRequest(CAN_NOT_DELETE_OWN_ACCOUNT);
            }
            userRepository.delete(optionalUser.get());
            return ResponseEntity.ok(new MessageResponse(USER_DELETION_SUCCESS));
        }
        return badRequest(USER_NOT_FOUND);
    }

    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    private static boolean isCurrentUser(String username) {
        UserDetailsImpl userDetails = getConnectedUSer();
        return userDetails.getUsername().equalsIgnoreCase(username);
    }

    private static boolean isAdmin() {
        UserDetailsImpl userDetails = getConnectedUSer();
        return userDetails.getAuthorities().stream()
                .map(role -> role.getAuthority().toUpperCase())
                .collect(Collectors.toList()).contains(ROLE_ADMIN.getRoleId());
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
