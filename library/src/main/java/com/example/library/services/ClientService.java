package com.example.library.services;

import com.example.library.exceptions.UserAlreadySubscibedException;
import com.example.library.exceptions.UserNotActivatedException;
import com.example.library.exceptions.UserNotAuthenticatedException;
import com.example.library.exceptions.UserNotFoundException;
import com.example.library.model.Client;
import com.example.library.model.ClientDTO;
import com.example.library.model.Subscription;
import com.example.library.model.SubscriptionDTO;
import com.example.library.repository.ClientRepository;
import com.example.library.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final SubscriptionRepository subscriptionRepository;

    public ClientDTO register(ClientDTO clientDTO) {

        if (clientRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        Client client = Client.builder()
                .name(clientDTO.getName())
                .surname(clientDTO.getSurname())
                .email(clientDTO.getEmail())
                .activated(false)
                .password(passwordEncoder.encode(clientDTO.getPassword()))
                .build();

        Client savedClient = clientRepository.save(client);
        mailService.sendConfirmation(client.getEmail(), "Potwierdz adres email: http://localhost:8080/user/activate/" + savedClient.getId());

        return ClientDTO.builder()
                .name(savedClient.getName())
                .surname(savedClient.getSurname())
                .email(savedClient.getEmail())
                .build();
    }

    public void activateClient(Long userId) {
        Client client = clientRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));

        client.setActivated(true);

        clientRepository.save(client);
    }

    @Transactional
    public void addSubscription(SubscriptionDTO dto) {

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            throw new UserNotAuthenticatedException("User is not authenticated");
        }

        String email = authentication.getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Authenticated user not found: " + email));

        isActivated(client);

        String author = dto.getAuthor();
        String category = dto.getCategory();

        if ((author == null || author.isBlank()) &&
                (category == null || category.isBlank())) {
            throw new IllegalArgumentException(
                    "Author or category must be provided");
        }

        if (author != null && !author.isBlank()) {
            author = author.trim();

            if (subscriptionRepository.existsByClientIdAndAuthor(
                    client.getId(), author)) {
                throw new UserAlreadySubscibedException("Author already subscribed");
            }

            subscriptionRepository.save(
                    Subscription.builder()
                            .client(client)
                            .author(author)
                            .build()
            );
        }

        if (category != null && !category.isBlank()) {
            category = category.trim();

            if (subscriptionRepository.existsByClientIdAndCategory(
                    client.getId(), category)) {
                throw new UserAlreadySubscibedException("Category already subscribed");
            }

            subscriptionRepository.save(
                    Subscription.builder()
                            .client(client)
                            .category(category)
                            .build()
            );
        }
    }

    @Transactional
    public void removeSubscription(SubscriptionDTO dto) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));

        isActivated(client);

        String author = dto.getAuthor();
        String category = dto.getCategory();

        if ((author == null || author.isBlank()) &&
                (category == null || category.isBlank())) {
            throw new IllegalArgumentException(
                    "Author or category must be provided");
        }

        if (author != null && !author.isBlank()) {
            subscriptionRepository.deleteByClientIdAndAuthor(
                    client.getId(), author.trim());
        }

        if (category != null && !category.isBlank()) {
            subscriptionRepository.deleteByClientIdAndCategory(
                    client.getId(), category.trim());
        }
    }

    public void isActivated(Client client) {
        if (!client.getActivated()) {
            throw new UserNotActivatedException("User adress was not confirmed!");
        }
    }
}
