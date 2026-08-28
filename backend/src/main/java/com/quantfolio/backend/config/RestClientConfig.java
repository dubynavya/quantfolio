package com.quantfolio.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    /**
     * Yahoo Finance's chart endpoint returns "Edge: Too Many Requests" for requests without a
     * browser-like User-Agent, regardless of actual request volume. Setting one keeps the
     * free, key-less endpoint usable.
     */
    private static final ClientHttpRequestInterceptor BROWSER_USER_AGENT = (request, body, execution) -> {
        request.getHeaders().add("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36");
        return execution.execute(request, body);
    };

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(BROWSER_USER_AGENT);
        return restTemplate;
    }
}
