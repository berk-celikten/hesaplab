package com.hesaplab.account;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ErrorDto(String message) { }
    @ExceptionHandler(AccountService.AccountNotFoundException.class)
    public ResponseEntity<ErrorDto> accountNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Hesap bulunamadı."));
    }
    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<ErrorDto> databaseUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorDto("Veriler şu anda alınamıyor. Lütfen tekrar deneyin."));
    }
}
