package com.example.hoddog.filter;

import com.example.hoddog.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitFilter(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIP(request);
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            // ruxsat bor
            filterChain.doFilter(request, response);
        } else {
            // limit oshib ketdi
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "success": false,
                      "message": "Too many requests. Please try again later."
                    }
                    """);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
