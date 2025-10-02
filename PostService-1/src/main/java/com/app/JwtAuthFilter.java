package com.app;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Initialize secretKey once
        if (secretKey == null) {
            secretKey = JwtUtil.getKey(secret);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        System.out.println("JwtAuthFilter: Processing request to " + exchange.getRequest().getPath());
        System.out.println("JwtAuthFilter: Auth header present: " + (authHeader != null));

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("JwtAuthFilter: Token extracted: " + token.substring(0, Math.min(token.length(), 20)) + "...");

            try {
                if (JwtUtil.validateToken(token, secretKey)) {
                    // Extract userId
                    String userId = JwtUtil.extractUserId(token, secretKey);
                    if (userId != null && !userId.trim().isEmpty()) {
                        exchange.getAttributes().put("userId", userId);
                        System.out.println("JwtAuthFilter: UserId saved to exchange attributes: " + userId);
                    }

                    // Extract username for backward compatibility
                    String username = JwtUtil.extractUsername(token, secretKey);
                    if (username != null && !username.trim().isEmpty()) {
                        exchange.getAttributes().put("username", username);
                        System.out.println("JwtAuthFilter: Username saved to exchange attributes: " + username);
                    }

                    if (userId == null && username == null) {
                        System.out.println("JwtAuthFilter: No userId or username found in token!");
                    }
                } else {
                    System.out.println("JwtAuthFilter: Token validation failed");
                    return unauthorized(exchange);
                }
            } catch (ExpiredJwtException e) {
                System.out.println("JwtAuthFilter: Token expired: " + e.getMessage());
                return unauthorized(exchange);
            } catch (Exception e) {
                System.out.println("JwtAuthFilter: Error processing token: " + e.getMessage());
                e.printStackTrace();
                return unauthorized(exchange);
            }
        } else {
            System.out.println("JwtAuthFilter: No valid Authorization header found");
            return unauthorized(exchange);
        }

        // If everything is fine, continue the filter chain
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
