package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecaptchaResponse {

    private boolean success;
    private double score;
    private String action;
    private String hostname;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    @JsonProperty("error-codes")
    private List<String> errorCodes;

    // getters & setters
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getChallengeTs() {
        return challengeTs;
    }
    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    public List<String> getErrorCodes() {
        return errorCodes;
    }
    public void setErrorCodes(List<String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    @Override
    public String toString() {
        return "RecaptchaResponse{" +
                "success=" + success +
                ", score=" + score +
                ", action='" + action + '\'' +
                ", hostname='" + hostname + '\'' +
                ", challengeTs='" + challengeTs + '\'' +
                ", errorCodes=" + errorCodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecaptchaResponse that = (RecaptchaResponse) o;
        return success == that.success &&
                Double.compare(that.score, score) == 0 &&
                Objects.equals(action, that.action) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(challengeTs, that.challengeTs) &&
                Objects.equals(errorCodes, that.errorCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, score, action, hostname, challengeTs, errorCodes);
    }
}
