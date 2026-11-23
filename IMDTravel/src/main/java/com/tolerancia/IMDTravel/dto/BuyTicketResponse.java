package com.tolerancia.IMDTravel.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BuyTicketResponse(
            String transactionId,
            String flight,
            LocalDate day,
            String user,
            BigDecimal valueUSD,
            BigDecimal valueBRL,
            BigDecimal rate,
            Instant rateAsOf,
            int bonus
    ) {}
