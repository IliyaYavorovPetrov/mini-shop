package com.example.minishop.app.auth;

import com.example.minishop.app.users.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTUtils {
    private static final long TOKEN_VALIDITY = 100000L;
    public static final String ROLE_CLAIM_NAME = "role";
    private static final String DELIMITER = "|";
    private final Key googleKey;
    private final Key facebookKey;

    private final Logger logger = LoggerFactory.getLogger(JWTUtils.class);

    public JWTUtils(
            @Value("${app.oauth2.provider.google.secret}") String googleClientSecret,
            @Value("${app.oauth2.provider.facebook.secret}") String facebookClientSecret
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(
                googleClientSecret.getBytes().length
                        + DELIMITER.getBytes().length
                        + googleClientSecret.getBytes().length
        );
        buffer.put(googleClientSecret.getBytes());
        buffer.put(DELIMITER.getBytes());
        buffer.put(googleClientSecret.getBytes());
        this.googleKey = Keys.hmacShaKeyFor(buffer.array());
        this.facebookKey = Keys.hmacShaKeyFor(facebookClientSecret.getBytes());
    }

    public String createToken(UserModel userModel, AuthProviderType authProviderType) {
        Key key = getProperKey(authProviderType);
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM_NAME, userModel.getRole());

        return Jwts.builder()
                .setSubject(userModel.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + TOKEN_VALIDITY))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication verifyAndGetAuthentication(String token, AuthProviderType authProviderType) {
        try {
            Key key = getProperKey(authProviderType);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get(ROLE_CLAIM_NAME, String.class));

            return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
        } catch (Exception ex) {
            logger.error("Failed to verify a token {}, with error message {}", token, ex.getMessage());
            return null;
        }
    }

    private Key getProperKey(AuthProviderType authProviderType) {
        return switch (authProviderType) {
            case GOOGLE -> googleKey;
            case FACEBOOK -> facebookKey;
        };
    }
}
