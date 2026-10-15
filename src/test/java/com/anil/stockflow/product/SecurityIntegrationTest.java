package com.anil.stockflow;

import com.anil.stockflow.entity.User;
import com.anil.stockflow.product.Product;
import com.anil.stockflow.product.ProductRepository;
import com.anil.stockflow.repository.UserRepository;
import com.anil.stockflow.service.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Test
    void requestWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithInvalidTokenShouldReturn401() throws Exception {
        when(jwtService.extractUsername("invalid.token.here"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoleShouldNotDeleteProduct() throws Exception {
        User user = new User(
                "testuser",
                "testuser@example.com",
                "encoded-password",
                "USER"
        );

        when(jwtService.extractUsername("user-token"))
                .thenReturn("testuser");
        when(jwtService.isTokenValid("user-token", "testuser"))
                .thenReturn(true);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleShouldDeleteProduct() throws Exception {
        User admin = new User(
                "testadmin",
                "testadmin@example.com",
                "encoded-password",
                "ADMIN"
        );

        when(jwtService.extractUsername("admin-token"))
                .thenReturn("testadmin");
        when(jwtService.isTokenValid("admin-token", "testadmin"))
                .thenReturn(true);
        when(userRepository.findByUsername("testadmin"))
                .thenReturn(Optional.of(admin));

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setQuantity(5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());
    }
}
