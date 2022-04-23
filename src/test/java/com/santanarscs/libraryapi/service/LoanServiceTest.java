package com.santanarscs.libraryapi.service;

import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.santanarscs.libraryapi.api.dto.LoanFilterDTO;
import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.model.entity.Loan;
import com.santanarscs.libraryapi.model.repository.LoanRepository;
import com.santanarscs.libraryapi.service.impl.LoanServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

  LoanService service;

  @MockBean
  LoanRepository repository;

  @BeforeEach
  public void setup() {
    this.service = new LoanServiceImpl(repository);
  }

  @Test
  @DisplayName("Should be able to create a loan")
  void saveLoanTest() {
    Loan savingLoan = createLoan();
    Loan savedLoan = createLoan();
    Mockito.when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(false);
    Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);
    Loan loan = service.save(savingLoan);

    Assertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
    Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
    Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
    Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
  }

  @Test
  @DisplayName("Should not be able to create a loan if already loan book")
  void notSaveLoanTest() {
    Loan savingLoan = createLoan();
    Mockito.when(repository.existsByBookAndNotReturned(savingLoan.getBook())).thenReturn(true);
    Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));

    Assertions.assertThat(exception)
        .isInstanceOf(BusinessException.class)
        .hasMessage("Book already loaned");

    Mockito.verify(repository, never()).save(savingLoan);

  }

  @Test
  @DisplayName("Should be able to return a loan")
  void getLoanDetailsTest() {
    Long id = 1L;
    Loan loan = createLoan();
    loan.setId(id);

    Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

    Optional<Loan> result = service.getById(id);

    Assertions.assertThat(result.isPresent()).isTrue();
    Assertions.assertThat(result.get().getId()).isEqualTo(id);
    Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
    Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
    Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

    Mockito.verify(repository).findById(id);
  }

  @Test
  @DisplayName("Should be able to update a loan")
  void updateLoanTest() {
    Loan loan = createLoan();
    loan.setId(1L);
    loan.setReturned(true);

    Mockito.when(repository.save(loan)).thenReturn(loan);

    Loan updatedLoan = service.update(loan);

    Assertions.assertThat(updatedLoan.getReturned()).isTrue();
    Mockito.verify(repository).save(loan);

  }

  @Test
  @DisplayName("Should be able to filter loans")
  void findLoanTest() {
    LoanFilterDTO dto = LoanFilterDTO.builder().customer("Jhon").isbn("001").build();
    Loan loan = createLoan();
    loan.setId(1L);
    
    PageRequest pageRequest = PageRequest.of(0, 10);
    List<Loan> list = Arrays.asList(loan);
    Page<Loan> page = new PageImpl<Loan>(list, pageRequest, 1);

    Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class) )).thenReturn(page);

    Page<Loan> result = service.find(dto, pageRequest);

    Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
    Assertions.assertThat(result.getContent()).isEqualTo(list);
    Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
    Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
  }

  private Loan createLoan() {
    Book book = Book.builder().id(1L).build();
    return Loan.builder().book(book).customer("Jhon").loanDate(LocalDate.now()).build();
  }
}
