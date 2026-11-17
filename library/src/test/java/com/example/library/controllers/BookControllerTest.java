package com.example.library.controllers;

import com.example.library.model.BookDTO;
import com.example.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    void testAddBook() {

        BookDTO bookDTO = BookDTO.builder()
                .title("Mroczna Wieża")
                .author("Stephen King")
                .category("Fantasy")
                .build();

        when(bookService.addBook(bookDTO)).thenReturn(bookDTO);

        BookDTO result = bookController.addBook(bookDTO);

        assertEquals("Mroczna Wieża", result.getTitle());
        assertEquals("Stephen King", result.getAuthor());
        assertEquals("Fantasy", result.getCategory());

        verify(bookService).addBook(bookDTO);
    }
}
