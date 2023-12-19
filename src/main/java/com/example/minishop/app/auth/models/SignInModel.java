package com.example.minishop.app.auth.models;

public class SignInModel {
    private String token;
    private String userID;
    private String userRole;
    private String authProvider;

    public SignInModel(String token, String userID, String userRole, String authProvider) {
        this.token = token;
        this.userID = userID;
        this.userRole = userRole;
        this.authProvider = authProvider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }
}
