package com.example.idempotency.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;

import java.lang.reflect.Method;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpelUtil {

    public static final String PROPERTY_PATTERN = "\\$\\{([^}]+)}}";

    public static String resolveSpelExpression(ExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer, String spelExpression, ProceedingJoinPoint joinPoint) {
        return SpelUtil.resolveSpelExpression(expressionParser, parameterNameDiscoverer, spelExpression, joinPoint, String.class);
    }

    public static <T> T resolveSpelExpression(ExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer, String spelExpression, ProceedingJoinPoint joinPoint, Class<T> clazz) {
        if (StringUtils.isEmpty(spelExpression)) {
            return Optional.of(spelExpression).filter(clazz::isInstance).map(clazz::cast).orElse(null);
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();

        EvaluationContext evaluationContext = createEvaluationContext(parameterNameDiscoverer, method, args, target);
        return expressionParser.parseExpression(spelExpression).getValue(evaluationContext, clazz);
    }

    private static EvaluationContext createEvaluationContext(ParameterNameDiscoverer parameterNameDiscoverer, Method method, Object[] args, Object target) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(target, method, args, parameterNameDiscoverer);
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        if (parameterNames == null) return evaluationContext;
        for (int i = 0; i < parameterNames.length; i++) {
            evaluationContext.setVariable(parameterNames[i], args[i]);
        }
        return evaluationContext;
    }

}
