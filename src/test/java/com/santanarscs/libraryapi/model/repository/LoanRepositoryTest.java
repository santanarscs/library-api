package com.santanarscs.libraryapi.model.repository;

import java.time.LocalDate;
import java.util.List;

import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.model.entity.Loan;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
  
  @Autowired
  TestEntityManager entityManager;

  @Autowired
  LoanRepository repository;

  @Test
  @DisplayName("Should be able verify if exists load to book")
  void existsByBookAndNotReturnedTest() {
    
    Book book = createNewBook(); 
    entityManager.persist(book);

    Loan loan = createNewLoan(book);
    entityManager.persist(loan);

    boolean exists = repository.existsByBookAndNotReturned(book);

    Assertions.assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Should be able to find loan by customer or isbn book")
  void findByBookIsbnOrCustomerTest() {
    Book book = createNewBook(); 
    entityManager.persist(book);

    Loan loan = createNewLoan(book);
    entityManager.persist(loan);

    Page<Loan> result = repository.findByBookIsbnOrCustomer("001", "Jhon", PageRequest.of(0, 10));
    Assertions.assertThat(result.getContent()).hasSize(1);
    Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);

  }

  @Test
  @DisplayName("shoud be able to return late loans")
  void findByLoanDateLessThanAndNotReturnedTest() {
    Book book = createNewBook(); 
    entityManager.persist(book);

    Loan loan = createNewLoan(book);
    loan.setLoanDate(LocalDate.now().minusDays(5));
    entityManager.persist(loan);

    List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
    Assertions.assertThat(result).hasSize(1).contains(loan);
  }

  @Test
  @DisplayName("shoud not be able to return late loans")
  void notFindByLoanDateLessThanAndNotReturnedTest() {
    Book book = createNewBook(); 
    entityManager.persist(book);

    Loan loan = createNewLoan(book);
    loan.setLoanDate(LocalDate.now());
    entityManager.persist(loan);

    List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
    Assertions.assertThat(result).hasSize(0);
  }



  private Loan createNewLoan(Book book) {
    return Loan.builder().book(book).customer("Jhon").customerEmail("jhondoe@example.com").loanDate(LocalDate.now()).build();
  }

  private Book createNewBook() {
    return Book.builder().title("My Book").isbn("001").author("Jhon Doe").build();
  }
}
