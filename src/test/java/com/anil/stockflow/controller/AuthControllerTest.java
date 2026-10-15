
package com.anil.stockflow.controller;

import com.anil.stockflow.dto.LoginResponse;
import com.anil.stockflow.entity.User;
import com.anil.stockflow.service.AuthService;
import com.anil.stockflow.service.JwtService;
import com.anil.stockflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void registerShouldReturn201WhenRequestIsValid() throws Exception {
        User user = new User(
                "anil",
                "anil@example.com",
                "encoded-password",
                "USER"
        );
        user.setId(1L);

        when(authService.register(
                "anil", "anil@example.com", "password123"
        )).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anil",
                                  "email": "anil@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("anil"))
                .andExpect(jsonPath("$.email").value("anil@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(authService).register(
                "anil", "anil@example.com", "password123"
        );
    }

    @Test
    void registerShouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void registerShouldReturn400WhenUsernameAlreadyExists() throws Exception {
        when(authService.register(
                "anil", "anil@example.com", "password123"
        )).thenThrow(new IllegalArgumentException(
                "Username already exists"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anil",
                                  "email": "anil@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturn200WhenCredentialsAreValid() throws Exception {
        when(authService.login("anil", "password123"))
                .thenReturn(new LoginResponse(
                        "test-token", "Bearer", 3600L
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anil",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void loginShouldReturn400WhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void loginShouldReturn400WhenCredentialsAreInvalid() throws Exception {
        when(authService.login("anil", "wrong-password"))
                .thenThrow(new IllegalArgumentException(
                        "Invalid username or password"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "anil",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
