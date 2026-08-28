package com.quantfolio.backend.marketdata;

import java.time.LocalDate;

public record PriceBar(LocalDate date, double open, double high, double low, double close, long volume) {}
