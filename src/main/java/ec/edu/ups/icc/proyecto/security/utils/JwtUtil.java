package ec.edu.ups.icc.proyecto.security.utils;

import ec.edu.ups.icc.proyecto.security.config.JwtProperties;
import ec.edu.ups.icc.proyecto.security.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetailsImpl userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getAccessExpiration(),
                ACCESS);
    }

    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return buildToken(
                userDetails,
                jwtProperties.getRefreshExpiration(),
                REFRESH);
    }

    private String buildToken(
            UserDetailsImpl userDetails,
            long expiration,
            String tokenType) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        String roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getId()))
                .claim("email", userDetails.getUsername())
                .claim("roles", roles)
                .claim(TOKEN_TYPE, tokenType)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE, String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token)
                && ACCESS.equals(getTokenType(token));
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token)
                && REFRESH.equals(getTokenType(token));
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}