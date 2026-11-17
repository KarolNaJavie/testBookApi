package com.example.library.services;

import com.example.library.model.Client;
import com.example.library.model.ClientDTO;
import com.example.library.model.Subscription;
import com.example.library.model.SubscriptionDTO;
import com.example.library.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ClientService clientService;

    private void authenticate() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "test@gmail.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    @Test
    void testRegisterHappyPath() {
        ClientDTO dto = ClientDTO.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("password")
                .build();
        Subscription subscription = Subscription.builder()
                .subscribedAuthors(new ArrayList<>())
                .subscribedCategories(new ArrayList<>())
                .build();
        Client client = Client.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .subscription(subscription)
                .build();
        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(clientRepository.save(any(Client.class)))
                .thenReturn(client);

        ClientDTO result = clientService.register(dto);

        assertEquals("Karol", result.getName());
        assertEquals("Testowy", result.getSurname());
        assertEquals("test@gmail.com", result.getEmail());

        verify(passwordEncoder).encode("password");
        verify(clientRepository).save(any(Client.class));
        verify(mailService).sendConfirmation(
                eq("test@gmail.com"),
                eq("Konto zalozone pomyslnie!")
        );
    }

    @Test
    void testRegisterWhenEmailAlreadyExists() {
        Subscription subscription = Subscription.builder()
                .subscribedAuthors(new ArrayList<>())
                .subscribedCategories(new ArrayList<>())
                .build();
        Client client = Client.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .subscription(subscription)
                .build();
        ClientDTO dto = ClientDTO.builder()
                .email("test@gmail.com")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(client));

        assertThrows(RuntimeException.class,
                () -> clientService.register(dto));

        verify(clientRepository, never()).save(any(Client.class));
        verifyNoInteractions(mailService);
    }

    @Test
    void testAddAuthorSubscription() {
        authenticate();
        Subscription subscription = Subscription.builder()
                .subscribedAuthors(new ArrayList<>())
                .subscribedCategories(new ArrayList<>())
                .build();
        Client client = Client.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .subscription(subscription)
                .build();

        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(client));

        clientService.addSubscription(dto);

        assertTrue(client.getSubscription()
                .getSubscribedAuthors().contains("Stephen King"));

        verify(clientRepository).save(client);
    }

    @Test
    void testAddSubscriptionWhenClientDoesNotExist() {
        authenticate();

        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> clientService.addSubscription(dto));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testRemoveSubscription() {
        authenticate();
        Subscription subscription = Subscription.builder()
                .subscribedAuthors(new ArrayList<>())
                .subscribedCategories(new ArrayList<>())
                .build();
        Client client = Client.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .subscription(subscription)
                .build();

        client.getSubscription().getSubscribedAuthors()
                .add("Stephen King");

        client.getSubscription().getSubscribedCategories()
                .add("Fantasy");

        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .category("Fantasy")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(client));

        clientService.removeSubscription(dto);

        assertFalse(client.getSubscription()
                .getSubscribedAuthors().contains("Stephen King"));

        assertFalse(client.getSubscription()
                .getSubscribedCategories().contains("Fantasy"));

        verify(clientRepository).save(client);
    }

}
