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

        String path = request.getRequestURI();
        // Ignora recursos estáticos, h2-console, swagger, api-docs e rotas de erro
        if (path.startsWith("/h2-console") || path.startsWith("/swagger-ui") || path.startsWith("/api-docs") || path.startsWith("/error") ||
            path.equals("/") || path.equals("/index.html") || path.equals("/app.js") || path.equals("/styles.css") || path.equals("/favicon.ico") ||
            path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".ico") || path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".svg")) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String idempotencyKey = request.getHeader("X-Idempotency-Key");
            if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Chave de idempotência (X-Idempotency-Key) ausente.");
                return;
            }
            if (processedKeys.contains(idempotencyKey)) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Chave de idempotência já processada.");
                return;
            }
            processedKeys.add(idempotencyKey);
        }
        filterChain.doFilter(request, response);
    }
}
