package com.anil.stockflow.service;

import com.anil.stockflow.dto.LoginResponse;
import com.anil.stockflow.entity.User;
import com.anil.stockflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                3600000L
        );
    }

    @Test
    void registerShouldCreateUserWithEncodedPasswordAndUserRole() {
        when(userRepository.existsByUsername("anil"))
                .thenReturn(false);
        when(userRepository.existsByEmail("anil@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(
                "anil", "anil@example.com", "password123");

        assertEquals("anil", result.getUsername());
        assertEquals("anil@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals("USER", result.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldFailWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("anil"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(
                        "anil", "anil@example.com", "password123")
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldFailWhenEmailAlreadyExists() {
        when(userRepository.existsByUsername("anil"))
                .thenReturn(false);
        when(userRepository.existsByEmail("anil@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(
                        "anil", "anil@example.com", "password123")
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreCorrect() {
        User user = new User(
                "anil",
                "anil@example.com",
                "encoded-password",
                "USER"
        );

        when(userRepository.findByUsername("anil"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);
        when(jwtService.generateToken("anil"))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login("anil", "password123");

        assertNotNull(response);
        assertEquals("test-jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }

    @Test
    void loginShouldFailWhenUsernameDoesNotExist() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("unknown", "password123")
        );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginShouldFailWhenPasswordIsIncorrect() {
        User user = new User(
                "anil",
                "anil@example.com",
                "encoded-password",
                "USER"
        );

        when(userRepository.findByUsername("anil"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("anil", "wrong-password")
        );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );
        verifyNoInteractions(jwtService);
    }
}