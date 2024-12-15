package com.example.idempotency.aspect;

import com.example.idempotency.annotation.Idempotent;
import com.example.idempotency.exception.ConflictException;
import com.example.idempotency.model.IdempotencyRequestResponse;
import com.example.idempotency.util.SpelUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class IdempotentAspect {

    public static final String IDEMPOTENT = "Idempotent";
    private final RedissonClient redisson;
    private final ExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public IdempotentAspect(RedissonClient redisson, ExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.redisson = redisson;
        this.expressionParser = expressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Around("@annotation(idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = SpelUtil.resolveSpelExpression(expressionParser, parameterNameDiscoverer, idempotent.key(), joinPoint);
        if (key == null || key.contains("null"))
            return joinPoint.proceed();
        Object request = Optional.ofNullable(idempotent.request())
                .filter(StringUtils::isNotEmpty)
                .map(rhc -> SpelUtil.resolveSpelExpression(expressionParser, parameterNameDiscoverer, rhc, joinPoint, Object.class))
                .orElse(null);
        RMapCache<String, Object> mapCache = redisson.getMapCache(IDEMPOTENT);
        IdempotencyRequestResponse requestResponse = Optional.ofNullable(mapCache.get(key))
                .filter(IdempotencyRequestResponse.class::isInstance)
                .map(IdempotencyRequestResponse.class::cast)
                .orElse(null);
        if (requestResponse != null) {
            if (!Objects.equals(requestResponse.getRequest(), request)) {
                throw new ConflictException("Request is different");
            }
            return requestResponse.getResponse();
        }
        Object response = joinPoint.proceed();
        long expiration = idempotent.expiration();
        mapCache.put(key, IdempotencyRequestResponse.builder().request(request).response(response).build(), expiration, TimeUnit.MILLISECONDS);
        return response;
    }
}
