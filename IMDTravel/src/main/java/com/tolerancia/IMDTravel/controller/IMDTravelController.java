package com.tolerancia.IMDTravel.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import com.tolerancia.IMDTravel.repository.SalesFileRepository;
import com.tolerancia.IMDTravel.service.IMDTravelService;
import com.tolerancia.IMDTravel.dto.BuyTicketRequest;
import com.tolerancia.IMDTravel.dto.BuyTicketResponse;
import com.tolerancia.IMDTravel.dto.ErrorMessage;
import com.tolerancia.IMDTravel.dto.SaleRecord;

import java.util.List;
@RestController
public class IMDTravelController {

    private final IMDTravelService travelService;
    private final SalesFileRepository salesRepository;

    public IMDTravelController(IMDTravelService travelService, SalesFileRepository salesRepository) {
        this.travelService = travelService;
        this.salesRepository = salesRepository;
    }

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    public ResponseEntity<ErrorMessage> handleSoldOut() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorMessage("sold-out"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage>handleInvalidJson() {
        return ResponseEntity.badRequest()
                .body(new ErrorMessage("invalid-request"));
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorMessage> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage("not-found"));
    }

    @PostMapping("/buyTicket")
    public ResponseEntity<?> buyTicket(@RequestBody BuyTicketRequest request) {
        if (request == null
            || request.flight() == null || request.flight().isBlank()
            || request.day() == null
            || request.user() == null || request.user().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorMessage("invalid-request"));
        }
        BuyTicketResponse response =
                travelService.buyTicket(request.flight(), request.day(), request.user());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sales")
    public List<SaleRecord> getSales() {
        return salesRepository.findAll();
    }
}
