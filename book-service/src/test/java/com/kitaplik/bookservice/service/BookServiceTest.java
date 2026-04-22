package com.kitaplik.bookservice.service;

import com.kitaplik.bookservice.dto.BookDto;
import com.kitaplik.bookservice.dto.BookIdDto;
import com.kitaplik.bookservice.exception.BookNotFoundException;
import com.kitaplik.bookservice.model.Book;
import com.kitaplik.bookservice.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookServiceTest {

    private BookService bookService;
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = Mockito.mock(BookRepository.class);
        bookService = new BookService(bookRepository);
    }

    @DisplayName("should Return BookDto when addBook Called With Valid BookDto")
    @Test
    void shouldReturnBookDto_whenAddBookCalledWithValidBookDto() {
        BookIdDto bookIdDto = new BookIdDto("", "isbn-123");
        BookDto bookDto = new BookDto(bookIdDto, "Test Title", 2021, "Test Author", "Test Press");
        BookDto expectedResult = new BookDto(null, "Test Title", 2021, "Test Author", "Test Press");

        BookDto result = bookService.addBook(bookDto);

        assertEquals(expectedResult, result);
        Mockito.verify(bookRepository).save(Mockito.any(Book.class));
    }

    @DisplayName("should Throw IllegalArgumentException when addBook Called With Null ISBN")
    @Test
    void shouldThrowIllegalArgumentException_whenAddBookCalledWithNullIsbn() {
        BookDto bookDto = new BookDto(null, "Test Title", 2021, "Test Author", "Test Press");

        assertThatThrownBy(() -> bookService.addBook(bookDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN is required");

        Mockito.verifyNoInteractions(bookRepository);
    }

    @DisplayName("should Return BookIdDto when findByIsbn Called With Existing ISBN")
    @Test
    void shouldReturnBookIdDto_whenFindByIsbnCalledWithExistingIsbn() {
        String isbn = "isbn-123";
        Book book = new Book("book-id", "Title", 2021, "Author", "Press", isbn);
        BookIdDto expectedResult = new BookIdDto("book-id", isbn);

        Mockito.when(bookRepository.getBookByIsbn(isbn)).thenReturn(Optional.of(book));

        BookIdDto result = bookService.findByIsbn(isbn);

        assertEquals(expectedResult, result);
        Mockito.verify(bookRepository).getBookByIsbn(isbn);
    }

    @DisplayName("should Throw BookNotFoundException when findByIsbn Called With Non-Existing ISBN")
    @Test
    void shouldThrowBookNotFoundException_whenFindByIsbnCalledWithNonExistingIsbn() {
        String isbn = "non-existing-isbn";

        Mockito.when(bookRepository.getBookByIsbn(isbn)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findByIsbn(isbn))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book could not found by isbn: " + isbn);

        Mockito.verify(bookRepository).getBookByIsbn(isbn);
    }

    @DisplayName("should Return BookDto when findBookDetailsById Called With Existing ID")
    @Test
    void shouldReturnBookDto_whenFindBookDetailsByIdCalledWithExistingId() {
        String id = "book-id";
        String isbn = "isbn-123";
        Book book = new Book(id, "Title", 2021, "Author", "Press", isbn);
        BookDto expectedResult = BookDto.convert(book);

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        BookDto result = bookService.findBookDetailsById(id);

        assertEquals(expectedResult, result);
        Mockito.verify(bookRepository).findById(id);
    }

    @DisplayName("should Throw BookNotFoundException when findBookDetailsById Called With Non-Existing ID")
    @Test
    void shouldThrowBookNotFoundException_whenFindBookDetailsByIdCalledWithNonExistingId() {
        String id = "non-existing-id";

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findBookDetailsById(id))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book could not found by id: " + id);

        Mockito.verify(bookRepository).findById(id);
    }
}
