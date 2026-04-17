package com.checkout.payment.gateway.exception;

public class BadRequestFromBankException extends RuntimeException {

  public BadRequestFromBankException(String message) {
    super(message);
  }

  public BadRequestFromBankException(String message, Throwable cause) {
    super(message, cause);
  }
}

