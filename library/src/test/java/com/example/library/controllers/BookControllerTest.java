package com.example.library.controllers;

import com.example.library.config.CustomUserDetailsService;
import com.example.library.config.SecurityConfig;
import com.example.library.model.BookDTO;
import com.example.library.services.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private BookService bookService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAddBookWhenAdmin() throws Exception {

        BookDTO bookDTO = new BookDTO();

        when(bookService.addBook(any(BookDTO.class)))
                .thenReturn(bookDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/book/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isOk());

        verify(bookService, times(1))
                .addBook(any(BookDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUser() throws Exception {

        BookDTO bookDTO = new BookDTO();

        mockMvc.perform(MockMvcRequestBuilders.post("/book/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {

        BookDTO bookDTO = new BookDTO();

        mockMvc.perform(MockMvcRequestBuilders.post("/book/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }
}
