package com.example.fashionstoreclothing;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private boolean isAdmin;
    private LocalDateTime lastLoginTime;
    private String ipAddress;
    private boolean active;

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.active = false;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password != null && this.password.equals(password);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}