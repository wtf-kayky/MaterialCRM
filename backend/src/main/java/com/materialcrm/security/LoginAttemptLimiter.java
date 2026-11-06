package com.materialcrm.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class LoginAttemptLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public void check(String email, String remoteAddress) {
        String key = key(email, remoteAddress);
        AttemptState state = attempts.get(key);
        if (state == null) {
            return;
        }

        Instant now = Instant.now();
        if (now.isAfter(state.firstFailureAt().plus(WINDOW))) {
            attempts.remove(key, state);
            return;
        }
        if (state.failures() >= MAX_FAILURES) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas. Tente novamente em alguns minutos.");
        }
    }

    public void registerFailure(String email, String remoteAddress) {
        String key = key(email, remoteAddress);
        Instant now = Instant.now();
        attempts.compute(key, (ignored, current) -> {
            if (current == null || now.isAfter(current.firstFailureAt().plus(WINDOW))) {
                return new AttemptState(1, now);
            }
            return new AttemptState(current.failures() + 1, current.firstFailureAt());
        });
    }

    public void registerSuccess(String email, String remoteAddress) {
        attempts.remove(key(email, remoteAddress));
    }

    private String key(String email, String remoteAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = remoteAddress == null ? "" : remoteAddress;
        return normalizedEmail + "|" + normalizedAddress;
    }

    private record AttemptState(int failures, Instant firstFailureAt) {}
}
