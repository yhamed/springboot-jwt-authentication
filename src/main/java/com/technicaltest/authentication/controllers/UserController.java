package com.technicaltest.authentication.controllers;

import com.technicaltest.authentication.payload.request.PasswordChangeRequest;
import com.technicaltest.authentication.payload.request.UserUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.security.services.UserDetailsImpl;
import com.technicaltest.authentication.security.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') and hasAuthority('ROLE_USER')")
    @PutMapping("/user")
    public ResponseEntity<MessageResponse> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return userService.updateUser(userUpdateRequest);
    }

    @PatchMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {

        return userService.changePassword(passwordChangeRequest);
    }


}
