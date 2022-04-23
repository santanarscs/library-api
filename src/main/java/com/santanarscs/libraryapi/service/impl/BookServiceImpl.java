package com.santanarscs.libraryapi.service.impl;

import java.util.Optional;

import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.model.repository.BookRepository;
import com.santanarscs.libraryapi.service.BookService;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

  private BookRepository repository;

  public BookServiceImpl(BookRepository repository) {
    this.repository = repository;
  }

  @Override
  public Book save(Book book) {
    if (repository.existsByIsbn(book.getIsbn())) {
      throw new BusinessException("Alread exists ISBN registred");
    }
    return repository.save(book);
  }

  @Override
  public Optional<Book> getById(Long id) {
    return repository.findById(id);
  }

  @Override
  public void delete(Book book) {
    if (book == null || book.getId() == null) {
      throw new IllegalArgumentException("Book id cant be null");
    }
    this.repository.delete(book);

  }

  @Override
  public Book update(Book book) {
    if (book == null || book.getId() == null) {
      throw new IllegalArgumentException("Book id cant be null");
    }
    return this.repository.save(book);

  }

  @Override
  public Page<Book> find(Book filter, Pageable pageRequest) {
    Example<Book> example = Example.of(filter,
        ExampleMatcher
            .matching()
            .withIgnoreCase()
            .withIgnoreNullValues()
            .withStringMatcher(StringMatcher.CONTAINING));

    return repository.findAll(example, pageRequest);
  }

  @Override
  public Optional<Book> getBookByIsbn(String isbn) {
    return repository.findByIsbn(isbn);
  }

}
