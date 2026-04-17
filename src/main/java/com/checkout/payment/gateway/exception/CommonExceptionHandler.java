package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler({BadRequestFromBankException.class, BankServiceUnavailableException.class})
  public ResponseEntity<ErrorResponse> handleBankErrors(RuntimeException ex) {
    LOG.error(ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse("System busy, please try again later"), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Page not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOG.error("Unhandled exception", ex);
    return new ResponseEntity<>(new ErrorResponse("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
  }


  @ExceptionHandler(PaymentRequestValidationException.class)
  public ResponseEntity<ErrorResponse> handlePaymentValidation(PaymentRequestValidationException ex) {
    LOG.error("Payment validation failed: ", ex);
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      PaymentRequestValidationException ex) {
    LOG.error("Parse error: ", ex);
    return new ResponseEntity<>(new ErrorResponse("Bad request"), HttpStatus.BAD_REQUEST);
  }

}
