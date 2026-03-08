package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for /api/auth/otp/send
 * uid is optional but recommended to tie OTP to a user id.
 */
public class SendOtpRequest {

    // Optional: tie OTP to UID created by Firebase; if null we fallback to email in controller
    private String uid;

    @NotBlank(message = "Email ne doit pas être vide")
    @Email(message = "Email invalide")
    private String email;

    public SendOtpRequest() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
