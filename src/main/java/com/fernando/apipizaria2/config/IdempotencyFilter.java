package com.fernando.apipizaria2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final Set<String> processedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String idempotencyKey = request.getHeader("X-Idempotency-Key");
            if (idempotencyKey != null) {
                if (processedKeys.contains(idempotencyKey)) {
                    response.setStatus(HttpStatus.CONFLICT.value());
                    response.getWriter().write("Idempotency key already processed");
                    return;
                }
                processedKeys.add(idempotencyKey);
            }
        }
        filterChain.doFilter(request, response);
    }
}
