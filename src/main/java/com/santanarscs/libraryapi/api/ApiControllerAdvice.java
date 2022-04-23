package com.santanarscs.libraryapi.api;

import com.santanarscs.libraryapi.api.exception.ApiErrors;
import com.santanarscs.libraryapi.exception.BusinessException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrors handleValidationExcepitons(MethodArgumentNotValidException ex) {
    BindingResult bindingResult = ex.getBindingResult();
    return new ApiErrors(bindingResult);
  }

  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrors handleBussinessExcepitons(BusinessException ex) {
    return new ApiErrors(ex);
  }

  @ExceptionHandler(ResponseStatusException.class)

  public ResponseEntity<ApiErrors> handleResponseStatusException(ResponseStatusException ex) {
    return new ResponseEntity(new ApiErrors(ex), ex.getStatus());
  }
}
