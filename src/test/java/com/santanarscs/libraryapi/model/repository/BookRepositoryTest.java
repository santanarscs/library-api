package com.santanarscs.libraryapi.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import com.santanarscs.libraryapi.model.entity.Book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

  @Autowired
  TestEntityManager entityManager;

  @Autowired
  BookRepository repository;

  @Test
  @DisplayName("Should be able to return true if exists a book in database")
  void returnTrueWhenIsbnExists() {
    String isbn = "001";
    Book book = createNewBook(isbn);
    entityManager.persist(book);
    boolean exists = repository.existsByIsbn(isbn);

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Should be able to return false if not exists a book in database")
  void returnFalseWhenIsbnExists() {
    String isbn = "001";
    boolean exists = repository.existsByIsbn(isbn);

    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Should be able to return a book")
  void findByIdTest() {
    Book book = createNewBook("123");
    entityManager.persist(book);

    Optional<Book> foundBook = repository.findById(book.getId());

    assertThat(foundBook.isPresent()).isTrue();

  }

  @Test
  @DisplayName("Should be able to save a book")
  void saveBookTest() {
    Book book = createNewBook("001");

    Book savedBook = repository.save(book);

    assertThat(savedBook.getId()).isNotNull();
  }

  @Test
  @DisplayName("Should be able to delete a book")
  void deleteBookTest(){
    Book book = createNewBook("001");
    entityManager.persist(book);

    Book foundBook = entityManager.find(Book.class, book.getId());

    repository.delete(foundBook);

    Book deletedBook = entityManager.find(Book.class, book.getId());

    assertThat(deletedBook).isNull();

  }

  private Book createNewBook(String isbn) {
    return Book.builder().title("My Book").author("Jhon Doe").isbn(isbn).build();
  }
}
