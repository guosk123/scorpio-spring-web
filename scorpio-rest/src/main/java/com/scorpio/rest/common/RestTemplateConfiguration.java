package com.scorpio.rest.common;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * rest template 配置
 */
@Configuration
public class RestTemplateConfiguration {

  @Value("${restapi.timeout.ms}")
  private int timeout;

  @Bean
  public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
    RestTemplate restTempalte = new RestTemplate(factory);
    restTempalte.getMessageConverters().add(0,
        new StringHttpMessageConverter(StandardCharsets.UTF_8));
    return restTempalte;
  }

  @Bean
  public ClientHttpRequestFactory simpleClientHttpRequestFactory()
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(TrustAllStrategy.INSTANCE)
        .build();
    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext,
        NoopHostnameVerifier.INSTANCE);
    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

    factory.setHttpClient(httpClient);
    factory.setReadTimeout(timeout);// 单位为ms
    factory.setConnectTimeout(timeout);// 单位为ms
    return factory;
  }
}
