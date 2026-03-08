package com.example.demo.service;

import com.example.demo.dto.RecaptchaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    @Value("${recaptcha.secret}")
    private String secret;

    @Value("${recaptcha.threshold:0.5}")
    private double threshold;

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate;

    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // -----------------------
    // Verify token with Google
    // -----------------------
    public RecaptchaResponse verify(String token, String remoteIp) {
        if (token == null || token.isBlank()) {
            log.warn("verify() called with empty token");
            return null;
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secret);
            params.add("response", token);
            if (remoteIp != null && !remoteIp.isBlank()) {
                params.add("remoteip", remoteIp);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<RecaptchaResponse> response = restTemplate.postForEntity(VERIFY_URL, entity, RecaptchaResponse.class);

            if (response == null) {
                log.error("Empty ResponseEntity from Google reCAPTCHA siteverify");
                return null;
            }

            RecaptchaResponse body = response.getBody();
            if (body == null) {
                log.error("Empty body in Google reCAPTCHA response (status={})", response.getStatusCode());
                return null;
            }

            // Log the parsed response for debugging
            log.debug("Google reCAPTCHA raw response: {}", body);

            return body;

        } catch (RestClientException ex) {
            log.error("reCAPTCHA HTTP request failed", ex);
            return null;
        } catch (Exception ex) {
            log.error("Unexpected error while verifying reCAPTCHA", ex);
            return null;
        }
    }

    // -----------------------
    // Business validation - FIXED for v2
    // -----------------------
    public boolean isValid(String token, String action, String remoteIp) {

        RecaptchaResponse resp = verify(token, remoteIp);

        if (resp == null) {
            log.warn("reCAPTCHA response is NULL (verify failed or network error)");
            return false;
        }

        // Log full response summary
        log.info("reCAPTCHA RESPONSE => success={} score={} action={} hostname={} challenge_ts={} errors={}",
                resp.isSuccess(),
                resp.getScore(),
                resp.getAction(),
                resp.getHostname(),
                resp.getChallengeTs(),
                resp.getErrorCodes()
        );

        // 1) Check success flag (REQUIRED for both v2 and v3)
        if (!resp.isSuccess()) {
            log.warn("reCAPTCHA failed: success=false, errors={}", resp.getErrorCodes());
            return false;
        }

        // 2) Action match - only relevant for v3 (v2 doesn't use actions)
        if (action != null && resp.getAction() != null && !action.equals(resp.getAction())) {
            log.warn("reCAPTCHA action mismatch: expected='{}' but got='{}'", action, resp.getAction());
            return false;
        }

        // 3) ✅ FIXED: Handle score check for both v2 and v3
        double score = resp.getScore();

        // reCAPTCHA v2 returns score = 0.0 or doesn't include score at all
        // reCAPTCHA v3 returns score between 0.0 and 1.0
        if (score > 0.0) {
            // This is v3, check score threshold
            boolean scoreOk = score >= threshold;
            log.info("reCAPTCHA v3 detected - score check: score={} threshold={} passed={}",
                    score, threshold, scoreOk);
            if (!scoreOk) {
                log.warn("reCAPTCHA v3 score too low: {}", score);
                return false;
            }
        } else {
            // This is v2, no score check needed (success flag is enough)
            log.info("reCAPTCHA v2 detected (score=0.0) - skipping score check");
        }

        log.info("✅ reCAPTCHA validation passed");
        return true;
    }
}