
package com.anil.stockflow.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
