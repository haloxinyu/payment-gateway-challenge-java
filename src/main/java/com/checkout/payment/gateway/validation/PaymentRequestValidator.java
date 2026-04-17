package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.enums.SupportCurrency;
import com.checkout.payment.gateway.exception.PaymentRequestValidationException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.DateTimeException;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestValidator {

  public void validate(PostPaymentRequest request) {
    validateCardNumber(request.getCardNumber());
    validateExpiryDate(request.getExpiryYear(), request.getExpiryMonth());
    validateAmount(request.getAmount());
    validateCvv(request.getCvv());
    validateCurrency(request.getCurrency());
  }

  private static void validateCardNumber(String cardNumber) {
    if (cardNumber == null || !cardNumber.matches("\\d{14,19}")) {
      throw new PaymentRequestValidationException(ValidationMessages.INVALID_CARD_NUMBER);
    }
  }

  private static void validateExpiryDate(int expiryYear,
      int expiryMonth) {
    try {
      YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
      YearMonth now = YearMonth.now();
      if (!expiry.isAfter(now)) {
        throw new PaymentRequestValidationException(ValidationMessages.CARD_EXPIRED);
      }
    } catch (DateTimeException e) {
      throw new PaymentRequestValidationException(ValidationMessages.INVALID_EXPIRY_DATE);
    }
  }

  private static void validateAmount(int amount) {
    if (amount < 1) {
      throw new PaymentRequestValidationException(ValidationMessages.AMOUNT_MINIMUM);
    }
  }

  private static void validateCvv(int cvv) {
    if (cvv < 100 || cvv > 9999) {
      throw new PaymentRequestValidationException(ValidationMessages.CVV_INVALID);
    }
  }

  private static void validateCurrency(String currency) {
    if (currency == null || currency.isBlank()) {
      throw new PaymentRequestValidationException(ValidationMessages.CURRENCY_REQUIRED);
    }
    try {
      SupportCurrency.valueOf(currency);
    } catch (Exception e) {
      StringBuilder supported = new StringBuilder();
      for (SupportCurrency sc : SupportCurrency.values()) {
        if (!supported.isEmpty())
          supported.append(", ");
        supported.append(sc.getName());
      }
      String msg =
          String.format(ValidationMessages.CURRENCY_UNSUPPORTED, currency) + "; " + String.format(
              ValidationMessages.CURRENCY_SUPPORTED_LIST, supported);
      throw new PaymentRequestValidationException(msg);
    }
  }

}
