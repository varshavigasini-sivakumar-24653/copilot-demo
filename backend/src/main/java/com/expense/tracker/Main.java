package com.expense.tracker;

import com.expense.tracker.dao.UserDao;
import com.expense.tracker.handler.AdminHandler;
import com.expense.tracker.handler.AuthHandler;
import com.expense.tracker.handler.ExpenseHandler;
import com.expense.tracker.model.User;
import com.expense.tracker.util.Database;
import com.expense.tracker.util.JwtUtil;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.jsonwebtoken.Claims;

public class Main {

    public static void main(String[] args) {
        Database.init();
        AuthHandler authHandler = new AuthHandler();
        ExpenseHandler expenseHandler = new ExpenseHandler();
        AdminHandler adminHandler = new AdminHandler();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
        });

        app.before("/api/*", ctx -> {
            if (ctx.path().equals("/api/auth/google"))
                return;
            String auth = ctx.header("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Missing or invalid Authorization"));
                return;
            }
            String token = auth.substring(7);
            try {
                Claims claims = JwtUtil.parseToken(token);
                String userId = JwtUtil.getUserId(claims);
                User user = new UserDao().findById(userId).orElse(null);
                if (user == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "User not found"));
                    return;
                }
                ctx.attribute("user", user);
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(java.util.Map.of("error", "Invalid token"));
            }
        });

        app.post("/api/auth/google", authHandler.googleLogin);
        app.get("/api/me", authHandler.me);

        app.get("/api/expenses", expenseHandler::list);
        app.get("/api/expenses/summary", expenseHandler::summary);
        app.get("/api/expenses/{id}", expenseHandler::getOne);
        app.post("/api/expenses", expenseHandler::create);
        app.put("/api/expenses/{id}", expenseHandler::update);
        app.delete("/api/expenses/{id}", expenseHandler::delete);

        app.get("/api/admin/users", adminHandler::users);
        app.get("/api/admin/logins", adminHandler::logins);

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7001"));
        app.start(port);
        System.out.println("Expense Tracker API running on http://localhost:" + port);
    }
}
