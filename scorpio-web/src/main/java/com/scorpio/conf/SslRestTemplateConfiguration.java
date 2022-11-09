package com.scorpio.conf;

import java.nio.charset.StandardCharsets;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SslRestTemplateConfiguration {

  public RestTemplate sslRestTemplate() throws Exception {

    SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
        new SSLContextBuilder().loadTrustMaterial(TrustAllStrategy.INSTANCE).build(),
        NoopHostnameVerifier.INSTANCE);

    HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
        httpClient);
    factory.setReadTimeout(5000);// 单位为ms
    factory.setConnectTimeout(5000);// 单位为ms
    RestTemplate restTempalte = new RestTemplate(factory);
    restTempalte.getMessageConverters().add(0,
        new StringHttpMessageConverter(StandardCharsets.UTF_8));

    return restTempalte;
  }
}

