package com.example.library.controllers;

import com.example.library.config.CustomUserDetailsService;
import com.example.library.config.SecurityConfig;
import com.example.library.model.ClientDTO;
import com.example.library.model.SubscriptionDTO;
import com.example.library.services.ClientService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldRegisterClient() throws Exception {

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(clientService).register(any(ClientDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldActivateClient() throws Exception {

        mockMvc.perform(get("/user/activate/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Account activated successfully!"));

        verify(clientService).activateClient(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAddSubscription() throws Exception {

        mockMvc.perform(post("/user/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(clientService).addSubscription(
                any(SubscriptionDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRemoveSubscription() throws Exception {

        mockMvc.perform(delete("/user/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(clientService).removeSubscription(
                any(SubscriptionDTO.class));
    }
}