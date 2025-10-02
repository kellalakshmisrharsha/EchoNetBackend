package com.app;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
public class JwtFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Initialize the Key once
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Store userId in the exchange attributes
                if (claims.get("userId") != null) {
                    exchange.getAttributes().put("userId", Long.valueOf(claims.get("userId").toString()));
                }

            } catch (ExpiredJwtException e) {
                System.out.println("JwtFilter: Token expired: " + e.getMessage());
                return unauthorized(exchange);
            } catch (Exception e) {
                System.out.println("JwtFilter: Invalid token: " + e.getMessage());
                return unauthorized(exchange);
            }
        } else {
            System.out.println("JwtFilter: No Authorization header found");
            return unauthorized(exchange);
        }

        // Proceed with filter chain if token is valid
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
