
package com.anil.stockflow;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void handleDatabaseErrorShouldReturnInternalServerError() {
        DataAccessResourceFailureException exception =
                new DataAccessResourceFailureException(
                        "Database connection failed");

        ResponseEntity<?> response =
                handler.handleDatabaseError(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
    }

    @Test
    void handleDataIntegrityViolationShouldReturnConflict() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Duplicate username");

        ResponseEntity<?> response =
                handler.handleDataIntegrityViolation(exception);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode());
    }
}
