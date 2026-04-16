package com.example.appsocialver2.Models;

public class User {
    private String userId;      // UID từ Firebase Authentication
    private String username;
    private String email;
    private String avatarUrl;   // Đường dẫn ảnh đại diện
    private String bio;         // Giới thiệu bản thân ngắn gọn

    // Constructor trống bắt buộc cho Firebase
    public User() {}

    public User(String userId, String username, String email, String avatarUrl, String bio) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    // Getter và Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
