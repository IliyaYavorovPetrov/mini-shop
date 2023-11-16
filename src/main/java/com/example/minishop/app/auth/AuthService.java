package com.example.minishop.app.auth;

import com.example.minishop.app.auth.dtos.IdTokenRequestDTO;
import com.example.minishop.app.users.UserRoleType;
import com.example.minishop.app.users.UsersService;
import com.example.minishop.app.users.models.UserModel;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class AuthService {
    private final UsersService userService;
    private final JWTUtils jwtUtils;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Value("${app.oauth2.provider.google.id}")
    private String googleID;

    public AuthService(UsersService userService, JWTUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;

        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleID))
                .build();
    }

    public String loginOAuthGoogle(IdTokenRequestDTO requestBody) {
        return "jwt-token";
    }

    @Transactional
    public UserModel createOrUpdateUser(UserModel account) {
        UserModel existingAccount = userService.getUserById(account.getId());
//        if (existingAccount == null) {
//            account.setRoles("ROLE_USER");
//            accountRepository.save(account);
//            return account;
//        }
//        existingAccount.setFirstName(account.getFirstName());
//        existingAccount.setLastName(account.getLastName());
//        existingAccount.setPictureUrl(account.getPictureUrl());
//        accountRepository.save(existingAccount);
        return existingAccount;
    }

    private UserModel verifyGoogleIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = googleIdTokenVerifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String id = payload.getSubject();
            String firstName = (String) payload.get("name");
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");

            return new UserModel(id, firstName, email, pictureUrl, UserRoleType.CLIENT);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }
}
