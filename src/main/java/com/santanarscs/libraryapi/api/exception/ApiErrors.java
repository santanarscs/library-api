package com.santanarscs.libraryapi.api.exception;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.santanarscs.libraryapi.exception.BusinessException;

import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

public class ApiErrors {

  @Getter
  private List<String> errors;

  public ApiErrors(BindingResult bindingResult) {
    this.errors = bindingResult.getAllErrors().stream().map(error -> error.getDefaultMessage())
        .collect(Collectors.toList());
  }

  public ApiErrors(BusinessException ex) {
    this.errors = Arrays.asList(ex.getMessage());
  }

  public ApiErrors(ResponseStatusException ex) {
    this.errors = Arrays.asList(ex.getReason());
  }

}
