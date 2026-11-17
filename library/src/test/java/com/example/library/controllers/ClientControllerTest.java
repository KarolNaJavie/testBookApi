package com.example.library.controllers;

import com.example.library.model.ClientDTO;
import com.example.library.model.SubscriptionDTO;
import com.example.library.services.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    @Test
    void testRegister() {
        ClientDTO clientDTO = ClientDTO.builder()
                .name("Jan")
                .surname("Kowalski")
                .email("test@gmail.com")
                .password("password123")
                .build();

        when(clientService.register(clientDTO)).thenReturn(clientDTO);

        ClientDTO result = clientController.register(clientDTO);

        assertEquals("Jan", result.getName());
        assertEquals("Kowalski", result.getSurname());
        assertEquals("test@gmail.com", result.getEmail());

        verify(clientService).register(clientDTO);
    }

    @Test
    void testAddSubscription() {
        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .build();

        clientController.addSubscription(dto);

        verify(clientService).addSubscription(dto);
    }

    @Test
    void testRemoveSubscription() {
        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .build();

        clientController.removeSubscription(dto);

        verify(clientService).removeSubscription(dto);
    }
}
