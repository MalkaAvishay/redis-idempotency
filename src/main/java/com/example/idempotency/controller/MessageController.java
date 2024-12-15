package com.example.idempotency.controller;

import com.example.idempotency.service.IdempotentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final IdempotentService idempotentService;

    public MessageController(IdempotentService idempotentService) {
        this.idempotentService = idempotentService;
    }

    @PostMapping("/doSomething")
    public String sendMessage(@RequestHeader String idempotencyKey, @RequestBody String message) {
        return idempotentService.doSomething(idempotencyKey, message);
    }
}
