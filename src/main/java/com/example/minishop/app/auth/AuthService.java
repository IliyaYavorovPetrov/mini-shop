package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.SignInRequestDTO;
import com.example.minishop.app.auth.dtos.SignUpRequestDTO;
import com.example.minishop.app.auth.models.SignInModel;
import com.example.minishop.app.auth.models.SignUpModel;
import com.example.minishop.app.users.UserRoleType;
import com.example.minishop.app.users.UserService;
import com.example.minishop.app.users.models.UserModel;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.minishop.app.users.UserMapper.fromUserModelToUserRequestDTO;

@Service
public class AuthService {
    public static final long TOKEN_VALIDITY_SECS = 3600L;
    public static final String COOKIE_AUTH_TOKEN_NAME = "AUTH-TOKEN";
    public static final String COOKIE_AUTH_PROVIDER_NAME = "AUTH-PROVIDER";
    public static final String ROLE_CLAIM_NAME = "role";
    private static final String DELIMITER = "|";
    private final Key googleKey;
    private final Key facebookKey;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService usersService;
    private final GoogleIdTokenVerifier googleIDTokenVerifier;

    public AuthService(
            @Value("${app.oauth2.provider.google.id}") String googleClientID,
            @Value("${app.oauth2.provider.google.secret}") String googleClientSecret,
            @Value("${app.oauth2.provider.facebook.id}") String facebookClientID,
            @Value("${app.oauth2.provider.facebook.secret}") String facebookClientSecret,
            UserService usersService
    ) {
        this.usersService = usersService;

        googleIDTokenVerifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleClientID))
                .build();
        this.googleKey = Keys.hmacShaKeyFor((googleClientSecret + DELIMITER + googleClientSecret).getBytes());

        // TODO: add Facebook ID token verifier
        this.facebookKey = Keys.hmacShaKeyFor((facebookClientSecret + DELIMITER + facebookClientSecret).getBytes());
    }

    public Optional<SignUpModel> signUp(SignUpRequestDTO signUpRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyIdentityByAuthProvider(signUpRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            return Optional.empty();
        }

        String userID = usersService.createUser(fromUserModelToUserRequestDTO(userFromProvider.get()));

        return Optional.of(
                new SignUpModel(
                        createJWTToken(userFromProvider.get(), authProviderType),
                        userID,
                        UserRoleType.CLIENT.name(),
                        authProviderType.name()
                )
        );
    }

    public Optional<SignInModel> signIn(SignInRequestDTO signInRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyIdentityByAuthProvider(signInRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            return Optional.empty();
        }

        Optional<UserModel> user = usersService.getUserById(userFromProvider.get().getId());
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return Optional.of(
                new SignInModel(
                        createJWTToken(user.get(), authProviderType),
                        user.get().getId(),
                        user.get().getRole().toString(),
                        authProviderType.name()
                )
        );
    }

    private Optional<UserModel> verifyIdentityByAuthProvider(String token, AuthProviderType authProviderType) {
        try {
            if (authProviderType.equals(AuthProviderType.GOOGLE)) {
                GoogleIdToken googleIDToken = googleIDTokenVerifier.verify(token);
                if (googleIDToken == null) {
                    return Optional.empty();
                }

                GoogleIdToken.Payload payload = googleIDToken.getPayload();
                String id = payload.getSubject();
                String name = (String) payload.get("name");
                String email = payload.getEmail();
                String imageURL = (String) payload.get("picture");

                return Optional.of(new UserModel(id, name, email, imageURL, UserRoleType.CLIENT));
            } else if (authProviderType.equals(AuthProviderType.FACEBOOK)) {
                // TODO: Add logic to verify Facebook user
            }
        } catch (Exception ex) {
            logger.error("Failed to verify token with error message {}", ex.getMessage());
        }

        return Optional.empty();
    }

    public String createJWTToken(UserModel userModel, AuthProviderType authProviderType) {
        Key key = getProperAuthProviderKey(authProviderType);
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE_CLAIM_NAME, userModel.getRole().name());

        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(TOKEN_VALIDITY_SECS);
        return Jwts.builder()
                .setSubject(userModel.getId())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean verifyIfJWTTokenIsValid(String token, AuthProviderType authProviderType) {
        try {
            Key key = getProperAuthProviderKey(authProviderType);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    claims.get(ROLE_CLAIM_NAME, String.class)
            );

            return true;
        } catch (Exception ex) {
            logger.error("Failed to verify a token {}, with error message {}", token, ex.getMessage());
            return false;
        }
    }

    private Key getProperAuthProviderKey(AuthProviderType authProviderType) {
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
