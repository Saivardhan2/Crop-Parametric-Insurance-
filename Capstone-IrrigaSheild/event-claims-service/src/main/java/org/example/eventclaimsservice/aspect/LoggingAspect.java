package org.example.eventclaimsservice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(
                    LoggingAspect.class
            );

    @Around(
            "execution(* org.example.eventclaimsservice..service..*(..))"
    )
    public Object logServiceMethod(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        String method =
                joinPoint.getSignature()
                        .toShortString();

        long startTime =
                System.currentTimeMillis();

        log.info(
                "Started method={}",
                method
        );

        try {
            Object result = joinPoint.proceed();

            if (result instanceof Mono<?> mono) {
                return logMono(
                        mono,
                        method,
                        startTime
                );
            }

            if (result instanceof Flux<?> flux) {
                return logFlux(
                        flux,
                        method,
                        startTime
                );
            }

            logSuccess(
                    method,
                    startTime
            );

            return result;

        } catch (Throwable exception) {
            logFailure(
                    method,
                    startTime,
                    exception
            );

            throw exception;
        }
    }

    private Mono<?> logMono(
            Mono<?> mono,
            String method,
            long startTime) {

        return mono
                .doOnSuccess(result ->
                        logSuccess(
                                method,
                                startTime
                        )
                )
                .doOnError(exception ->
                        logFailure(
                                method,
                                startTime,
                                exception
                        )
                );
    }

    private Flux<?> logFlux(
            Flux<?> flux,
            String method,
            long startTime) {

        return flux
                .doOnComplete(() ->
                        logSuccess(
                                method,
                                startTime
                        )
                )
                .doOnError(exception ->
                        logFailure(
                                method,
                                startTime,
                                exception
                        )
                );
    }

    private void logSuccess(
            String method,
            long startTime) {

        log.info(
                "Completed method={} durationMs={}",
                method,
                elapsedTime(startTime)
        );
    }

    private void logFailure(
            String method,
            long startTime,
            Throwable exception) {

        log.error(
                "Failed method={} durationMs={} error={}",
                method,
                elapsedTime(startTime),
                exception.getMessage(),
                exception
        );
    }

    private long elapsedTime(long startTime) {

        return System.currentTimeMillis()
                - startTime;
    }
}