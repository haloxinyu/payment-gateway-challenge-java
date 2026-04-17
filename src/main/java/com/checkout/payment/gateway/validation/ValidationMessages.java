package com.checkout.payment.gateway.validation;

public final class ValidationMessages {

  private ValidationMessages() {}

  public static final String CURRENCY_REQUIRED = "Currency is required";
  public static final String CURRENCY_UNSUPPORTED = "Unsupported currency: %s";
  public static final String CURRENCY_SUPPORTED_LIST = "Supported currencies: %s";
  public static final String CVV_INVALID = "CVV must be 3 or 4 numeric digits";
  public static final String AMOUNT_MINIMUM = "Amount must be at least 1";
  public static final String INVALID_CARD_NUMBER = "Card number must be 14-19 numeric characters only.";
  public static final String INVALID_EXPIRY_DATE = "Invalid expiry date";
  public static final String CARD_EXPIRED = "Card expired";

}

