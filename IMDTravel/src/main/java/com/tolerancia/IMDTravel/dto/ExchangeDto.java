package com.tolerancia.IMDTravel.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeDto(BigDecimal rate, Instant asOf) {}
