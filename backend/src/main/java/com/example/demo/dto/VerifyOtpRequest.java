package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for /api/auth/otp/verify
 */
public class VerifyOtpRequest {

    @NotBlank(message = "uid ne doit pas être vide")
    private String uid;

    @NotBlank(message = "otp ne doit pas être vide")
    private String otp;

    public VerifyOtpRequest() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
