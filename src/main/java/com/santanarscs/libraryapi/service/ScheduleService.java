package com.santanarscs.libraryapi.service;

import java.util.List;
import java.util.stream.Collectors;

import com.santanarscs.libraryapi.model.entity.Loan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

  @Value("${application.mail.lateloans.message}")
  private String message;

  private final LoanService loanService;
  private final EmailService emailService;

  @Scheduled(cron = CRON_LATE_LOANS)
  public void sendMailToLateLoans() {
    List<Loan> allLateLoans = loanService.getAllLateLoans();
    List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());
    emailService.sendMails(message, mailsList);
  }
}
