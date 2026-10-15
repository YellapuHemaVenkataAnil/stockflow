
package com.anil.stockflow.controller;

import com.anil.stockflow.dto.RegisterRequest;
import com.anil.stockflow.dto.UserResponse;
import com.anil.stockflow.entity.User;
import com.anil.stockflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.anil.stockflow.dto.LoginRequest;
import com.anil.stockflow.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(
                request.username(),
                request.password()
        );

        return ResponseEntity.ok(response);
    }
}
