package com.app;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    public static Key getKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("JwtUtil: Token validation successful");
            return true;
        } catch (JwtException e) {
            System.out.println("JwtUtil: Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public static String extractUserId(String token, Key key) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

            Object userIdClaim = claims.get("userId");
            if (userIdClaim != null) {
                return userIdClaim.toString();
            }

            String subject = claims.getSubject();
            if (subject != null && subject.matches("\\d+")) {
                return subject;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String extractUsername(String token, Key key) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
