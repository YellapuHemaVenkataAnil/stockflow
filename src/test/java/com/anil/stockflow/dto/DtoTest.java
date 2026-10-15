package com.anil.stockflow.dto;

import com.anil.stockflow.entity.User;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void userResponseShouldCopySafeUserFields() {
        User user = new User(
                "anil",
                "anil@example.com",
                "secret-password",
                "USER"
        );
        user.setId(1L);

        UserResponse response = UserResponse.from(user);

        assertEquals(1L, response.id());
        assertEquals("anil", response.username());
        assertEquals("anil@example.com", response.email());
        assertEquals("USER", response.role());
    }

    @Test
    void registerRequestShouldAcceptValidInput() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anil");
        request.setEmail("anil@example.com");
        request.setPassword("password123");

        Set<?> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequestShouldRejectBlankFields() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(" ");
        request.setEmail(" ");
        request.setPassword(" ");

        Set<?> violations = validator.validate(request);

        assertEquals(6, violations.size());
    }

    @Test
    void registerRequestShouldRejectShortUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setEmail("anil@example.com");
        request.setPassword("password123");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void registerRequestShouldRejectInvalidEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anil");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void registerRequestShouldRejectShortPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anil");
        request.setEmail("anil@example.com");
        request.setPassword("123");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void registerRequestGettersShouldReturnSetValues() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anil");
        request.setEmail("anil@example.com");
        request.setPassword("password123");

        assertEquals("anil", request.getUsername());
        assertEquals("anil@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginRequestShouldAcceptValidInput() {
        LoginRequest request = new LoginRequest("anil", "password123");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void loginRequestShouldRejectBlankUsername() {
        LoginRequest request = new LoginRequest("", "password123");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void loginRequestShouldRejectBlankPassword() {
        LoginRequest request = new LoginRequest("anil", " ");

        assertFalse(validator.validate(request).isEmpty());
    }
}