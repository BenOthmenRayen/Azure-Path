package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    // Accept JSON key "fullName" and map it to this field
    @JsonProperty("fullName")
    @NotBlank(message = "Le nom ne doit pas être vide")
    private String name;

    @NotBlank(message = "L'email ne doit pas être vide")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe ne doit pas être vide")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins {min} caractères")
    private String password;

    // Accept "role" in input so requests won't fail if they send it.
    // BUT server will not trust this value and will assign ROLE_USER by default.
    @JsonProperty("role")
    private String role;

    public RegisterRequest() {}

    // getters / setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
