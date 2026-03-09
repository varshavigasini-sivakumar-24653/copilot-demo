package com.expense.tracker.handler;

import com.expense.tracker.dao.LoginEventDao;
import com.expense.tracker.dao.UserDao;
import com.expense.tracker.dto.GoogleLoginRequest;
import com.expense.tracker.model.User;
import com.expense.tracker.util.GoogleTokenVerifier;
import com.expense.tracker.util.JwtUtil;
import io.javalin.http.Handler;

import java.util.HashMap;
import java.util.Map;

public class AuthHandler {
    private final UserDao userDao = new UserDao();
    private final LoginEventDao loginEventDao = new LoginEventDao();

    public Handler googleLogin = ctx -> {
        GoogleLoginRequest body = ctx.bodyAsClass(GoogleLoginRequest.class);
        String idToken = body != null ? body.getIdToken() : null;
        if (idToken == null || idToken.isBlank()) {
            ctx.status(400).json(Map.of("error", "idToken is required"));
            return;
        }
        GoogleTokenVerifier.GoogleUserInfo info = GoogleTokenVerifier.verify(idToken);
        if (info.getEmail() == null || info.getEmail().isBlank()) {
            ctx.status(401).json(Map.of("error", "Invalid token"));
            return;
        }

        User user = userDao.findByEmail(info.getEmail()).orElseGet(() -> {
            String role = "USER";
            if (userDao.count() == 0) role = "ADMIN";
            return userDao.insert(info.getEmail(), info.getName(), info.getPicture(), role);
        });

        String ip = ctx.header("X-Forwarded-For");
        if (ip == null) ip = ctx.ip();
        loginEventDao.insert(user.getId(), ip);

        String token = JwtUtil.createToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userResponse(user));
        ctx.json(response);
    };

    public Handler me = ctx -> {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        ctx.json(userResponse(user));
    };

    private Map<String, Object> userResponse(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("email", u.getEmail());
        m.put("name", u.getName());
        m.put("pictureUrl", u.getPictureUrl());
        m.put("role", u.getRole());
        m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        return m;
    }
}
