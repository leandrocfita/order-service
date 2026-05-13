package com.fiap.cheffyorderservice.infrastructure.adapters.in.controllers.exception;

import com.fiap.cheffyorderservice.domain.exception.PaymentServiceException;
import com.fiap.cheffyorderservice.domain.exception.PaymentTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentTimeoutException.class)
    public ResponseEntity<ErrorResponse> handlePaymentTimeout(PaymentTimeoutException e) {
        log.warn("GlobalExceptionHandler.handlePaymentTimeout - WARN - PaymentTimeoutException captured: [{}]",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(
                new ErrorResponse("PAYMENT_TIMEOUT", e.getMessage(), HttpStatus.REQUEST_TIMEOUT.value())
        );
    }

    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ErrorResponse> handlePaymentService(PaymentServiceException e) {
        log.warn("GlobalExceptionHandler.handlePaymentService - WARN - PaymentServiceException captured: [{}] - HTTP Status: [{}]",
                e.getMessage(), e.getHttpStatus()
        );

        HttpStatus status = HttpStatus.resolve(e.getHttpStatus());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(
                new ErrorResponse("PAYMENT_ERROR", e.getMessage(), e.getHttpStatus())
        );
    }

    record ErrorResponse(String code, String message, int status) {}
}