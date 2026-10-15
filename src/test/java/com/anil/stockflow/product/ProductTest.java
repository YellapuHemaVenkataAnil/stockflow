
package com.anil.stockflow.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTest {

    @Test
    void constructorWithIdShouldSetAllFields() {
        Product product = new Product(
                1L,
                "Laptop",
                new BigDecimal("45000.00"),
                5
        );

        assertEquals(1L, product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals(new BigDecimal("45000.00"), product.getPrice());
        assertEquals(5, product.getQuantity());
    }

    @Test
    void constructorWithoutIdShouldSetRemainingFields() {
        Product product = new Product(
                "Keyboard",
                new BigDecimal("1500.00"),
                10
        );

        assertNull(product.getId());
        assertEquals("Keyboard", product.getName());
        assertEquals(new BigDecimal("1500.00"), product.getPrice());
        assertEquals(10, product.getQuantity());
    }
}
