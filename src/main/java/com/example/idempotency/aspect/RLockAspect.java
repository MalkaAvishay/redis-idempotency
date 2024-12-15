package com.example.idempotency.aspect;

import com.example.idempotency.annotation.RLock;
import com.example.idempotency.exception.ConflictException;
import com.example.idempotency.util.SpelUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RLockAspect {

    private final RedissonClient redisson;
    private final ExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public RLockAspect(RedissonClient redisson, ExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.redisson = redisson;
        this.expressionParser = expressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Around("@annotation(rLock)")
    public Object lockAround(ProceedingJoinPoint joinPoint, RLock rLock) throws Throwable {
        String lockName = SpelUtil.resolveSpelExpression(expressionParser, parameterNameDiscoverer, rLock.name(), joinPoint);
        org.redisson.api.RLock lock = redisson.getLock(lockName);
        boolean locked = lock.tryLock(rLock.waitTime(), rLock.leaseTime(), TimeUnit.MILLISECONDS);
        if (!locked) {
            throw new ConflictException("resource is locked");
        }
        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }
}
