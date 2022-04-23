package com.santanarscs.libraryapi.service;

import java.util.List;
import java.util.Optional;

import com.santanarscs.libraryapi.api.dto.LoanFilterDTO;
import com.santanarscs.libraryapi.model.entity.Loan;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface LoanService {
  Loan save(Loan loan);

  Optional<Loan> getById(Long id);

  Loan update(Loan loan);

  Page<Loan> find(LoanFilterDTO filter, Pageable pageable);

  List<Loan> getAllLateLoans();
}
