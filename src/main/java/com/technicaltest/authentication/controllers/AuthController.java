package com.technicaltest.authentication.controllers;

import com.technicaltest.authentication.payload.request.LoginRequest;
import com.technicaltest.authentication.payload.request.SignupRequest;
import com.technicaltest.authentication.security.services.AuthenticationService;
import com.technicaltest.authentication.security.services.SignupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	final AuthenticationService authenticationService;

	final SignupService signupService;

	public AuthController(AuthenticationService authenticationService, SignupService signupService) {
		this.authenticationService = authenticationService;
		this.signupService = signupService;
	}

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		return authenticationService.doAuthenticate(loginRequest);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		return signupService.doSignup(signUpRequest);
	}
}
