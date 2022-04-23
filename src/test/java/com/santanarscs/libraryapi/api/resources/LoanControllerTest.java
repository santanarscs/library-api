package com.santanarscs.libraryapi.api.resources;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santanarscs.libraryapi.api.dto.LoanDTO;
import com.santanarscs.libraryapi.api.dto.LoanFilterDTO;
import com.santanarscs.libraryapi.api.dto.ReturnedLoanDTO;
import com.santanarscs.libraryapi.exception.BusinessException;
import com.santanarscs.libraryapi.model.entity.Book;
import com.santanarscs.libraryapi.model.entity.Loan;
import com.santanarscs.libraryapi.service.BookService;
import com.santanarscs.libraryapi.service.LoanService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

	static final String LOAN_API = "/api/loans";

	@Autowired
	MockMvc mvc;

	@MockBean
	private BookService bookService;
	@MockBean
	private LoanService loanService;

	@Test
	@DisplayName("Should be able to loan")
	void createLoanTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().isbn("001").customer("Jhon Doe").email("jhon@example.com").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1L).isbn("001").build();
		BDDMockito.given(bookService.getBookByIsbn("001"))
				.willReturn(Optional.of(book));

		Loan loan = Loan.builder().id(1L).customer("Jhon Doe").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willReturn(loan);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.content().string("1"));
	}

	@Test
	@DisplayName("Should be not to create a loan")
	void invalidIsbnCreateLoanTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().isbn("001").customer("Jhon Doe").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(bookService.getBookByIsbn("001"))
				.willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
	}

	@Test
	@DisplayName("Should be not to create a loan if book loaned")
	void loanedBookErrorCreateLoanTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().isbn("001").customer("Jhon Doe").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1L).isbn("001").build();
		BDDMockito.given(bookService.getBookByIsbn("001"))
				.willReturn(Optional.of(book));

		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("Book already loaned"));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		mvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book already loaned"));
	}

	@Test
	@DisplayName("Should be able to return a loan")
	void returnBookTest() throws Exception {
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		Loan loan = Loan.builder().id(1L).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));
		String json = new ObjectMapper().writeValueAsString(dto);

		mvc.perform(
				MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk());
		Mockito.verify(loanService, Mockito.times(1)).update(loan);
	}

	@Test
	@DisplayName("Should not be able to return a loan")
	void notReturnBookTest() throws Exception {
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		mvc.perform(
				MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("should be able to return a list filtred")
	void findLoansTest() throws Exception {
		Long id = 1L;
		Loan loan = createLoan();
		loan.setId(id);

		
		BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 100), 1));

		String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100", loan.getBook().getIsbn(), loan.getCustomer());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1))
				.andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}

	private Loan createLoan() {
    Book book = Book.builder().id(1L).isbn("001").build();
    return Loan.builder().book(book).customer("Jhon").loanDate(LocalDate.now()).build();
  }
}
