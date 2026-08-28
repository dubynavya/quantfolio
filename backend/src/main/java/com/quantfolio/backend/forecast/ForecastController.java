package com.quantfolio.backend.forecast;

import com.quantfolio.backend.config.QuantfolioProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thin proxy to the ml-service FastAPI forecasting/Monte-Carlo microservice, so the frontend
 * only ever talks to this backend and doesn't need to know the ML service exists.
 */
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final RestTemplate restTemplate;
    private final QuantfolioProperties properties;

    public ForecastController(RestTemplate restTemplate, QuantfolioProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @GetMapping("/{ticker}")
    public String forecast(@PathVariable String ticker, @RequestParam(defaultValue = "10") int days) {
        String url = properties.getMlService().getBaseUrl() + "/forecast/" + ticker + "?days=" + days;
        return proxyGet(url);
    }

    @GetMapping("/{ticker}/monte-carlo")
    public String monteCarlo(@PathVariable String ticker,
                              @RequestParam(defaultValue = "1000") int simulations,
                              @RequestParam(defaultValue = "20") int horizonDays) {
        String url = properties.getMlService().getBaseUrl() + "/risk/monte-carlo/" + ticker
                + "?simulations=" + simulations + "&horizon_days=" + horizonDays;
        return proxyGet(url);
    }

    private String proxyGet(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML forecasting service unavailable", ex);
        }
    }
}
