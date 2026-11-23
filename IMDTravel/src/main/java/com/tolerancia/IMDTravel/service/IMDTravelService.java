package com.tolerancia.IMDTravel.service;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.tolerancia.IMDTravel.repository.SalesFileRepository;
import com.tolerancia.IMDTravel.dto.BuyTicketResponse;
import com.tolerancia.IMDTravel.dto.ExchangeDto;
import com.tolerancia.IMDTravel.dto.FlightDto;
import com.tolerancia.IMDTravel.dto.SaleRecord;
import com.tolerancia.IMDTravel.dto.SellDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Service
public class IMDTravelService {

    private final RestClient airlinesClient;
    private final RestClient exchangeClient;
    private final RestClient fidelityClient;
    private final SalesFileRepository salesRepository;

    public IMDTravelService(
            RestClient airlinesClient,
            RestClient exchangeClient,
            RestClient fidelityClient,
            SalesFileRepository salesRepository) {
        this.airlinesClient = airlinesClient;
        this.exchangeClient = exchangeClient;
        this.fidelityClient = fidelityClient;
        this.salesRepository = salesRepository;
    }


    public BuyTicketResponse buyTicket(String flight, LocalDate day, String user) {
        String correlationId = MDC.get("cid");

        FlightDto flightInfo = airlinesClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/flight")
                        .queryParam("flight", flight)
                        .queryParam("day", day)
                        .build())
                .retrieve()
                .body(FlightDto.class);

        ExchangeDto exchange = exchangeClient.get()
                .uri("/convert")
                .retrieve()
                .body(ExchangeDto.class);

        BigDecimal valueUSD = flightInfo.priceUSD();
        BigDecimal valueBRL = valueUSD.multiply(exchange.rate()).setScale(2, RoundingMode.HALF_UP);
        int bonusPoints = valueUSD.intValue();


        SellDto sell = airlinesClient.post()
                .uri("/sell")
                .body(Map.of("flight", flight, "day", day))
                .retrieve()
                .body(SellDto.class);

        fidelityClient.post()
                .uri("/bonus")
                .body(Map.of("user", user, "bonus", bonusPoints))
                .retrieve()
                .toBodilessEntity();

        salesRepository.append(new SaleRecord(
                correlationId,
                sell.transactionId(),
                flight,
                day,
                user,
                valueUSD,
                valueBRL,
                Instant.now()
        ));

        return new BuyTicketResponse(
                sell.transactionId(),
                flight,
                day,
                user,
                valueUSD,
                valueBRL,
                exchange.rate(),
                exchange.asOf(),
                bonusPoints
        );
    }
}
