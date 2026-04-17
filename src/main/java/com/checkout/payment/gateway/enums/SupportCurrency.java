package com.checkout.payment.gateway.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SupportCurrency {
  USD("USD"),
  CNY("CNY"),
  EUR("EUR");

  private final String name;

  SupportCurrency(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return this.name;
  }
}
