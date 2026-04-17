package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.BadRequestFromBankException;
import com.checkout.payment.gateway.exception.BankServiceUnavailableException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankClientRestTest {

  private RestTemplate restTemplate;
  private BankClientRest bankClient;

  @BeforeEach
  void setUp() {
    restTemplate = mock(RestTemplate.class);
    bankClient = new BankClientRest(restTemplate);
  }

  @Test
  void sendPayment_whenAuthorized_returnsBankResponseAuthorized() {
    PostPaymentRequest req = new PostPaymentRequest();
    req.setCardNumber("4111111111111111");
    BankResponse resp = new BankResponse();
    resp.setAuthorized(true);
    resp.setAuthorizationCode("AUTH123");

    when(restTemplate.postForObject("http://localhost:8080/payments", req, BankResponse.class))
        .thenReturn(resp);

    BankResponse result = bankClient.sendPayment(req);
    assertNotNull(result);
    assertTrue(result.isAuthorized());
    assertEquals("AUTH123", result.getAuthorizationCode());
  }

  @Test
  void sendPayment_whenDeclined_returnsBankResponseNotAuthorized() {
    PostPaymentRequest req = new PostPaymentRequest();
    req.setCardNumber("4111111111111112");
    BankResponse resp = new BankResponse();
    resp.setAuthorized(false);

    when(restTemplate.postForObject("http://localhost:8080/payments", req, BankResponse.class))
        .thenReturn(resp);

    BankResponse result = bankClient.sendPayment(req);
    assertNotNull(result);
    assertFalse(result.isAuthorized());
  }

  @Test
  void sendPayment_whenBankReturns400_throwsBadRequestFromBankException() {
    PostPaymentRequest req = new PostPaymentRequest();
    when(restTemplate.postForObject("http://localhost:8080/payments", req, BankResponse.class))
        .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

    assertThrows(BadRequestFromBankException.class, () -> bankClient.sendPayment(req));
  }

  @Test
  void sendPayment_whenBankUnavailable_throwsBankServiceUnavailableException() {
    PostPaymentRequest req = new PostPaymentRequest();
    when(restTemplate.postForObject("http://localhost:8080/payments", req, BankResponse.class))
        .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, null, null));

    assertThrows(BankServiceUnavailableException.class, () -> bankClient.sendPayment(req));
  }
}