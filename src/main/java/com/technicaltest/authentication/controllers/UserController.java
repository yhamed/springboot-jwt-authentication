package com.technicaltest.authentication.controllers;

import com.technicaltest.authentication.payload.request.UserUpdateRequest;
import com.technicaltest.authentication.payload.response.MessageResponse;
import com.technicaltest.authentication.security.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


}
