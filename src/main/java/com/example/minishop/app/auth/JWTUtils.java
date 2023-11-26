package com.example.minishop.app.auth;

import com.example.minishop.app.users.models.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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
    private static final long TOKEN_VALIDITY = 100000L;
    public static final String ROLE_CLAIM_NAME = "role";
    private final Key googleKey;

    public JWTUtils(@Value("${app.oauth2.provider.google.secret}") String googleClientSecret) {
        this.googleKey = Keys.hmacShaKeyFor(googleClientSecret.getBytes());
    }

    public String createToken(UserModel userModel, AuthProviderType authProviderType) {
        long now = (new Date()).getTime();
        Key key = getProperKey(authProviderType);

        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM_NAME, userModel.getRole());

        return Jwts.builder()
                .setSubject(userModel.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(now + TOKEN_VALIDITY))
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
            return null;
        }
    }

    private Key getProperKey(AuthProviderType authProviderType) {
        Key key = googleKey;
        if (authProviderType.equals(AuthProviderType.GOOGLE)) {
            key = googleKey;
        }

        return key;
    }
}
