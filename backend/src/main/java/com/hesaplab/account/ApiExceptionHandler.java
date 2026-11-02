package com.hesaplab.account;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(com.hesaplab.transfer.TransferService.Rejected.class)
    public ResponseEntity<ErrorDto> transferRejected(RuntimeException error) {
        return ResponseEntity.badRequest().body(new ErrorDto(error.getMessage()));
    }
    @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorDto> locked() {
        return ResponseEntity.status(409).body(new ErrorDto("Hesap şu anda kullanımda. İşlem yapılmadı; daha sonra tekrar deneyin."));
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> invalidBody() {
        return ResponseEntity.badRequest().body(new ErrorDto("Geçersiz transfer isteği."));
    }
    @ExceptionHandler(com.hesaplab.transfer.TransferService.Conflict.class)
    public ResponseEntity<ErrorDto> idempotencyConflict(RuntimeException error) {
        return ResponseEntity.status(409).body(new ErrorDto(error.getMessage()));
    }
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
