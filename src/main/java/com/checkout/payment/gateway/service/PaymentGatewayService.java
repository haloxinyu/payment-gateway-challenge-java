package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankClientRest;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.enums.SupportCurrency;
import com.checkout.payment.gateway.exception.BadRequestFromBankException;
import com.checkout.payment.gateway.exception.BankServiceUnavailableException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.validation.PaymentRequestValidator;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankClientRest bankClient;
  private final PaymentRequestValidator paymentRequestValidator;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClientRest bankClient, PaymentRequestValidator paymentRequestValidator) {
    this.paymentRequestValidator = paymentRequestValidator;
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PostPaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PostPaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    LOG.debug("Processing payment request: {}", paymentRequest);
    paymentRequestValidator.validate(paymentRequest);
    BankResponse response;
    try {
      response = bankClient.sendPayment(paymentRequest);
    } catch (BankServiceUnavailableException | BadRequestFromBankException e) {
      throw e;
    }
    PaymentStatus status =
        response.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;

    PostPaymentResponse paymentResponse = new PostPaymentResponse();
    UUID id = UUID.randomUUID();
    paymentResponse.setId(id);
    paymentResponse.setStatus(status);
    paymentResponse.setCardNumberLastFour(paymentRequest.getCardNumberLastFour());
    paymentResponse.setExpiryYear(paymentRequest.getExpiryYear());
    paymentResponse.setExpiryMonth(paymentRequest.getExpiryMonth());
    paymentResponse.setAmount(paymentRequest.getAmount());
    paymentResponse.setCurrency(SupportCurrency.valueOf(paymentRequest.getCurrency()));
    paymentsRepository.add(paymentResponse);

    LOG.debug("Payment processed with ID {}", id);
    return paymentResponse;
  }
}
