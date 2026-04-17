package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.BadRequestFromBankException;
import com.checkout.payment.gateway.exception.BankServiceUnavailableException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClientRest extends RestBaseClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClientRest.class);
  private static final String PAYMENTS_PATH = "/payments";

  public BankClientRest(RestTemplate restTemplate) {
    super(restTemplate, "http://localhost:8080");
  }

  public BankResponse sendPayment(PostPaymentRequest request) {
    try {
      LOG.debug("Calling bank with request: {}", request);
      return post(PAYMENTS_PATH, request, BankResponse.class);
    } catch (HttpClientErrorException e) {
        throw new BadRequestFromBankException("Bad Request", e);
    } catch (HttpServerErrorException e) {
        throw new BankServiceUnavailableException("Bank service unavailable", e);
    }
  }
}
