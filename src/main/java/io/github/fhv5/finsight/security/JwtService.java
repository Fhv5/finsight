package io.github.fhv5.finsight.security;

import io.github.fhv5.finsight.config.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@AllArgsConstructor
public class JwtService {
    private final TokenProperties tokenProperties;

    public String generateToken(String jti, String userId) {
        return Jwts.builder()
                .id(jti)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(tokenProperties.accessTokenExpiration())))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(tokenProperties.secretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseClaims(String accessToken) {
        return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
    }
}
