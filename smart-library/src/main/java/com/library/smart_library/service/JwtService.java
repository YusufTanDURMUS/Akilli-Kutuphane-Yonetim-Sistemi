package com.library.smart_library.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // BU ANAHTAR ÇOK ÖNEMLİ! (Bunu kimse bilmemeli, yoksa sahte kart basabilirler)
    // Gerçek projede bu application.properties içinde saklanır.
    // Şimdilik buraya uzun ve karmaşık bir şifre koyuyoruz.
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // 1. Token Üret (Kullanıcı Adı ile)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Token'ın sahibi kim?
                .setIssuedAt(new Date(System.currentTimeMillis())) // Ne zaman basıldı?
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Saat geçerli
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // İmzala
                .compact();
    }

    // 2. Token Geçerli mi?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Token üzerindeki isim ile veritabanındaki isim aynı mı? VE Süresi dolmamış
        // mı?
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 3. Token'dan Kullanıcı Adını Çıkar
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- YARDIMCI METOTLAR (Private) ---

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}