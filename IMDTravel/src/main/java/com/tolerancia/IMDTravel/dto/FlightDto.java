package com.tolerancia.IMDTravel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FlightDto(String flight,
                        LocalDate day,
                        BigDecimal priceUSD,
                        int seatsAvailable) {}
