
package com.anil.stockflow.product;

import com.anil.stockflow.entity.User;
import com.anil.stockflow.repository.UserRepository;
import com.anil.stockflow.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private String getAuthToken(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setEmail(username + "@example.com");
                    newUser.setPassword("test-password");
                    newUser.setRole(role);
                    return userRepository.save(newUser);
                });

        // Ensure the test account has the required role.
        user.setRole(role);
        userRepository.save(user);

        return "Bearer " + jwtService.generateToken(user.getUsername());
    }

    private MockHttpServletRequestBuilder authenticated(
            MockHttpServletRequestBuilder request) {
        return request.header(
                HttpHeaders.AUTHORIZATION,
                getAuthToken("integration-test-user", "USER"));
    }

    private MockHttpServletRequestBuilder authenticatedAsAdmin(
            MockHttpServletRequestBuilder request) {
        return request.header(
                HttpHeaders.AUTHORIZATION,
                getAuthToken("integration-test-admin", "ADMIN"));
    }

    @Test
    void createProductShouldPersistInDatabase() throws Exception {
        mockMvc.perform(authenticated(post("/api/products"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Integration Test Product",
                                    "price": 499.00,
                                    "quantity": 15
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name")
                        .value("Integration Test Product"));

        mockMvc.perform(authenticated(get("/api/products"))
                        .param("search", "Integration Test Product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Integration Test Product"))
                .andExpect(jsonPath("$.content[0].quantity").value(15));
    }

    @Test
    void updateProductShouldPersistChangesInDatabase() throws Exception {
        String response = mockMvc.perform(
                        authenticated(post("/api/products"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Mouse Before Update",
                                            "price": 500.00,
                                            "quantity": 10
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long productId = new ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(
                        authenticated(MockMvcRequestBuilders.put(
                                "/api/products/" + productId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Mouse After Update",
                                            "price": 750.00,
                                            "quantity": 20
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse After Update"))
                .andExpect(jsonPath("$.quantity").value(20));

        mockMvc.perform(authenticated(get("/api/products/" + productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mouse After Update"))
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void deleteProductShouldRemoveItFromDatabase() throws Exception {
        String response = mockMvc.perform(
                        authenticated(post("/api/products"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Product To Delete",
                                            "price": 250.00,
                                            "quantity": 5
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long productId = new ObjectMapper()
                .readTree(response).get("id").asLong();

        mockMvc.perform(authenticatedAsAdmin(
                        MockMvcRequestBuilders.delete(
                                "/api/products/" + productId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(authenticated(get("/api/products/" + productId)))
                .andExpect(status().isNotFound());
    }

    
@Test
void normalUserShouldNotBeAllowedToDeleteProduct() throws Exception {
    String response = mockMvc.perform(
                    authenticated(post("/api/products"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Protected Product",
                                        "price": 300.00,
                                        "quantity": 8
                                    }
                                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long productId = new ObjectMapper()
            .readTree(response)
            .get("id")
            .asLong();

    mockMvc.perform(authenticated(
                    MockMvcRequestBuilders.delete(
                            "/api/products/" + productId)))
            .andExpect(status().isForbidden());

    // Confirm the product still exists.
    mockMvc.perform(authenticated(
                    get("/api/products/" + productId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Protected Product"));
}

}
