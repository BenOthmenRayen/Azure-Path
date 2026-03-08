package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginVerifyRequest {

    @NotBlank(message = "idToken ne doit pas être vide")
    private String idToken;

    public LoginVerifyRequest() {}

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
