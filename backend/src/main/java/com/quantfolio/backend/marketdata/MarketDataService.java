package com.quantfolio.backend.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantfolio.backend.config.QuantfolioProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches daily OHLCV history from Yahoo Finance's public (key-less) chart endpoint. Chosen over
 * providers like Alpha Vantage so anyone cloning this project can run it immediately without
 * signing up for an API key.
 */
@Service
public class MarketDataService {

    private record CacheEntry(List<PriceBar> bars, Instant fetchedAt) {}

    private final RestTemplate restTemplate;
    private final QuantfolioProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    public MarketDataService(RestTemplate restTemplate, QuantfolioProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /** Returns daily bars for the configured history window, oldest first. */
    public List<PriceBar> getHistory(String ticker) {
        String key = normalize(ticker);
        CacheEntry cached = cache.get(key);
        if (cached != null && Duration.between(cached.fetchedAt(), Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cached.bars();
        }

        List<PriceBar> bars = fetchFromYahoo(key);
        cache.put(key, new CacheEntry(bars, Instant.now()));
        return bars;
    }

    public double getLatestClose(String ticker) {
        List<PriceBar> bars = getHistory(ticker);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No price data for " + ticker);
        }
        return bars.get(bars.size() - 1).close();
    }

    private String normalize(String ticker) {
        return ticker.trim().toUpperCase();
    }

    private List<PriceBar> fetchFromYahoo(String ticker) {
        int days = properties.getMarketData().getHistoryDays();
        String range = days <= 90 ? "6mo" : (days <= 260 ? "1y" : "2y");
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker
                + "?range=" + range + "&interval=1d";

        String json;
        try {
            json = restTemplate.getForObject(url, String.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not reach market data provider for " + ticker, ex);
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Malformed market data response for " + ticker);
        }

        JsonNode errorNode = root.path("chart").path("error");
        JsonNode resultArray = root.path("chart").path("result");
        if (!errorNode.isNull() && !errorNode.isMissingNode() || !resultArray.isArray() || resultArray.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown ticker or no data available: " + ticker);
        }

        JsonNode result = resultArray.get(0);
        JsonNode timestamps = result.path("timestamp");
        JsonNode quote = result.path("indicators").path("quote").get(0);
        String tz = result.path("meta").path("exchangeTimezoneName").asText("UTC");

        List<PriceBar> bars = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            JsonNode closeNode = quote.path("close").get(i);
            if (closeNode == null || closeNode.isNull()) continue;

            long epochSeconds = timestamps.get(i).asLong();
            LocalDate date = Instant.ofEpochSecond(epochSeconds).atZone(ZoneOffset.UTC).toLocalDate();

            double open = doubleOrClose(quote, "open", i, closeNode.asDouble());
            double high = doubleOrClose(quote, "high", i, closeNode.asDouble());
            double low = doubleOrClose(quote, "low", i, closeNode.asDouble());
            long volume = quote.path("volume").get(i) != null && !quote.path("volume").get(i).isNull()
                    ? quote.path("volume").get(i).asLong() : 0L;

            bars.add(new PriceBar(date, open, high, low, closeNode.asDouble(), volume));
        }

        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown ticker or no data available: " + ticker);
        }

        int from = Math.max(0, bars.size() - days);
        return bars.subList(from, bars.size());
    }

    private double doubleOrClose(JsonNode quote, String field, int i, double fallback) {
        JsonNode node = quote.path(field).get(i);
        return (node == null || node.isNull()) ? fallback : node.asDouble();
    }
}
