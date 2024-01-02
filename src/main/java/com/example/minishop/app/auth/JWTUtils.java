package com.example.minishop.app.auth;

import com.example.minishop.app.users.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JWTUtils {
    public static final long TOKEN_VALIDITY_SECS = 18000L;
    public static final String COOKIE_AUTH_TOKEN_NAME = "AUTH-TOKEN";
    public static final String COOKIE_AUTH_PROVIDER_NAME = "AUTH-PROVIDER";
    public static final String ROLE_CLAIM_NAME = "role";
    private static final String DELIMITER = "|";
    private final Key googleKey;
    private final Key facebookKey;

    private final Logger logger = LoggerFactory.getLogger(JWTUtils.class);

    public JWTUtils(
            @Value("${app.oauth2.provider.google.id}") String googleClientSecret,
            @Value("${app.oauth2.provider.facebook.secret}") String facebookClientSecret
    ) {
        this.googleKey = Keys.hmacShaKeyFor(googleClientSecret.getBytes());
        this.facebookKey = Keys.hmacShaKeyFor(facebookClientSecret.getBytes());
    }

    public String createToken(UserModel userModel, AuthProviderType authProviderType) {
        Key key = getProperKey(authProviderType);
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM_NAME, userModel.getRole());

        return Jwts.builder()
                .setSubject(userModel.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + TOKEN_VALIDITY_SECS))
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
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    claims.get(ROLE_CLAIM_NAME, String.class)
            );

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

    public ResponseCookie getCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .maxAge(maxAgeSeconds)
                .path("/")
                .secure(false)
                .httpOnly(false)
                .build();
    }
}
