package com.quantfolio.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    /**
     * On free-tier hosting, this backend and the ml-service can both be cold at once — the
     * backend wakes up, then immediately calls a separately-sleeping ml-service, and that
     * combined wait can run past a minute. Without an explicit timeout here the call's actual
     * behavior was undefined; 90s covers realistic worst-case cold starts on both ends.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(90_000);
        factory.setReadTimeout(90_000);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(BROWSER_USER_AGENT);
        return restTemplate;
    }
}
