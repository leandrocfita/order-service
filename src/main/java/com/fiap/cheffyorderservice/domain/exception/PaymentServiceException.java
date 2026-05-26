package com.fiap.cheffyorderservice.domain.exception;

public class PaymentServiceException extends RuntimeException {
    private final int httpStatus;

    public PaymentServiceException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public PaymentServiceException(String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}