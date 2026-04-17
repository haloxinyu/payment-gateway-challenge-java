package com.checkout.payment.gateway.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.enums.SupportCurrency;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import com.checkout.payment.gateway.client.BankClientRest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;
  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  private BankClientRest bankClient;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PostPaymentResponse payment = new PostPaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency(SupportCurrency.USD);
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour(4321);

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency().getName()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void whenPostValidPayment_thenReturnIdAndStorePayment() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424241\","
        + "\"expiry_month\": 12,"
        + "\"expiry_year\": 2028,"
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    BankResponse br = new BankResponse();
    br.setAuthorized(true);
    br.setAuthorizationCode("AUTH123");

    when(bankClient.sendPayment(any())).thenReturn(br);

    String body = mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    PostPaymentResponse resp = objectMapper.readValue(body, PostPaymentResponse.class);
    UUID id = resp.getId();
    var stored = paymentsRepository.get(id).orElse(null);
    assertThat(stored).isNotNull();
    assertThat(stored.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    assertThat(stored.getAmount()).isEqualTo(1050);
    assertThat(stored.getCurrency().getName()).isEqualTo("USD");
  }

  @Test
  void whenPostUnsupportedCurrency_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"expiry_month\": 12,"
        + "\"expiry_year\": 2028,"
        + "\"currency\": \"GBP\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported currency")));
  }

  @Test
  void whenPostInvalidCardNumber_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"abc\","
        + "\"expiry_month\": 12,"
        + "\"expiry_year\": 2028,"
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Card number must be 14-19")));
  }

  @Test
  void whenPostAmountZero_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"expiry_month\": 12,"
        + "\"expiry_year\": 2028,"
        + "\"currency\": \"USD\","
        + "\"amount\": 0,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Amount must be at least 1")));
  }

  @Test
  void whenPostInvalidCvv_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"expiry_month\": 12,"
        + "\"expiry_year\": 2028,"
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 12"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CVV must be 3 or 4")));
  }

  @Test
  void whenPostCardExpired_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"expiry_month\": 1,"
        + "\"expiry_year\": 2020,"
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Card expired")));
  }

  @Test
  void whenPostInvalidExpired_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"expiry_month\": 13,"
        + "\"expiry_year\": 20202,"
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid expiry date")));
  }

  @Test
  void whenPostMissingExpiry_then400() throws Exception {
    String json = "{"
        + "\"card_number\": \"4242424242424242\","
        + "\"currency\": \"USD\","
        + "\"amount\": 1050,"
        + "\"cvv\": 123"
        + "}";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid expiry date")));
  }
}
