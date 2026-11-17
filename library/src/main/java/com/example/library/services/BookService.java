package com.example.library.services;

import com.example.library.model.Book;
import com.example.library.model.BookDTO;
import com.example.library.model.Client;
import com.example.library.repository.BookRepository;
import com.example.library.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final MailService mailService;

    public BookDTO addBook(BookDTO bookDTO) {

        Book book = Book.builder()
                .author(bookDTO.getAuthor())
                .title(bookDTO.getTitle())
                .category(bookDTO.getCategory())
                .build();
        Book added = bookRepository.save(book);

        List<Client> subscribers = clientRepository.findAll().stream()
                .filter(client -> client.getSubscription() != null)
                .filter(client ->
                        (client.getSubscription().getSubscribedAuthors() != null &&
                                client.getSubscription().getSubscribedAuthors()
                                        .contains(book.getAuthor()))
                                ||
                                (client.getSubscription().getSubscribedCategories() != null &&
                                        client.getSubscription().getSubscribedCategories()
                                                .contains(book.getCategory()))
                )
                .toList();
        String text = "Zostala dodana ksiazka ktora moze cie zainteresowac: " + book.getTitle() + " z gatunku: " + book.getCategory();
        subscribers.forEach(client ->
                mailService.sendConfirmation(client.getEmail(), text)
        );

        return BookDTO.builder()
                .title(added.getTitle())
                .author(added.getAuthor())
                .category(added.getCategory())
                .build();
    }
}
