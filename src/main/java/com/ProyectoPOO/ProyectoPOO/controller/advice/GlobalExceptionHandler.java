package com.ProyectoPOO.ProyectoPOO.controller.advice;

import jakarta.persistence.RollbackException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({TransactionSystemException.class, RollbackException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> handlePersistenceRelatedExceptions(Exception ex) {
        String message = extractRootMessage(ex);
        if (message == null || message.isBlank()) {
            message = "Error de validacion al guardar el recurso";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    private String extractRootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        if (current instanceof IllegalArgumentException) {
            return current.getMessage();
        }

        return current.getMessage();
    }
}
