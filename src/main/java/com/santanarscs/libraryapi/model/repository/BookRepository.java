package com.santanarscs.libraryapi.model.repository;

import java.util.Optional;

import com.santanarscs.libraryapi.model.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

  boolean existsByIsbn(String isbn);

  Optional<Book> findByIsbn(String isbn);

}
