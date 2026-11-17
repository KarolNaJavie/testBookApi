package com.example.library.services;

import com.example.library.model.Client;
import com.example.library.model.ClientDTO;
import com.example.library.model.Subscription;
import com.example.library.model.SubscriptionDTO;
import com.example.library.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public ClientDTO register(ClientDTO clientDTO) {

        if (clientRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        Subscription subscription = Subscription.builder()
                .subscribedAuthors(new ArrayList<>())
                .subscribedCategories(new ArrayList<>())
                .build();
        Client client = Client.builder()
                .name(clientDTO.getName())
                .surname(clientDTO.getSurname())
                .email(clientDTO.getEmail())
                .subscription(subscription)
                .password(passwordEncoder.encode(clientDTO.getPassword()))
                .build();

        Client savedClient = clientRepository.save(client);
mailService.sendConfirmation(client.getEmail(), "Konto zalozone pomyslnie!");

        return ClientDTO.builder()
                .name(savedClient.getName())
                .surname(savedClient.getSurname())
                .email(savedClient.getEmail())
                .build();
    }

    public void addSubscription(SubscriptionDTO dto) {

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Authenticated user not found: " + email));

        if (dto.getAuthor() != null) {
            client.getSubscription().getSubscribedAuthors().add(dto.getAuthor());
        }

        if (dto.getCategory() != null) {
            client.getSubscription().getSubscribedCategories().add(dto.getCategory());
        }

        clientRepository.save(client);
    }

    public void removeSubscription(SubscriptionDTO dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getSubscription() == null) {
            return;
        }

        if (dto.getAuthor() != null) {
            client.getSubscription().getSubscribedAuthors()
                    .remove(dto.getAuthor());
        }

        if (dto.getCategory() != null) {
            client.getSubscription().getSubscribedCategories()
                    .remove(dto.getCategory());
        }

        clientRepository.save(client);
    }
}
