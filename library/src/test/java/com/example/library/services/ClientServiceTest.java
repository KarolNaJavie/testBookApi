
package com.example.library.services;

import com.example.library.model.Client;
import com.example.library.model.ClientDTO;
import com.example.library.model.Subscription;
import com.example.library.model.SubscriptionDTO;
import com.example.library.repository.ClientRepository;
import com.example.library.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private SubscriptionRepository subscriptionRepository;

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

        Client client = Client.builder()
                .id(1L)
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .activated(false)
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(clientRepository.save(any(Client.class)))
                .thenReturn(client);

        ClientDTO result = clientService.register(dto);

        assertNotNull(result);
        assertEquals("Karol", result.getName());
        assertEquals("Testowy", result.getSurname());
        assertEquals("test@gmail.com", result.getEmail());

        verify(passwordEncoder).encode("password");
        verify(clientRepository).save(any(Client.class));

        verify(mailService).sendConfirmation(
                eq("test@gmail.com"),
                eq("Potwierdz adres email: http://localhost:8080/user/activate/1")
        );

        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    void testRegisterWhenEmailAlreadyExists() {
        Client client = Client.builder()
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
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
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testAddAuthorSubscription() {
        authenticate();

        Client client = Client.builder()
                .id(1L)
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .activated(true)
                .build();

        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(client));

        when(subscriptionRepository.existsByClientIdAndAuthor(
                1L, "Stephen King"))
                .thenReturn(false);

        clientService.addSubscription(dto);

        verify(subscriptionRepository).existsByClientIdAndAuthor(
                1L, "Stephen King");

        verify(subscriptionRepository).save(argThat(subscription ->
                "Stephen King".equals(subscription.getAuthor())
                        && subscription.getClient().equals(client)
        ));

        verifyNoInteractions(mailService);
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

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void testRemoveSubscription() {
        authenticate();

        Client client = Client.builder()
                .id(1L)
                .name("Karol")
                .surname("Testowy")
                .email("test@gmail.com")
                .password("encodedPassword")
                .activated(true)
                .build();

        SubscriptionDTO dto = SubscriptionDTO.builder()
                .author("Stephen King")
                .category("Fantasy")
                .build();

        when(clientRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(client));

        clientService.removeSubscription(dto);

        verify(subscriptionRepository).deleteByClientIdAndAuthor(
                1L, "Stephen King");

        verify(subscriptionRepository).deleteByClientIdAndCategory(
                1L, "Fantasy");

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }
}
