package io.github.spartateam6.commercepaymentsystem.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class PortoneClientConfig {

    @Bean
    public RestClient portoneRestClient(
            RestClient.Builder builder,
            @Value("${portone.api-secret}") String apiSecret
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));

        return builder
                .baseUrl("https://api.portone.io")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

}
