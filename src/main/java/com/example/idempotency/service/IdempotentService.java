package com.example.idempotency.service;

import com.example.idempotency.annotation.Idempotent;
import com.example.idempotency.annotation.RLock;
import org.springframework.stereotype.Service;

import static jodd.util.ThreadUtil.sleep;

@Service
public class IdempotentService {

    @RLock(name = "#idempotencyKey")
    @Idempotent(key = "#idempotencyKey", request = "#message")
    public String doSomething(String idempotencyKey, String message) {
        sleep(5000);
        return "Idempotency Key: " + idempotencyKey + " Message: " + message;
    }
}

