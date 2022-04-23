package com.santanarscs.libraryapi.api.resources;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santanarscs.libraryapi.api.dto.BookDTO;
import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.service.BookService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService service;

	@Test
	@DisplayName("Should be able to create a book.")
	void createBookTest() throws Exception {

		BookDTO dto = createNewBook();
		Book savedBook = Book.builder().id(10L).author("Jhon Doe").title("My Book").isbn("001").build();

		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

		String json = new ObjectMapper().writeValueAsString(dto);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc
				.perform(request)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("id").isNotEmpty())
				.andExpect(jsonPath("title").value(dto.getTitle()))
				.andExpect(jsonPath("author").value(dto.getAuthor()))
				.andExpect(jsonPath("isbn").value(dto.getIsbn()));

	}

	@Test
	@DisplayName("Should not be able to create a book with invalid data.")
	void createInvalidBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(new BookDTO());
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(3)));
	}

	@Test
	@DisplayName("Should not be able to create a book with duplicate ISBN")
	void createBookWithDuplicateIsbn() throws Exception {

		BookDTO dto = createNewBook();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(service.save(Mockito.any(Book.class)))
				.willThrow(new BusinessException("Alread exists ISBN registred"));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(("Alread exists ISBN registred")));

	}

	@Test
	@DisplayName("Should be able to return a book")
	void getBookDetailsTest() throws Exception {
		Long id = 1L;
		Book book = Book.builder().id(id).author("Jhon Doe").title("My Book").isbn("001").build();
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(book.getTitle()))
				.andExpect(jsonPath("author").value(book.getAuthor()))
				.andExpect(jsonPath("isbn").value(book.getIsbn()));

	}

	@Test
	@DisplayName("Should be able to return not found with book doesnt exists")
	void bookNotFoundTest() throws Exception {
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + 1L))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should be able to delete a book")
	void deleteBookTest() throws Exception {

		BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1L).build()));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + 1L));

		mvc.perform(request)
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Should not be able to delete a book with not exists")
	void notDeleteBookTest() throws Exception {

		BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + 1L));

		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should be able to update a book")
	void updateBookTest() throws Exception {
		Long id = 1L;
		String json = new ObjectMapper().writeValueAsString(createNewBook());

		Book updatingBook = Book.builder().id(1L).title("Other Book").author("Other Author").isbn("0002").build();
		BDDMockito.given(service.getById(anyLong()))
				.willReturn(Optional.of(updatingBook));

		Book updatedBook = Book.builder().id(id).author("Jhon Doe").title("My Book").isbn("0002").build();
		BDDMockito.given(service.update(updatingBook))
				.willReturn(updatedBook);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + id))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBook().getTitle()))
				.andExpect(jsonPath("author").value(createNewBook().getAuthor()))
				.andExpect(jsonPath("isbn").value("0002"));

	}

	@Test
	@DisplayName("Should not be able to update a book")
	void noUpdateBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(createNewBook());

		BDDMockito.given(service.getById(anyLong()))
				.willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1L))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("should be able to return a list filtred")
	void findBooksTest() throws Exception {
		Long id = 1L;
		Book book = Book.builder().id(id).title(createNewBook().getTitle()).author(createNewBook().getAuthor())
				.isbn(createNewBook().getIsbn()).build();
		BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1))
				.andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}

	private BookDTO createNewBook() {
		return BookDTO.builder().author("Jhon Doe").title("My Book").isbn("001").build();
	}
}
