package com.fernando.apipizaria2.config;

import com.fernando.apipizaria2.repositories.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String MASTER_API_KEY = "api-pizzaria-secret-key-272"; // Chave mestre de contingência
    
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // Ignora h2-console, swagger, api-docs, rotas de erro e rotas de autenticação (geração de chave)
        if (path.startsWith("/h2-console") || path.startsWith("/swagger-ui") || path.startsWith("/api-docs") || path.startsWith("/error") || path.startsWith("/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        boolean isValid = false;
        if (apiKey != null) {
            if (MASTER_API_KEY.equals(apiKey)) {
                isValid = true;
            } else {
                isValid = apiKeyRepository.findByChaveAndAtivoTrue(apiKey).isPresent();
            }
        }

        if (!isValid) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Senha inválida ou ausente.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
