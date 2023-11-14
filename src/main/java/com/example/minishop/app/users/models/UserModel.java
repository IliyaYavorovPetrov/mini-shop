package com.example.minishop.app.users.models;

import com.example.minishop.app.users.UserRoleType;

public class UserModel {
    private String id;
    private String name;
    private String email;
    private String imageURL;
    private UserRoleType role;

    public UserModel(String id, String name, String email, String imageURL, UserRoleType role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageURL = imageURL;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public UserRoleType getRole() {
        return role;
    }

    public void setRole(UserRoleType role) {
        this.role = role;
    }
}
