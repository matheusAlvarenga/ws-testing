package com.alvarenga.ws_testing.controllers;

import com.alvarenga.ws_testing.services.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/tickets")
@CrossOrigin
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public Map<String, String> createTicket(@RequestHeader("Authorization") String authorization) {
        String token = Optional.ofNullable(authorization)
                .map(t -> t.replace("Bearer ", ""))
                .orElse("");

        return Map.of("ticket", ticketService.buildAndSaveTicket(token));
    }
}
