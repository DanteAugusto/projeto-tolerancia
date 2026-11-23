package com.tolerancia.IMDTravel.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SaleRecord(
            String correlationId,
            String transactionId,
            String flight,
            LocalDate day,
            String user,
            BigDecimal valueUSD,
            BigDecimal valueBRL,
            Instant ts
    ) {}
