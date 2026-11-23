package com.tolerancia.IMDTravel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tolerancia.IMDTravel.dto.HealthResponse;
import com.tolerancia.IMDTravel.repository.SalesFileRepository;

import java.time.Duration;
import java.time.Instant;

@RestController
public class HealthController {

    private final Instant startedAt = Instant.now();
    private final SalesFileRepository salesRepository;

    public HealthController(SalesFileRepository salesRepository) {
        this.salesRepository = salesRepository;
    }


    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        long uptimeS = Duration.between(startedAt, Instant.now()).getSeconds();
        boolean healthy = checkServiceHealth();

        if (!healthy) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResponse("down", uptimeS));
        }
        return ResponseEntity.ok(new HealthResponse("ok", uptimeS));
    }

    private boolean checkServiceHealth() {
        return salesRepository.isWritable();
    }
}
