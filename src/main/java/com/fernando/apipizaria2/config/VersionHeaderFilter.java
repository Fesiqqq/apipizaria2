package com.fernando.apipizaria2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the optional {@code X-API-Version} header and makes it available as a request attribute.
 * This can be used by controllers or other filters to adapt behavior based on the requested version.
 * The filter does not modify the request URI; it only records the version for downstream components.
 */
@Component
public class VersionHeaderFilter extends OncePerRequestFilter {

    private static final String VERSION_HEADER = "X-API-Version";
    private static final String VERSION_ATTR = "apiVersion";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String version = request.getHeader(VERSION_HEADER);
        if (version != null && ("1".equals(version) || "2".equals(version))) {
            // Store the version for later use (e.g., in controllers or other filters)
            request.setAttribute(VERSION_ATTR, version);
        }
        // Continue processing the request
        filterChain.doFilter(request, response);
    }
}
