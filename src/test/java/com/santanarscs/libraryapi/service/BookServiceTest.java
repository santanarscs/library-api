package com.santanarscs.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.model.repository.BookRepository;
import com.santanarscs.libraryapi.service.impl.BookServiceImpl;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

  BookService service;

  @MockBean
  BookRepository repository;

  @BeforeEach
  public void setup() {
    this.service = new BookServiceImpl(repository);
  }

  @Test
  @DisplayName("Should be able to save a book")
  void saveBookTest() {
    Book book = createValidBook();
    Book expectedBook = Book.builder().id(1L).isbn("001").author("Jhon Doe").title("My Book").build();
    Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
    Mockito.when(repository.save(book)).thenReturn(expectedBook);
    Book savedBook = service.save(book);

    assertThat(savedBook.getId()).isNotNull();
    assertThat(savedBook.getIsbn()).isEqualTo("001");
    assertThat(savedBook.getAuthor()).isEqualTo("Jhon Doe");
    assertThat(savedBook.getTitle()).isEqualTo("My Book");
  }

  @Test
  @DisplayName("Should be able to throw buissiness exception when duplicate isbn")
  void shouldNotSaveABookWithDuplicateISBN() {
    Book book = createValidBook();
    Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
    Throwable exception = Assertions.catchThrowable(() -> service.save(book));

    assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Alread exists ISBN registred");
    Mockito.verify(repository, Mockito.never()).save(book);
  }

  @Test
  @DisplayName("Should be able to return a book ")
  void getByIdTest() {
    Long id = 1L;

    Book book = createValidBook();
    book.setId(id);
    Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

    Optional<Book> foundBook = service.getById(id);

    assertThat(foundBook.isPresent()).isTrue();
    assertThat(foundBook.get().getId()).isEqualTo(id);
    assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
    assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
    assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());

  }

  @Test
  @DisplayName("Should not be able to return a book ")
  void notGetByIdTest() {

    Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

    Optional<Book> book = service.getById(1L);

    assertThat(book.isPresent()).isFalse();

  }

  @Test
  @DisplayName("Should be able to delete a book")
  void deleteBookTest() {

    Book book = Book.builder().id(1L).build();

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

    Mockito.verify(repository, Mockito.times(1)).delete(book);

  }

  @Test
  @DisplayName("Should not be able to delete a book")
  void notDeleteBookTest() {

    Book book = new Book();

    org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

    Mockito.verify(repository, Mockito.never()).delete(book);

  }

  @Test
  @DisplayName("Should not be able to update a book")
  void notUpdateBookTest() {

    Book book = new Book();

    org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

    Mockito.verify(repository, Mockito.never()).save(book);

  }

  @Test
  @DisplayName("Should be able to update a book")
  void updateBookTest() {
    Long id = 1L;
    Book updatingBook = Book.builder().id(id).build();

    Book updatedBook = createValidBook();
    updatedBook.setId(id);

    Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

    Book book = service.update(updatingBook);

    assertThat(book.getId()).isNotNull();
    assertThat(book.getIsbn()).isEqualTo(createValidBook().getIsbn());
    assertThat(book.getAuthor()).isEqualTo(createValidBook().getAuthor());
    assertThat(book.getTitle()).isEqualTo(createValidBook().getTitle());
  }

  @Test
  @DisplayName("Should be able to filter books")
  void findBookTest() {
    Book book = createValidBook();
    PageRequest pageRequest = PageRequest.of(0, 10);
    List<Book> list = Arrays.asList(book);
    Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);

    Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

    Page<Book> result = service.find(book, pageRequest);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).isEqualTo(list);
    assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    assertThat(result.getPageable().getPageSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("Should be able to return a book by isbn")
  void getBookByIsbnTest() {
    String isbn = "001";

    Mockito.when(repository.findByIsbn(isbn))
        .thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));
    
    Optional<Book> book = service.getBookByIsbn(isbn);

    assertThat(book.isPresent()).isTrue();
    assertThat(book.get().getId()).isEqualTo(1L);
    assertThat(book.get().getIsbn()).isEqualTo(isbn);

    Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn);
  }

  private Book createValidBook() {
    return Book.builder().isbn("001").author("Jhon Doe").title("My Book").build();
  }

}
