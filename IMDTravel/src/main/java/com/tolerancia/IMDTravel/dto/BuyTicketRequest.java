package com.tolerancia.IMDTravel.dto;

import java.time.LocalDate;

public record BuyTicketRequest(String flight, LocalDate day, String user) {}
