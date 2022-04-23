package com.santanarscs.libraryapi.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.santanarscs.libraryapi.api.dto.LoanFilterDTO;
import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Loan;
import com.santanarscs.libraryapi.model.repository.LoanRepository;
import com.santanarscs.libraryapi.service.LoanService;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;

@Service
public class LoanServiceImpl implements LoanService {

  private LoanRepository repository;

  public LoanServiceImpl(LoanRepository repository) {
    this.repository = repository;
  }

  @Override
  public Loan save(Loan loan) {
    if (repository.existsByBookAndNotReturned(loan.getBook())) {
      throw new BusinessException("Book already loaned");
    }
    return repository.save(loan);
  }

  @Override
  public Optional<Loan> getById(Long id) {
    return repository.findById(id);
  }

  @Override
  public Loan update(Loan loan) {
    return repository.save(loan);
  }

  @Override
  public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
    return repository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
  }

  @Override
  public List<Loan> getAllLateLoans() {
    final Integer loanDays = 4;
    LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
    return repository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
  }

}
