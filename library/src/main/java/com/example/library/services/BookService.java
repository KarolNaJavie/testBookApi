package com.example.library.services;

import com.example.library.model.*;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ClientRepository;
import com.example.library.repository.NotificationRepository;
import com.example.library.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final MailService mailService;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;


    public BookDTO addBook(BookDTO bookDTO) {

        Book book = Book.builder()
                .author(bookDTO.getAuthor())
                .title(bookDTO.getTitle())
                .category(bookDTO.getCategory())
                .build();

        Book added = bookRepository.save(book);

        List<Subscription> subscriptions =
                subscriptionRepository.findMatchingSubscriptions(
                        book.getAuthor(),
                        book.getCategory()
                );

        subscriptions.stream()
                .map(Subscription::getClient)
                .distinct()
                .forEach(client -> {
                    Notification notification = Notification.builder()
                            .client(client)
                            .book(added)
                            .build();

                    if (!notificationRepository.existsByClientIdAndBookId(
                            client.getId(), added.getId())) {
                        notificationRepository.save(notification);
                    }
                });

        return BookDTO.builder()
                .title(added.getTitle())
                .author(added.getAuthor())
                .category(added.getCategory())
                .build();
    }
}




