package com.kitaplik.bookservice.service;

import com.kitaplik.bookservice.dto.BookDto;
import com.kitaplik.bookservice.dto.BookIdDto;
import com.kitaplik.bookservice.exception.BookNotFoundException;
import com.kitaplik.bookservice.model.Book;
import com.kitaplik.bookservice.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public BookDto addBook(BookDto bookDto) {
        if (bookDto.getId() == null || bookDto.getId().getIsbn() == null || bookDto.getId().getIsbn().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required");
        }

        Book book = new Book(null, bookDto.getTitle(), bookDto.getBookYear(), bookDto.getAuthor(), bookDto.getPressName(), bookDto.getId().getIsbn());

        repository.save(book);

        return BookDto.convert(book);
    }

    public List<BookDto> getAllBooks(){
        return repository.findAll().
                stream()
                .map(BookDto::convert)
                .collect(Collectors.toList());
    }

    public BookIdDto findByIsbn(String isbn){
        return repository.getBookByIsbn(isbn)
                .map(book -> new BookIdDto(book.getId(),book.getIsbn()))
                .orElseThrow(()->new BookNotFoundException("Book could not found by isbn: "+isbn));
    }

    public BookDto findBookDetailsById(String id){
        return repository.findById(id)
                .map(BookDto::convert)
                .orElseThrow(()->new BookNotFoundException("Book could not found by id: "+id));
    }

}
