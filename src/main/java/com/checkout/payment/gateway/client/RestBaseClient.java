package com.checkout.payment.gateway.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public abstract class RestBaseClient {

  protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
  protected final RestTemplate restTemplate;
  protected final String baseUrl;

  protected RestBaseClient(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  protected <TRequest, TResponse> TResponse post(String path, TRequest body, Class<TResponse> responseType) {
    String url =  baseUrl + path;
    LOG.debug("POST {} -> {}", url, body);
    return restTemplate.postForObject(url, body, responseType);
  }
}

