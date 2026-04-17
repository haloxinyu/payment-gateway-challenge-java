package com.checkout.payment.gateway.exception;

public class BankServiceUnavailableException extends RuntimeException {
  public BankServiceUnavailableException(String message) {
    super(message);
  }

  public BankServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}

