package com.example.fashionstoreclothing;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;

public class LoginRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;

    public LoginRecord(String username, LocalDateTime loginTime, String ipAddress) {
        this.username = username;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDuration() {
        if (logoutTime == null) {
            Duration duration = Duration.between(loginTime, LocalDateTime.now());
            return formatDuration(duration);
        } else {
            Duration duration = Duration.between(loginTime, logoutTime);
            return formatDuration(duration);
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
    public String getSessionDuration() {
        if (logoutTime == null) {
            return "Active";
        }
        long minutes = java.time.Duration.between(loginTime, logoutTime).toMinutes();
        return minutes + " minutes";
    }
}