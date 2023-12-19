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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static com.example.minishop.app.users.UserMapper.fromUserModelToUserRequestDTO;

@Service
public class AuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserService usersService;
    private final JWTUtils jwtUtils;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public AuthService(
            @Value("${app.oauth2.provider.google.id}") String googleClientID,
            @Value("${app.oauth2.provider.facebook.id}") String facebookClientID,
            UserService usersService,
            JWTUtils jwtUtils
    ) {
        this.usersService = usersService;
        this.jwtUtils = jwtUtils;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientID))
                .build();
    }

    public Optional<SignUpModel> signUp(SignUpRequestDTO signUpRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyToken(signUpRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            return Optional.empty();
        }

        String userID = usersService.createUser(fromUserModelToUserRequestDTO(userFromProvider.get()));

        return Optional.of(
                new SignUpModel(
                        jwtUtils.createToken(userFromProvider.get(), authProviderType),
                        userID,
                        UserRoleType.CLIENT.name(),
                        authProviderType.name()
                )
        );
    }

    public Optional<SignInModel> signIn(SignInRequestDTO signInRequestDTO, AuthProviderType authProviderType) {
        Optional<UserModel> userFromProvider = verifyToken(signInRequestDTO.token(), authProviderType);
        if (userFromProvider.isEmpty()) {
            return Optional.empty();
        }

        Optional<UserModel> user = usersService.getUserById(userFromProvider.get().getId());
        if (user.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return Optional.of(
                new SignInModel(
                        jwtUtils.createToken(user.get(), authProviderType),
                        user.get().getId(),
                        user.get().getRole().toString(),
                        authProviderType.name()
                )
        );
    }

    private Optional<UserModel> verifyToken(String token, AuthProviderType authProviderType) {
        try {
            if (authProviderType.equals(AuthProviderType.GOOGLE)) {
                GoogleIdToken googleIDToken = googleIdTokenVerifier.verify(token);
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

            }
        } catch (Exception ex) {
            logger.error("Failed to verify token with error message {}", ex.getMessage());
        }

        return Optional.empty();
    }
}
