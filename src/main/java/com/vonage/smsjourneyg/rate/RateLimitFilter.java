package com.vonage.smsjourneyg.rate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter{
    
    private static final String SMS_API_PATH = "/api/sms";

    private final RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getHttpServletMapping().getPattern().equals(SMS_API_PATH)) {
            if (!rateLimiter.isAllowed()){
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "Too many requests. Please try again later."
                    }
                    """);

                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
