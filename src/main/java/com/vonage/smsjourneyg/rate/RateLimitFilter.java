package com.vonage.smsjourneyg.rate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter{


    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getHttpServletMapping().getPattern().equals("/api/sms")){
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
