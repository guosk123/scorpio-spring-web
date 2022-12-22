package com.machloop.fpc.manager.boot.configuration;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfiguration {

  @Value("${elasticsearch.server.host}")
  private String serverHost;

  @Value("${elasticsearch.server.port}")
  private String serverPort;

  @Value("${elasticsearch.server.socket.timeout.ms}")
  private int timeout;

  @Bean
  public RestHighLevelClient restHighLevelClient() {

    HttpHost esHost = new HttpHost(serverHost, Integer.parseInt(serverPort), "http");
    RestHighLevelClient client = new RestHighLevelClient(
        RestClient.builder(esHost).setHttpClientConfigCallback(new HttpClientConfigCallback() {

          @Override
          public HttpAsyncClientBuilder customizeHttpClient(
              HttpAsyncClientBuilder httpClientBuilder) {
            httpClientBuilder.setMaxConnPerRoute(5); // 每个路由设置5个线程，默认是10个线程
            return httpClientBuilder;
          }
        }).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {

          @Override
          public RequestConfig.Builder customizeRequestConfig(
              RequestConfig.Builder requestConfigBuilder) {
            return requestConfigBuilder.setSocketTimeout(timeout);
          }
        }));
    return client;
  }

}

