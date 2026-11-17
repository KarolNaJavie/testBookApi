package com.example.library.services;

import com.example.library.model.Book;
import com.example.library.model.BookDTO;
import com.example.library.model.Client;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ClientRepository;
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

    @InjectMocks
    private BookService bookService;

    @Test
    public void testAddBookHappyPath(){
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
        when(clientRepository.findAll()).thenReturn(List.of());

        BookDTO result = bookService.addBook(bookDTO);

        assertNotNull(result);
        assertEquals("Stephen King", result.getAuthor());
        assertEquals("Mroczna Wieża", result.getTitle());
        assertEquals("Fantasy", result.getCategory());

        verify(bookRepository).save(any(Book.class));
        verify(clientRepository).findAll();
        verifyNoInteractions(mailService);
    }

    @Test
    public void testAddBookResultsInSendingNotification(){
        Client client = mock(Client.class);
        var subscription = mock(com.example.library.model.Subscription.class);
        BookDTO bookDTO = BookDTO.builder()
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        when(client.getSubscription()).thenReturn(subscription);
        when(client.getEmail()).thenReturn("test@gmail.com");
        when(subscription.getSubscribedAuthors())
                .thenReturn(List.of("Stephen King"));

        Book savedBook = Book.builder()
                .id(1L)
                .author("Stephen King")
                .title("Mroczna Wieża")
                .category("Fantasy")
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(clientRepository.findAll()).thenReturn(List.of(client));

        bookService.addBook(bookDTO);

        verify(mailService).sendConfirmation(
                eq("test@gmail.com"),
                contains("Mroczna Wieża")
        );
    }
}
