package com.santanarscs.libraryapi.service;

import java.util.Optional;

import com.santanarscs.libraryapi.model.entity.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
  Book save(Book book);

  Optional<Book> getById(Long id);

  void delete(Book book);

  Book update(Book book);

  Page<Book> find(Book filter, Pageable pageRequest);

  Optional<Book> getBookByIsbn(String isbn);
}
