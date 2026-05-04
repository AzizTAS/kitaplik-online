package com.kitaplik.libraryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitaplik.libraryservice.client.BookServiceClient;
import com.kitaplik.libraryservice.dto.AddBookRequest;
import com.kitaplik.libraryservice.dto.BookDto;
import com.kitaplik.libraryservice.dto.BookIdDto;
import com.kitaplik.libraryservice.dto.LibraryDto;
import com.kitaplik.libraryservice.exception.LibraryNotFoundException;
import com.kitaplik.libraryservice.model.Library;
import com.kitaplik.libraryservice.repository.LibraryRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LibraryServiceTest {

    private LibraryService libraryService;
    private LibraryRepository libraryRepository;
    private BookServiceClient bookServiceClient;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        libraryRepository = Mockito.mock(LibraryRepository.class);
        bookServiceClient = Mockito.mock(BookServiceClient.class);

        libraryService = new LibraryService(libraryRepository, bookServiceClient);

    }

    @DisplayName("should Return LibraryDto with Detailed BookList With BookDto And Updated LibraryId when the parameter of the getAllBooksInLibraryById LibraryId Exist And Library UserBook List Size More Than Two")
    @Test
    void shouldReturnDetailedBookListWithBookDtoAndUpdatedLibraryId_whenLibraryIdExistAndLibraryUserBookListSizeMoreThan2() {
        String id = "libraryId";
        List<String> userBook = Arrays.asList("book1", "book2", "book3");
        Library library = new Library(id, userBook);
        BookDto book1 = new BookDto(new BookIdDto("book1", "isbn"), "title1", 2021, "author1", "press1");
        BookDto book2 = new BookDto(new BookIdDto("book2", "isbn"), "title1", 2021, "author1", "press1");
        BookDto book3 = new BookDto(new BookIdDto("book3", "isbn"), "title1", 2021, "author1", "press1");

        List<BookDto> bookDtoList = Arrays.asList(book1, book2, book3);
        LibraryDto expectedResult = new LibraryDto(id, bookDtoList);

        Mockito.when(libraryRepository.findById(id)).thenReturn(Optional.of(library));
        Mockito.when(bookServiceClient.getBookById("book1")).thenReturn(ResponseEntity.ok(book1));
        Mockito.when(bookServiceClient.getBookById("book2")).thenReturn(ResponseEntity.ok(book2));
        Mockito.when(bookServiceClient.getBookById("book3")).thenReturn(ResponseEntity.ok(book3));

        LibraryDto result = libraryService.getAllBooksInLibraryById(id);


        assertEquals(expectedResult, result);


        Mockito.verify(libraryRepository).findById(id);
        Mockito.verify(bookServiceClient).getBookById("book1");
        Mockito.verify(bookServiceClient).getBookById("book2");
        Mockito.verify(bookServiceClient).getBookById("book3");
        Mockito.verify(bookServiceClient, new Times(3)).getBookById(Mockito.any(String.class));
    }

    @DisplayName("should Return LibraryDto with Detailed BookList With BookDto when the parameter of the getAllBooksInLibraryById LibraryId Exist")
    @Test
    void shouldReturnDetailedBookListWithBookDto_whenLibraryIdExist() {

        String id = "libraryId";
        List<String> userBook = Arrays.asList("book1", "book2");
        Library library = new Library(id, userBook);
        BookDto book1 = new BookDto(new BookIdDto("book1", "isbn"), "title1", 2021, "author1", "press1");
        BookDto book2 = new BookDto(new BookIdDto("book2", "isbn"), "title1", 2021, "author1", "press1");

        List<BookDto> bookDtoList = Arrays.asList(book1, book2);
        LibraryDto expectedResult = new LibraryDto(id, bookDtoList);


        Mockito.when(libraryRepository.findById(id)).thenReturn(Optional.of(library));
        Mockito.when(bookServiceClient.getBookById("book1")).thenReturn(ResponseEntity.ok(book1));
        Mockito.when(bookServiceClient.getBookById("book2")).thenReturn(ResponseEntity.ok(book2));


        LibraryDto result = libraryService.getAllBooksInLibraryById(id);


        assertEquals(expectedResult, result);


        Mockito.verify(libraryRepository).findById(id);
        Mockito.verify(bookServiceClient).getBookById("book1");
        Mockito.verify(bookServiceClient).getBookById("book2");
        Mockito.verify(bookServiceClient, new Times(2)).getBookById(Mockito.any(String.class));
    }

    @DisplayName("should Throw LibraryNotFoundException when the parameter of the getAllBooksInLibraryById LibraryId Does Not Exist")
    @Test
    void shouldThrowLibraryNotFoundException_whenLibraryIdDoesNotExist() {

        String id = "libraryId";


        Mockito.when(libraryRepository.findById(id)).thenReturn(Optional.empty());


        org.assertj.core.api.Assertions.assertThatThrownBy(() -> libraryService.getAllBooksInLibraryById(id))
                .isInstanceOf(LibraryNotFoundException.class)
                .hasMessageContaining("Library could not found by id: " + id);


        Mockito.verify(libraryRepository).findById(id);
        Mockito.verifyNoInteractions(bookServiceClient);
    }

    @DisplayName("should Save New Library And Return LibraryDto With New Id When createLibrary Called")
    @Test
    void shouldSaveNewLibraryAndReturnLibraryDtoWithNewId_whenCreateLibraryCalled() {
        Library savedLibrary = new Library("new-library-id", new ArrayList<>());
        Mockito.when(libraryRepository.save(Mockito.any(Library.class))).thenReturn(savedLibrary);

        LibraryDto result = libraryService.createLibrary();

        assertEquals("new-library-id", result.getId());
        Mockito.verify(libraryRepository).save(Mockito.any(Library.class));
    }

    @DisplayName("should Add BookId To Library When addBookToLibrary Called With Valid Request")
    @Test
    void shouldAddBookIdToLibrary_whenAddBookToLibraryCalledWithValidRequest() {
        String libraryId = "library-id";
        String isbn = "some-isbn";
        String bookId = "book-id";
        AddBookRequest request = new AddBookRequest(libraryId, isbn);
        Library library = new Library(libraryId, new ArrayList<>());
        BookIdDto bookIdDto = new BookIdDto(bookId, isbn);

        Mockito.when(bookServiceClient.getBookByIsbn(isbn)).thenReturn(ResponseEntity.ok(bookIdDto));
        Mockito.when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        libraryService.addBookToLibrary(request);

        Mockito.verify(bookServiceClient).getBookByIsbn(isbn);
        Mockito.verify(libraryRepository).findById(libraryId);
        Mockito.verify(libraryRepository).save(library);
    }

    @DisplayName("should Throw LibraryNotFoundException When addBookToLibrary Called With Non-Existing Library Id")
    @Test
    void shouldThrowLibraryNotFoundException_whenAddBookToLibraryCalledWithNonExistingLibraryId() {
        String libraryId = "non-existing-id";
        String isbn = "some-isbn";
        AddBookRequest request = new AddBookRequest(libraryId, isbn);
        BookIdDto bookIdDto = new BookIdDto("book-id", isbn);

        Mockito.when(bookServiceClient.getBookByIsbn(isbn)).thenReturn(ResponseEntity.ok(bookIdDto));
        Mockito.when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> libraryService.addBookToLibrary(request))
                .isInstanceOf(LibraryNotFoundException.class)
                .hasMessageContaining("Library could not found by id: " + libraryId);

        Mockito.verify(libraryRepository).findById(libraryId);
        Mockito.verify(libraryRepository, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("should Return All Library Ids When getAllLibraries Called")
    @Test
    void shouldReturnAllLibraryIds_whenGetAllLibrariesCalled() {
        Library library1 = new Library("id-1", new ArrayList<>());
        Library library2 = new Library("id-2", new ArrayList<>());
        Mockito.when(libraryRepository.findAll()).thenReturn(Arrays.asList(library1, library2));

        List<String> result = libraryService.getAllLibraries();

        assertEquals(Arrays.asList("id-1", "id-2"), result);
        Mockito.verify(libraryRepository).findAll();
    }

    @AfterEach
    void tearDown() {

    }
}
