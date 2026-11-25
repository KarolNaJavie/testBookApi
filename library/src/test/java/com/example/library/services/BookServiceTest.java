
package com.example.library.services;

import com.example.library.model.*;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ClientRepository;
import com.example.library.repository.NotificationRepository;
import com.example.library.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MailService mailService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    public void testAddBookHappyPath() {
        Book savedBook = Book.builder()
                .id(1L)
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        when(subscriptionRepository.findMatchingSubscriptions(
                "Stephen King", "Fantasy"
        )).thenReturn(List.of());

        BookDTO result = bookService.addBook(bookDTO);

        assertNotNull(result);
        assertEquals("Stephen King", result.getAuthor());
        assertEquals("Mroczna Wieża", result.getTitle());
        assertEquals("Fantasy", result.getCategory());

        verify(bookRepository).save(any(Book.class));

        verify(subscriptionRepository).findMatchingSubscriptions(
                "Stephen King", "Fantasy"
        );

        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(mailService);
        verifyNoInteractions(clientRepository);
    }

    @Test
    public void testAddBookResultsInSendingNotification() {
        Client client = mock(Client.class);
        Subscription subscription = mock(Subscription.class);

        BookDTO bookDTO = BookDTO.builder()
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        Book savedBook = Book.builder()
                .id(1L)
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        when(client.getId()).thenReturn(1L);
        when(subscription.getClient()).thenReturn(client);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        when(subscriptionRepository.findMatchingSubscriptions(
                "Stephen King", "Fantasy"
        )).thenReturn(List.of(subscription));

        when(notificationRepository.existsByClientIdAndBookId(1L, 1L))
                .thenReturn(false);

        bookService.addBook(bookDTO);

        verify(bookRepository).save(any(Book.class));

        verify(subscriptionRepository).findMatchingSubscriptions(
                "Stephen King", "Fantasy"
        );

        verify(notificationRepository).existsByClientIdAndBookId(1L, 1L);

        verify(notificationRepository).save(any(Notification.class));

        verifyNoInteractions(mailService);
        verifyNoInteractions(clientRepository);
    }
}
