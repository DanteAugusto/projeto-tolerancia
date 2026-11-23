package com.tolerancia.IMDTravel.controller;

import com.tolerancia.IMDTravel.dto.BuyTicketRequest;
import com.tolerancia.IMDTravel.dto.BuyTicketResponse;
import com.tolerancia.IMDTravel.dto.ErrorMessage;
import com.tolerancia.IMDTravel.repository.SalesFileRepository;
import com.tolerancia.IMDTravel.service.IMDTravelService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IMDTravelControllerTest {

    @Test
    void buyTicketShouldReturn400WhenRequestHasMissingOrBlankFields() {
        IMDTravelService service = mock(IMDTravelService.class);
        SalesFileRepository repository = mock(SalesFileRepository.class);
        IMDTravelController controller = new IMDTravelController(service, repository);

        ResponseEntity<?> missingDay = controller.buyTicket(new BuyTicketRequest("IMD100", null, "ana"));
        assertEquals(HttpStatus.BAD_REQUEST, missingDay.getStatusCode());
        assertEquals(new ErrorMessage("invalid-request"), missingDay.getBody());

        ResponseEntity<?> blankUser = controller.buyTicket(new BuyTicketRequest("IMD100", LocalDate.of(2026, 12, 1), " "));
        assertEquals(HttpStatus.BAD_REQUEST, blankUser.getStatusCode());
        assertEquals(new ErrorMessage("invalid-request"), blankUser.getBody());

        verifyNoInteractions(service);
    }

    @Test
    void buyTicketShouldReturn201WhenValidRequest() {
        IMDTravelService service = mock(IMDTravelService.class);
        SalesFileRepository repository = mock(SalesFileRepository.class);
        IMDTravelController controller = new IMDTravelController(service, repository);

        LocalDate day = LocalDate.of(2026, 12, 1);
        BuyTicketResponse expected = new BuyTicketResponse(
                "tx-1", "IMD100", day, "ana",
                new BigDecimal("350.50"), new BigDecimal("1927.75"),
                new BigDecimal("5.50"), Instant.parse("2026-11-23T12:00:00Z"), 350
        );
        when(service.buyTicket("IMD100", day, "ana")).thenReturn(expected);

        ResponseEntity<?> response = controller.buyTicket(new BuyTicketRequest("IMD100", day, "ana"));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }
}
