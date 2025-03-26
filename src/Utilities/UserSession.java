package Utilities;

import java.util.*;

public class UserSession {
    private static UserSession instance;

    private Integer userId;
    private String username;
    private String userType;
    private final Map<String, Object> attributes;

    private UserSession() {
        attributes = new HashMap<>();
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void createSession(Integer userId, String username, String userType) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
    }

    public void clearSession() {
        userId = null;
        username = null;
        userType = null;
        attributes.clear();
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserType() {
        return userType;
    }
}